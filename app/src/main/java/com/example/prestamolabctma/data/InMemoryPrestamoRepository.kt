package com.example.prestamolabctma.data

import com.example.prestamolabctma.model.*

class InMemoryPrestamoRepository : PrestamoRepository {

    private val equipos = mutableListOf(
        Equipo(1, "Multímetro Digital Fluke", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE),
        Equipo(2, "Kit de Electrónica Arduino", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(3, "Osciloscopio Portátil", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE),
        Equipo(4, "Cámara Fotográfica Sony", CategoriaEquipo.PERIFERICOS, EstadoEquipo.PRESTADO),
        Equipo(5, "Tableta Gráfica Wacom", CategoriaEquipo.COMPUTO, EstadoEquipo.DISPONIBLE),
        Equipo(6, "Generador de Funciones", CategoriaEquipo.MEDICION, EstadoEquipo.NO_DISPONIBLE)
    )

    private var siguienteEquipoId = 7
    private val solicitudes = mutableListOf<SolicitudPrestamo>()
    private var siguienteSolicitudId = 1

    override suspend fun obtenerEquipos(): List<Equipo> = equipos.toList()

    override suspend fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }

    override suspend fun agregarEquipo(equipo: Equipo): Result<Unit> {
        val nuevoEquipo = equipo.copy(id = siguienteEquipoId++)
        equipos.add(nuevoEquipo)
        return Result.success(Unit)
    }

    override suspend fun eliminarEquipo(id: Int): Result<Unit> {
        val eliminado = equipos.removeIf { it.id == id }
        return if (eliminado) Result.success(Unit) else Result.failure(Exception("Equipo no encontrado"))
    }

    override suspend fun actualizarEstadoEquipo(id: Int, nuevoEstado: EstadoEquipo): Result<Unit> {
        val index = equipos.indexOfFirst { it.id == id }
        if (index != -1) {
            equipos[index] = equipos[index].copy(estado = nuevoEstado)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Equipo no encontrado"))
    }

    override suspend fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudes.toList()

    override suspend fun obtenerSolicitud(id: Int): SolicitudPrestamo? = solicitudes.find { it.id == id }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
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

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> {
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

    override suspend fun aprobarSolicitud(id: Int): Result<Unit> {
        val solicitudIndex = solicitudes.indexOfFirst { it.id == id }
        if (solicitudIndex == -1) return Result.failure(Exception("La solicitud no existe"))

        val solicitud = solicitudes[solicitudIndex]
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(Exception("Solo se pueden aprobar solicitudes en estado SOLICITADA"))
        }

        solicitudes[solicitudIndex] = solicitud.copy(estado = EstadoSolicitud.APROBADA)
        
        // Al aprobar, el equipo pasa de RESERVADO a PRESTADO
        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex != -1) {
            equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.PRESTADO)
        }

        return Result.success(Unit)
    }

    override suspend fun rechazarSolicitud(id: Int): Result<Unit> {
        val solicitudIndex = solicitudes.indexOfFirst { it.id == id }
        if (solicitudIndex == -1) return Result.failure(Exception("La solicitud no existe"))

        val solicitud = solicitudes[solicitudIndex]
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(Exception("Solo se pueden rechazar solicitudes en estado SOLICITADA"))
        }

        solicitudes[solicitudIndex] = solicitud.copy(estado = EstadoSolicitud.RECHAZADA)

        // Al rechazar, el equipo vuelve a estar DISPONIBLE
        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex != -1) {
            equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.DISPONIBLE)
        }

        return Result.success(Unit)
    }

    override suspend fun finalizarPrestamo(solicitudId: Int): Result<Unit> {
        val solicitudIndex = solicitudes.indexOfFirst { it.id == solicitudId }
        if (solicitudIndex == -1) return Result.failure(Exception("La solicitud no existe"))

        val solicitud = solicitudes[solicitudIndex]
        solicitudes[solicitudIndex] = solicitud.copy(estado = EstadoSolicitud.DEVUELTA)

        // Al devolver, el equipo vuelve a estar DISPONIBLE
        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex != -1) {
            equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.DISPONIBLE)
        }

        return Result.success(Unit)
    }
}