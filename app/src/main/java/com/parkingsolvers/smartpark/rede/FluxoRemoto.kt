package com.parkingsolvers.smartpark.rede

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Emite dados buscados da API sempre que o [Flow] é coletado e sempre que
 * [SincronizadorDados.notificarMudanca] é chamado. Mantém o último valor válido
 * em caso de falha temporária de rede para não derrubar o ViewModel.
 */
fun <T> fluxoRemoto(
    sincronizador: SincronizadorDados,
    inicial: T,
    buscar: suspend () -> T
): Flow<T> = flow {
    var ultimo = inicial
    sincronizador.versao.collect {
        val resultado = runCatching { buscar() }
        if (resultado.isSuccess) {
            ultimo = resultado.getOrThrow()
        }
        emit(ultimo)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : Any> fluxoRemotoOpcional(
    gatilho: Flow<*>,
    buscar: suspend () -> T
): Flow<T> = flow {
    var ultimo: T? = null
    gatilho.collect {
        val resultado = runCatching { buscar() }
        if (resultado.isSuccess) {
            ultimo = resultado.getOrThrow()
        }
        ultimo?.let { emit(it) }
    }
}
