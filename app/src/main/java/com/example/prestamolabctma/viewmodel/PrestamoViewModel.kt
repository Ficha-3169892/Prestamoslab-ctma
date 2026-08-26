package com.example.prestamolabctma.viewmodel

import androidx.lifecycle.ViewModel
import com.example.prestamolabctma.data.InMemoryPrestamoRepository
import com.example.prestamolabctma.data.PrestamoRepository
import com.example.prestamolabctma.model.EstadoSolicitud
import com.example.prestamolabctma.model.SolicitudPrestamo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PrestamoViewModel(
    private val repository: PrestamoRepository = InMemoryPrestamoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow<PrestamoUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        val listaEquipos = repository.obtenerEquipos()
        val listaSolicitudes = repository.obtenerSolicitudes()
        _uiState.update {
            it.copy(equipos = listaEquipos, solicitudes = listaSolicitudes)
        }
    }

    fun seleccionarEquipo(equipoId: Int) {
        val equipo = repository.obtenerEquipo(equipoId)
        _uiState.update {
            it.copy(
                equipoSeleccionado = equipo,
                formulario = FormularioSolicitudState(equipoId = equipoId)
            )
        }
    }

    // Actualiza los campos del formulario y valida en tiempo real
    fun onAmbienteChanged(nuevoAmbiente: String) {
        val error = if (nuevoAmbiente.isBlank()) "El ambiente o destino es obligatorio" else null
        _uiState.update { estado ->
            val form = estado.formulario.copy(
                ambienteDestino = nuevoAmbiente,
                errorAmbiente = error
            )
            estado.copy(formulario = form.copy(esFormularioValido = validarFormularioCompleto(form)))
        }
    }

    fun onPropositoChanged(nuevoProposito: String) {
        val error = when {
            nuevoProposito.isBlank() -> "El propósito es obligatorio"
            nuevoProposito.length < 10 -> "Debe tener al menos 10 caracteres"
            nuevoProposito.length > 180 -> "No puede superar los 180 caracteres"
            else -> null
        }
        _uiState.update { estado ->
            val form = estado.formulario.copy(
                proposito = nuevoProposito,
                errorProposito = error
            )
            estado.copy(formulario = form.copy(esFormularioValido = validarFormularioCompleto(form)))
        }
    }

    fun onDuracionChanged(nuevaDuracion: String) {
        val duracionNum = nuevaDuracion.toIntOrNull()
        val error = when {
            duracionNum == null -> "Ingresa un número válido"
            duracionNum < 1 -> "La duración mínima es 1 hora"
            duracionNum > 8 -> "La duración máxima son 8 horas"
            else -> null
        }
        _uiState.update { estado ->
            val form = estado.formulario.copy(
                duracionHoras = nuevaDuracion,
                errorDuracion = error
            )
            estado.copy(formulario = form.copy(esFormularioValido = validarFormularioCompleto(form)))
        }
    }

    private fun validarFormularioCompleto(form: FormularioSolicitudState): Boolean {
        val duracionNum = form.duracionHoras.toIntOrNull()
        return form.ambienteDestino.isNotBlank() &&
                form.proposito.length in 10..180 &&
                duracionNum != null && duracionNum in 1..8
    }

    // Crear la solicitud de préstamo
    fun guardarSolicitud() {
        val form = _uiState.value.formulario
        if (!form.esFormularioValido) return

        val nuevaSolicitud = SolicitudPrestamo(
            id = 0,
            equipoId = form.equipoId,
            ambienteDestino = form.ambienteDestino.trim(),
            proposito = form.proposito.trim(),
            duracionHoras = form.duracionHoras.toInt(),
            estado = EstadoSolicitud.SOLICITADA
        )

        val resultado = repository.crearSolicitud(nuevaSolicitud)

        resultado.onSuccess {
            cargarDatos()
            _uiState.update { estado ->
                estado.copy(
                    mensajeExito = "¡Solicitud registrada correctamente!",
                    formulario = FormularioSolicitudState()
                )
            }
        }.onFailure { error ->
            _uiState.update { estado ->
                estado.copy(mensajeError = error.message ?: "Error al guardar la solicitud")
            }
        }
    }

    fun cancelarSolicitud(solicitudId: Int) {
        val resultado = repository.cancelarSolicitud(solicitudId)
        resultado.onSuccess {
            cargarDatos()
            _uiState.update { it.copy(mensajeExito = "Solicitud cancelada con éxito") }
        }.onFailure { error ->
            _uiState.update { it.copy(mensajeError = error.message ?: "Error al cancelar") }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null, mensajeError = null) }
    }
}