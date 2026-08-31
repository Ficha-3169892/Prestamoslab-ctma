package com.example.prestamolabctma.data

import com.example.prestamolabctma.model.*

class InMemoryPrestamoRepository : PrestamoRepository {

    private val equipos = mutableListOf(
        Equipo(
            id = 1,
            nombre = "Multímetro Digital Fluke",
            descripcion = "Multímetro de alta precisión para medir voltaje, corriente y resistencia. Ideal para prácticas de circuitos.",
            categoria = CategoriaEquipo.MEDICION,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 2,
            nombre = "Kit de Electrónica Arduino",
            descripcion = "Contiene placa Arduino Uno, sensores, cables, LEDs y componentes básicos para prototipado.",
            categoria = CategoriaEquipo.ELECTRONICA,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 3,
            nombre = "Osciloscopio Portátil",
            descripcion = "Equipo para visualización de señales eléctricas en tiempo real. Frecuencia máxima 20MHz.",
            categoria = CategoriaEquipo.MEDICION,
            estado = EstadoEquipo.DISPONIBLE
        ),
        Equipo(
            id = 4,
            nombre = "Cámara Fotográfica Sony",
            descripcion = "Cámara Mirrorless 24MP con lente 18-55mm. Para registro de proyectos o eventos.",
            categoria = CategoriaEquipo.PERIFERICOS,
            estado = EstadoEquipo.PRESTADO
        ),
        Equipo(
            id = 5,
            nombre = "Tableta Gráfica Wacom",
            descripcion = "Tableta digitalizadora para diseño gráfico y dibujo artístico. Incluye lápiz sensible a la presión.",
            categoria = CategoriaEquipo.COMPUTO,
            estado = EstadoEquipo.DISPONIBLE
        )
    )

    private val solicitudes = mutableListOf<SolicitudPrestamo>()
    private var siguienteSolicitudId = 1

    override suspend fun obtenerEquipos(): List<Equipo> = equipos.toList()

    override suspend fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }

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
}