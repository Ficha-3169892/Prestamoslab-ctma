package com.example.prestamolabctma.data

import com.example.prestamolabctma.model.*

class InMemoryPrestamoRepository : PrestamoRepository {

    private val equipos = mutableListOf(
        Equipo(1, "Multímetro Digital Fluke", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE),
        Equipo(2, "Kit de Electrónica Arduino", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(3, "Osciloscopio Portátil", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE),
        Equipo(4, "Cámara Fotográfica Sony", CategoriaEquipo.PERIFERICOS, EstadoEquipo.PRESTADO),
        Equipo(5, "Tableta Gráfica Wacom", CategoriaEquipo.COMPUTO, EstadoEquipo.DISPONIBLE)
    )

    private val solicitudes = mutableListOf<SolicitudPrestamo>()
    private var siguienteSolicitudId = 1

    override fun obtenerEquipos(): List<Equipo> = equipos.toList()

    override fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }

    override fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudes.toList()

    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? = solicitudes.find { it.id == id }

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }

        if (equipoIndex == -1) {
            return Result.failure(Exception("El equipo solicitado no existe"))
        }

        val equipo = equipos[equipoIndex]
        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(Exception("El equipo no está disponible para préstamo"))
        }

        val nuevaSolicitud = solicitud.copy(id = siguienteSolicitudId++)
        solicitudes.add(nuevaSolicitud)
        equipos[equipoIndex] = equipo.copy(estado = EstadoEquipo.RESERVADO)

        return Result.success(Unit)
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {
        val solicitudIndex = solicitudes.indexOfFirst { it.id == id }

        if (solicitudIndex == -1) {
            return Result.failure(Exception("La solicitud no existe"))
        }

        val solicitud = solicitudes[solicitudIndex]
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(Exception("Solo se pueden cancelar solicitudes en estado SOLICITADA"))
        }

        solicitudes[solicitudIndex] = solicitud.copy(estado = EstadoSolicitud.CANCELADA)

        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex != -1) {
            equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.DISPONIBLE)
        }

        return Result.success(Unit)
    }
}