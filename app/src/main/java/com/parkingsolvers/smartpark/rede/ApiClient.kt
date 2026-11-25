package com.parkingsolvers.smartpark.rede

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Sinalizador simples de invalidação para que telas abertas (como Dashboard e listas)
 * recarreguem automaticamente após qualquer operação de escrita ou mudança de sessão.
 */
class SincronizadorDados {
    private val _versao = MutableStateFlow(0L)
    val versao: StateFlow<Long> = _versao.asStateFlow()

    fun notificarMudanca() {
        _versao.update { it + 1L }
    }
}

class AuthInterceptor(private val sessaoStore: SessaoStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath
        if (path.endsWith("/auth/login") || path.endsWith("/auth/refresh")) {
            return chain.proceed(original)
        }
        val token = sessaoStore.carregar()?.accessToken ?: return chain.proceed(original)
        val autenticada = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(autenticada)
    }
}

/**
 * Em caso de HTTP 401 numa rota protegida, tenta renovar o token via POST /auth/refresh.
 * Se a renovação falhar, limpa a sessão e aciona [onSessaoExpirada] para voltar ao login.
 */
class TokenAuthenticator(
    private val sessaoStore: SessaoStore,
    private val refreshApiProvider: () -> SmartParkApi,
    private val onSessaoExpirada: () -> Unit
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.endsWith("/auth/login") || path.endsWith("/auth/refresh")) return null
        if (contarTentativas(response) >= 2) return null

        val tokenUsado = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

        synchronized(lock) {
            val atual = sessaoStore.carregar() ?: return null

            // Outra requisição concorrente já renovou o token.
            if (tokenUsado != null && atual.accessToken != tokenUsado) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${atual.accessToken}")
                    .build()
            }

            val novo = runCatching {
                val resp = refreshApiProvider().refreshSync(RefreshApiRequest(atual.refreshToken)).execute()
                if (resp.isSuccessful) resp.body() else null
            }.getOrNull()

            if (novo == null) {
                sessaoStore.limpar()
                onSessaoExpirada()
                return null
            }

            sessaoStore.salvar(CredenciaisSessao(novo.accessToken, novo.refreshToken, novo.sessao))
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${novo.accessToken}")
                .build()
        }
    }

    private fun contarTentativas(response: Response): Int {
        var count = 1
        var anterior = response.priorResponse
        while (anterior != null) {
            count++
            anterior = anterior.priorResponse
        }
        return count
    }
}

object ApiClient {

    fun criar(
        baseUrl: String,
        sessaoStore: SessaoStore,
        debug: Boolean,
        onSessaoExpirada: () -> Unit
    ): SmartParkApi {
        val urlNormalizada = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        val logging = HttpLoggingInterceptor().apply {
            level = if (debug) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

        // Cliente dedicado sem authenticator para a chamada síncrona de /auth/refresh.
        val refreshClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val refreshApi = Retrofit.Builder()
            .baseUrl(urlNormalizada)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SmartParkApi::class.java)

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(sessaoStore))
            .authenticator(TokenAuthenticator(sessaoStore, { refreshApi }, onSessaoExpirada))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(urlNormalizada)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SmartParkApi::class.java)
    }
}
