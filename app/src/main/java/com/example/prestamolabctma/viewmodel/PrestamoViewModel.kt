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
                    equiposFiltrados = listaEquipos,
                    solicitudes = listaSolicitudes,
                    estaCargando = false
                )
            }
        }
    }

    fun onBusquedaChanged(query: String) {
        _uiState.update { estado ->
            val filtrados = if (query.isEmpty()) {
                estado.equipos
            } else {
                estado.equipos.filter { 
                    it.nombre.contains(query, ignoreCase = true) || 
                    it.categoria.name.contains(query, ignoreCase = true)
                }
            }
            estado.copy(queryBusqueda = query, equiposFiltrados = filtrados)
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
            val resultado = runCatching { repository.crearSolicitud(nuevaSolicitud) }
                .getOrElse { Result.failure(it) }
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
                    estado.copy(mensajeError = mapErrorToMessage(error))
                }
            }
        }
    }

    fun cancelarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = runCatching { repository.cancelarSolicitud(solicitudId) }
                .getOrElse { Result.failure(it) }
            _uiState.update { it.copy(estaCargando = false) }

            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Solicitud cancelada con éxito") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = mapErrorToMessage(error)) }
            }
        }
    }

    fun aprobarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = runCatching { repository.aprobarSolicitud(solicitudId) }
                .getOrElse { Result.failure(it) }
            _uiState.update { it.copy(estaCargando = false) }

            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Solicitud aprobada") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = mapErrorToMessage(error)) }
            }
        }
    }

    fun rechazarSolicitud(solicitudId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = runCatching { repository.rechazarSolicitud(solicitudId) }
                .getOrElse { Result.failure(it) }
            _uiState.update { it.copy(estaCargando = false) }

            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Solicitud rechazada") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = mapErrorToMessage(error)) }
            }
        }
    }

    fun finalizarPrestamo(solicitudId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val resultado = runCatching { repository.finalizarPrestamo(solicitudId) }
                .getOrElse { Result.failure(it) }
            _uiState.update { it.copy(estaCargando = false) }

            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Equipo devuelto correctamente") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = mapErrorToMessage(error)) }
            }
        }
    }

    private fun mapErrorToMessage(error: Throwable): String {
        return when (error) {
            is com.example.prestamolabctma.data.PrestamoError.ValidationError -> "Validación: ${error.message}"
            is com.example.prestamolabctma.data.PrestamoError.NotFoundError -> "No encontrado: ${error.message}"
            is com.example.prestamolabctma.data.PrestamoError.BusinessError -> "Error de negocio: ${error.message}"
            is com.example.prestamolabctma.data.PrestamoError.ServerError -> "Servidor: ${error.message}"
            else -> error.message ?: "Ocurrió un error inesperado"
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null, mensajeError = null) }
    }

    // --- CRUD EQUIPOS (feat/Ana) ---

    fun onNombreEquipoChanged(nuevoNombre: String) {
        _uiState.update { estado ->
            val error = if (nuevoNombre.isBlank()) "El nombre es obligatorio" else null
            val form = estado.formularioEquipo.copy(nombre = nuevoNombre, errorNombre = error)
            estado.copy(formularioEquipo = form.copy(esValido = nuevoNombre.isNotBlank()))
        }
    }

    fun onDescripcionEquipoChanged(nuevaDesc: String) {
        _uiState.update { estado ->
            estado.copy(formularioEquipo = estado.formularioEquipo.copy(descripcion = nuevaDesc))
        }
    }

    fun onCategoriaEquipoChanged(nuevaCat: CategoriaEquipo) {
        _uiState.update { estado ->
            estado.copy(formularioEquipo = estado.formularioEquipo.copy(categoria = nuevaCat))
        }
    }

    fun prepararNuevoEquipo() {
        _uiState.update { it.copy(formularioEquipo = FormularioEquipoState()) }
    }

    fun prepararEditarEquipo(equipo: Equipo) {
        _uiState.update { 
            it.copy(
                formularioEquipo = FormularioEquipoState(
                    id = equipo.id,
                    nombre = equipo.nombre,
                    descripcion = equipo.descripcion,
                    categoria = equipo.categoria,
                    esValido = true
                )
            )
        }
    }

    fun guardarEquipo() {
        val form = _uiState.value.formularioEquipo
        if (!form.esValido) return

        val equipo = Equipo(
            id = form.id,
            nombre = form.nombre,
            descripcion = form.descripcion,
            categoria = form.categoria,
            estado = if (form.id == 0) EstadoEquipo.DISPONIBLE 
                     else _uiState.value.equipos.find { it.id == form.id }?.estado ?: EstadoEquipo.DISPONIBLE
        )

        viewModelScope.launch {
            val resultado = runCatching {
                if (equipo.id == 0) repository.agregarEquipo(equipo)
                else repository.actualizarEquipo(equipo)
            }.getOrElse { Result.failure(it) }
            
            resultado.onSuccess {
                cargarDatos()
                _uiState.update { it.copy(mensajeExito = "Equipo guardado correctamente") }
            }.onFailure { error ->
                _uiState.update { it.copy(mensajeError = mapErrorToMessage(error)) }
            }
        }
    }

    fun eliminarEquipo(id: Int) {
        viewModelScope.launch {
            runCatching { repository.eliminarEquipo(id) }
                .getOrElse { Result.failure(it) }
                .onSuccess {
                    cargarDatos()
                    _uiState.update { it.copy(mensajeExito = "Equipo eliminado") }
                }.onFailure { error ->
                    _uiState.update { it.copy(mensajeError = mapErrorToMessage(error)) }
                }
        }
    }

    // --- Soporte para otras funciones de main si existen ---
    fun onQueryBusquedaChanged(nuevaQuery: String) {
        onBusquedaChanged(nuevaQuery)
    }

    fun onCategoriaSelected(categoria: CategoriaEquipo?) {
        _uiState.update { it.copy(categoriaSeleccionada = categoria) }
    }

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
}
