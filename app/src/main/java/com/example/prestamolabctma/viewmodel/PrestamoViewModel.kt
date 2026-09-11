package com.example.prestamolabctma.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamolabctma.data.InMemoryPrestamoRepository
import com.example.prestamolabctma.data.PrestamoRepository
import com.example.prestamolabctma.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrestamoViewModel(
    private val repository: PrestamoRepository = InMemoryPrestamoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow<PrestamoUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val listaEquipos = repository.obtenerEquipos()
            val listaSolicitudes = repository.obtenerSolicitudes()
            _uiState.update {
                it.copy(
                    equipos = listaEquipos,
                    solicitudes = listaSolicitudes,
                    estaCargando = false
                )
            }
        }
    }

    fun seleccionarEquipo(equipoId: Int) {
        viewModelScope.launch {
            val equipo = repository.obtenerEquipo(equipoId)
            val error = if (equipoId == 0) "Debe seleccionar un equipo" else null
            _uiState.update { estado ->
                val form = FormularioSolicitudState(equipoId = equipoId, errorEquipo = error)
                estado.copy(
                    equipoSeleccionado = equipo,
                    formulario = form.copy(esFormularioValido = validarFormularioCompleto(form)),
                    mensajeError = null
                )
            }
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
            nuevaDuracion.isBlank() -> "La duración es obligatoria"
            duracionNum == null -> "Ingresa un número válido"
            duracionNum <= 0 -> "La duración debe ser mayor a 0"
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
        return form.equipoId != 0 &&
                form.ambienteDestino.isNotBlank() &&
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

        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = repository.crearSolicitud(nuevaSolicitud)
            _uiState.update { it.copy(estaCargando = false) }

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
    }

    fun cancelarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = repository.cancelarSolicitud(solicitudId)
            _uiState.update { it.copy(estaCargando = false) }

            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Solicitud cancelada con éxito") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message ?: "Error al cancelar") }
            }
        }
    }

    fun aprobarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = repository.aprobarSolicitud(solicitudId)
            _uiState.update { it.copy(estaCargando = false) }

            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Solicitud aprobada") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message ?: "Error al aprobar") }
            }
        }
    }

    fun rechazarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = repository.rechazarSolicitud(solicitudId)
            _uiState.update { it.copy(estaCargando = false) }

            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Solicitud rechazada") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message ?: "Error al rechazar") }
            }
        }
    }

    fun finalizarPrestamo(solicitudId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = repository.finalizarPrestamo(solicitudId)
            _uiState.update { it.copy(estaCargando = false) }

            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Equipo devuelto correctamente") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = error.message ?: "Error al devolver") }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null, mensajeError = null) }
    }

    fun onQueryBusquedaChanged(nuevaQuery: String) {
        _uiState.update { it.copy(queryBusqueda = nuevaQuery) }
    }

    // --- Nuevas funcionalidades para Equipos ---

    fun toggleEstadoEquipo(equipoId: Int) {
        val equipo = _uiState.value.equipos.find { it.id == equipoId } ?: return
        val nuevoEstado = if (equipo.estado == EstadoEquipo.DISPONIBLE) {
            EstadoEquipo.PRESTADO
        } else {
            EstadoEquipo.DISPONIBLE
        }

        viewModelScope.launch {
            repository.actualizarEstadoEquipo(equipoId, nuevoEstado).onSuccess {
                cargarDatos()
            }
        }
    }

    fun mostrarDialogoNuevoEquipo(mostrar: Boolean) {
        _uiState.update { it.copy(formularioEquipo = it.formularioEquipo.copy(mostrarDialogo = mostrar)) }
    }

    fun onNombreEquipoChanged(nombre: String) {
        _uiState.update { it.copy(formularioEquipo = it.formularioEquipo.copy(nombre = nombre)) }
    }

    fun onCategoriaEquipoChanged(categoria: CategoriaEquipo) {
        _uiState.update { it.copy(formularioEquipo = it.formularioEquipo.copy(categoria = categoria)) }
    }

    fun agregarEquipo() {
        val nombre = _uiState.value.formularioEquipo.nombre
        val categoria = _uiState.value.formularioEquipo.categoria

        if (nombre.isBlank()) return

        val nuevoEquipo = Equipo(
            id = 0,
            nombre = nombre,
            categoria = categoria,
            estado = EstadoEquipo.DISPONIBLE
        )

        viewModelScope.launch {
            repository.agregarEquipo(nuevoEquipo).onSuccess {
                cargarDatos()
                mostrarDialogoNuevoEquipo(false)
                _uiState.update { it.copy(formularioEquipo = FormularioEquipoState()) }
            }
        }
    }

    fun eliminarEquipo(equipoId: Int) {
        viewModelScope.launch {
            repository.eliminarEquipo(equipoId).onSuccess {
                cargarDatos()
            }
        }
    }
}