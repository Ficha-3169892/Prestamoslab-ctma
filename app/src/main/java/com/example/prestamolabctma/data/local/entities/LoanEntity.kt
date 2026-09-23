package com.example.prestamolabctma.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prestamolabctma.model.EstadoSolicitud

@Entity(tableName = "solicitudes_prestamo")
data class LoanEntity(
    @PrimaryKey val id: Long,
    val equipoId: Long,
    val usuarioId: String,
    val ambienteDestino: String,
    val proposito: String,
    val duracionHoras: Int,
    val estado: EstadoSolicitud,
    val fotoValidacionUrl: String?,
    val borrowerName: String?,
    val createdAt: String?
)