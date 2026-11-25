package com.parkingsolvers.smartpark.usuario.service

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.rede.AlterarPerfilApiRequest
import com.parkingsolvers.smartpark.rede.ApiErrorMapper
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.rede.SmartParkApi
import com.parkingsolvers.smartpark.rede.fluxoRemoto
import com.parkingsolvers.smartpark.usuario.dto.request.AtualizarUsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.request.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse
import kotlinx.coroutines.flow.Flow

class UsuarioService(
    private val api: SmartParkApi,
    private val sincronizador: SincronizadorDados
) {
    fun observarTodos(): Flow<List<UsuarioResponse>> =
        fluxoRemoto(sincronizador, emptyList()) { api.listarUsuarios() }

    suspend fun buscarPorId(id: Long): UsuarioResponse =
        ApiErrorMapper.executar { api.buscarUsuario(id) }

    suspend fun buscarProprioPerfil(): UsuarioResponse =
        ApiErrorMapper.executar { api.meusDados() }

    suspend fun cadastrar(request: UsuarioRequest): UsuarioResponse =
        ApiErrorMapper.executar { api.cadastrarUsuario(request) }
            .also { sincronizador.notificarMudanca() }

    suspend fun atualizarProprioPerfil(
        @Suppress("UNUSED_PARAMETER") usuarioId: Long,
        request: AtualizarUsuarioRequest
    ): UsuarioResponse =
        ApiErrorMapper.executar { api.atualizarMeusDados(request) }
            .also { sincronizador.notificarMudanca() }

    suspend fun alternarAtivo(
        usuarioId: Long,
        @Suppress("UNUSED_PARAMETER") usuarioLogadoId: Long?
    ): UsuarioResponse =
        ApiErrorMapper.executar { api.alternarAtivoUsuario(usuarioId) }
            .also { sincronizador.notificarMudanca() }

    suspend fun alterarPerfil(
        usuarioId: Long,
        novoPerfil: PerfilUsuario,
        @Suppress("UNUSED_PARAMETER") usuarioLogadoId: Long?,
        servidorId: Long? = null
    ): UsuarioResponse =
        ApiErrorMapper.executar {
            api.alterarPerfilUsuario(usuarioId, AlterarPerfilApiRequest(novoPerfil, servidorId))
        }.also { sincronizador.notificarMudanca() }
}
