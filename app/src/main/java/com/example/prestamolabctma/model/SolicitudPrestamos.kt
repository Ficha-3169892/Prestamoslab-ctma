package com.example.prestamolabctma.model

data class SolicitudPrestamo(
    val id: Int,
    val equipoId: Int,
    val ambienteDestino: String,
    val proposito: String,
    val duracionHoras: Int,
    val estado: EstadoSolicitud,
    val fotoValidacionUrl: String? = null,
    val borrowerName: String? = null,
    val createdAt: String? = null
)