package com.example.prestamolabctma.data

import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.model.SolicitudPrestamo

interface PrestamoRepository {
    suspend fun obtenerEquipos(): List<Equipo>
    suspend fun obtenerEquipo(id: Int): Equipo?
    suspend fun agregarEquipo(equipo: Equipo): Result<Unit>
    suspend fun actualizarEquipo(equipo: Equipo): Result<Unit>
    suspend fun eliminarEquipo(id: Int): Result<Unit>
    suspend fun actualizarEstadoEquipo(id: Int, nuevoEstado: EstadoEquipo): Result<Unit>
    
    suspend fun obtenerSolicitudes(): List<SolicitudPrestamo>
    suspend fun obtenerSolicitud(id: Int): SolicitudPrestamo?
    suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit>
    suspend fun cancelarSolicitud(id: Int): Result<Unit>
    suspend fun aprobarSolicitud(id: Int): Result<Unit>
    suspend fun rechazarSolicitud(id: Int): Result<Unit>
    suspend fun finalizarPrestamo(solicitudId: Int): Result<Unit>
}
