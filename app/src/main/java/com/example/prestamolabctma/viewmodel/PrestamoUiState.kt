package com.example.prestamolabctma.viewmodel

import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.SolicitudPrestamo

// Estado del formulario de solicitud y sus mensajes de error
data class FormularioSolicitudState(
    val equipoId: Int = 0,
    val ambienteDestino: String = "",
    val proposito: String = "",
    val duracionHoras: String = "1",
    val errorAmbiente: String? = null,
    val errorProposito: String? = null,
    val errorDuracion: String? = null,
    val esFormularioValido: Boolean = false
)

// Estado global de la interfaz gráfica
data class PrestamoUiState(
    val equipos: List<Equipo> = emptyList(),
    val queryBusqueda: String = "",
    val equiposFiltrados: List<Equipo> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val equipoSeleccionado: Equipo? = null,
    val formulario: FormularioSolicitudState = FormularioSolicitudState(),
    val mensajeExito: String? = null,
    val mensajeError: String? = null,
    val estaCargando: Boolean = false
)