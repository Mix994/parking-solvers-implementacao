package com.parkingsolvers.smartpark.rede

import com.google.gson.Gson
import com.parkingsolvers.smartpark.compartilhado.exceptions.BloqueioTentativasException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.CredenciaisInvalidasException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object ApiErrorMapper {

    private val gson = Gson()

    suspend fun <T> executar(bloco: suspend () -> T): T {
        return try {
            bloco()
        } catch (e: HttpException) {
            throw traduzirHttp(e)
        } catch (e: SocketTimeoutException) {
            throw RegraDeNegocioException("Tempo esgotado ao comunicar com o servidor. Tente novamente.")
        } catch (e: IOException) {
            throw RegraDeNegocioException("Sem conexão com o servidor. Verifique sua internet e tente novamente.")
        }
    }

    fun traduzirHttp(e: HttpException): RegraDeNegocioException {
        val corpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val erro = corpo?.let { runCatching { gson.fromJson(it, ErroApiResponse::class.java) }.getOrNull() }
        val mensagem = erro?.mensagem?.takeIf { it.isNotBlank() }
        val campos = erro?.campos.orEmpty()

        return when (e.code()) {
            400, 422 -> if (campos.isNotEmpty()) {
                ValidacaoException(campos, mensagem ?: campos.values.first())
            } else {
                RegraDeNegocioException(mensagem ?: "Dados inválidos ou regra de negócio violada.")
            }
            401 -> CredenciaisInvalidasException(mensagem ?: "Sessão expirada ou credenciais inválidas.")
            403 -> RegraDeNegocioException(mensagem ?: "Você não tem permissão para realizar esta ação.")
            404 -> RecursoNaoEncontradoException(mensagem ?: "Recurso não encontrado no servidor.")
            409 -> ConflitoException(mensagem ?: "Conflito com um registro já existente.")
            429 -> BloqueioTentativasException(mensagem ?: "Muitas tentativas. Aguarde alguns minutos.")
            else -> RegraDeNegocioException(mensagem ?: "Erro no servidor (HTTP ${e.code()}). Tente novamente.")
        }
    }
}
