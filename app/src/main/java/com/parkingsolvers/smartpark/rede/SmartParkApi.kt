package com.parkingsolvers.smartpark.rede

import com.parkingsolvers.smartpark.auth.dto.LoginDTO
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.dashboard.dto.response.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.movimentacao.dto.request.EntradaRequest
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.usuario.dto.request.AtualizarUsuarioAdminRequest
import com.parkingsolvers.smartpark.usuario.dto.request.AtualizarUsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.request.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse
import com.parkingsolvers.smartpark.visitante.dto.request.VisitanteRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SmartParkApi {

    // Autenticação
    @POST("auth/login")
    suspend fun login(@Body request: LoginDTO): TokenApiResponse

    @POST("auth/refresh")
    fun refreshSync(@Body request: RefreshApiRequest): Call<TokenApiResponse>

    @GET("auth/me")
    suspend fun me(): UsuarioSessao

    // Servidores
    @GET("servidores")
    suspend fun listarServidores(): List<ServidorResponse>

    @GET("servidores/{id}")
    suspend fun buscarServidor(@Path("id") id: Long): ServidorResponse

    @POST("servidores")
    suspend fun cadastrarServidor(@Body request: ServidorRequest): ServidorResponse

    @PUT("servidores/{id}")
    suspend fun atualizarServidor(@Path("id") id: Long, @Body request: ServidorRequest): ServidorResponse

    @PATCH("servidores/{id}/ativo")
    suspend fun alternarAtivoServidor(@Path("id") id: Long): ServidorResponse

    // Usuários
    @GET("usuarios")
    suspend fun listarUsuarios(): List<UsuarioResponse>

    @GET("usuarios/{id}")
    suspend fun buscarUsuario(@Path("id") id: Long): UsuarioResponse

    @GET("usuarios/me")
    suspend fun meusDados(): UsuarioResponse

    @PUT("usuarios/me")
    suspend fun atualizarMeusDados(@Body request: AtualizarUsuarioRequest): UsuarioResponse

    @POST("usuarios")
    suspend fun cadastrarUsuario(@Body request: UsuarioRequest): UsuarioResponse

    @PUT("usuarios/{id}")
    suspend fun atualizarUsuarioAdmin(
        @Path("id") id: Long,
        @Body request: AtualizarUsuarioAdminRequest
    ): UsuarioResponse

    @PATCH("usuarios/{id}/ativo")
    suspend fun alternarAtivoUsuario(@Path("id") id: Long): UsuarioResponse

    @PATCH("usuarios/{id}/perfil")
    suspend fun alterarPerfilUsuario(
        @Path("id") id: Long,
        @Body request: AlterarPerfilApiRequest
    ): UsuarioResponse

    // Veículos
    @GET("veiculos")
    suspend fun listarVeiculos(): List<VeiculoApiResponse>

    @GET("veiculos/servidor/{servidorId}")
    suspend fun listarVeiculosPorServidor(@Path("servidorId") servidorId: Long): List<VeiculoApiResponse>

    @GET("veiculos/buscar")
    suspend fun buscarVeiculosPorTrecho(@Query("termo") termo: String): List<VeiculoApiResponse>

    @GET("veiculos/placa/{placa}")
    suspend fun consultarPlaca(@Path("placa") placa: String): ResultadoConsultaPlacaApiResponse

    @POST("veiculos")
    suspend fun cadastrarVeiculo(@Body request: VeiculoApiRequest): VeiculoApiResponse

    @PATCH("veiculos/{id}/ativo")
    suspend fun alternarAtivoVeiculo(@Path("id") id: Long): VeiculoApiResponse

    // Movimentações
    @GET("movimentacoes")
    suspend fun listarMovimentacoes(): List<MovimentacaoApiResponse>

    @GET("movimentacoes/no-estacionamento")
    suspend fun listarNoEstacionamento(): List<MovimentacaoApiResponse>

    @POST("movimentacoes/entrada")
    suspend fun registrarEntradaMovimentacao(@Body request: EntradaRequest): MovimentacaoApiResponse

    @POST("movimentacoes/{id}/saida")
    suspend fun registrarSaidaMovimentacao(@Path("id") id: Long): MovimentacaoApiResponse

    // Visitantes
    @GET("visitantes")
    suspend fun listarVisitantes(): List<VisitanteApiResponse>

    @GET("visitantes/no-campus")
    suspend fun listarVisitantesNoCampus(): List<VisitanteApiResponse>

    @POST("visitantes")
    suspend fun registrarEntradaVisitante(@Body request: VisitanteRequest): VisitanteApiResponse

    @POST("visitantes/{id}/saida")
    suspend fun registrarSaidaVisitante(@Path("id") id: Long): VisitanteApiResponse

    // Dashboard e Configuração
    @GET("dashboard/ocupacao")
    suspend fun obterDashboard(@Query("periodo") periodo: String): DashboardOcupacaoResponse

    @GET("configuracao-estacionamento")
    suspend fun obterConfiguracao(): ConfiguracaoApiDto

    @PUT("configuracao-estacionamento")
    suspend fun atualizarConfiguracao(@Body request: ConfiguracaoApiDto): ConfiguracaoApiDto
}
