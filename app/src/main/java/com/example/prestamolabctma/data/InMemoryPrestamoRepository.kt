package com.example.prestamolabctma.data

import com.example.prestamolabctma.model.*

class InMemoryPrestamoRepository : PrestamoRepository {

    private val equipos = mutableListOf(
        Equipo(
            id = 1,
            nombre = "Multímetro Digital Fluke",
            categoria = CategoriaEquipo.MEDICION,
            estado = EstadoEquipo.DISPONIBLE,
            descripcion = "Multímetro industrial de alta precisión para mediciones eléctricas avanzadas.",
            especificaciones = listOf("Rango 1000V", "True RMS", "Pantalla retroiluminada", "Categoría IV 600V")
        ),
        Equipo(
            id = 2,
            nombre = "Kit de Electrónica Arduino",
            categoria = CategoriaEquipo.ELECTRONICA,
            estado = EstadoEquipo.DISPONIBLE,
            descripcion = "Kit completo para prototipado con placa Uno R3 y variedad de sensores.",
            especificaciones = listOf("Atmega328P", "Cable USB incluido", "Set de 30 sensores", "Protoboard")
        ),
        Equipo(
            id = 3,
            nombre = "Osciloscopio Portátil",
            categoria = CategoriaEquipo.MEDICION,
            estado = EstadoEquipo.DISPONIBLE,
            descripcion = "Herramienta compacta para visualización de señales en campo.",
            especificaciones = listOf("2 Canales", "100 MHz", "Muestreo 1GSa/s", "Batería recargable")
        ),
        Equipo(
            id = 4,
            nombre = "Cámara Fotográfica Sony",
            categoria = CategoriaEquipo.PERIFERICOS,
            estado = EstadoEquipo.PRESTADO,
            descripcion = "Cámara mirrorless para registro de evidencias y proyectos multimedia.",
            especificaciones = listOf("Sensor APS-C", "Grabación 4K", "Lente 16-50mm", "Conectividad Wi-Fi")
        ),
        Equipo(
            id = 5,
            nombre = "Tableta Gráfica Wacom",
            categoria = CategoriaEquipo.COMPUTO,
            estado = EstadoEquipo.DISPONIBLE,
            descripcion = "Superficie táctil de alta sensibilidad para diseño gráfico y retoque digital.",
            especificaciones = listOf("4096 niveles de presión", "Lápiz sin batería", "4 teclas ExpressKey", "Tamaño Small")
        ),
        Equipo(
            id = 6,
            nombre = "Generador de Funciones",
            categoria = CategoriaEquipo.MEDICION,
            estado = EstadoEquipo.NO_DISPONIBLE,
            descripcion = "Generador de señales versátil para pruebas de laboratorio.",
            especificaciones = listOf("Frecuencia hasta 25MHz", "Formas de onda integradas", "Modulación AM/FM")
        )
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

    override suspend fun actualizarEquipo(equipo: Equipo): Result<Unit> {
        val index = equipos.indexOfFirst { it.id == equipo.id }
        if (index != -1) {
            equipos[index] = equipo
            return Result.success(Unit)
        }
        return Result.failure(PrestamoError.NotFoundError("El equipo con ID ${equipo.id} no existe"))
    }

    override suspend fun eliminarEquipo(id: Int): Result<Unit> {
        val eliminado = equipos.removeIf { it.id == id }
        return if (eliminado) Result.success(Unit) 
               else Result.failure(PrestamoError.NotFoundError("No se puede eliminar: equipo no encontrado"))
    }

    override suspend fun actualizarEstadoEquipo(id: Int, nuevoEstado: EstadoEquipo): Result<Unit> {
        val index = equipos.indexOfFirst { it.id == id }
        if (index != -1) {
            equipos[index] = equipos[index].copy(estado = nuevoEstado)
            return Result.success(Unit)
        }
        return Result.failure(PrestamoError.NotFoundError("Equipo no encontrado para actualizar estado"))
    }

    override suspend fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudes.toList()

    override suspend fun obtenerSolicitud(id: Int): SolicitudPrestamo? = solicitudes.find { it.id == id }

    override suspend fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        // Simulación de validación de negocio
        if (solicitud.ambienteDestino.isBlank()) {
            return Result.failure(PrestamoError.ValidationError("El ambiente de destino no puede estar vacío"))
        }

        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }

        if (equipoIndex == -1) {
            return Result.failure(PrestamoError.NotFoundError("El equipo solicitado (ID: ${solicitud.equipoId}) no existe"))
        }

        val equipo = equipos[equipoIndex]
        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(PrestamoError.BusinessError("El equipo '${equipo.nombre}' no está disponible actualmente"))
        }

        val nuevaSolicitud = solicitud.copy(id = siguienteSolicitudId++)
        solicitudes.add(nuevaSolicitud)
        equipos[equipoIndex] = equipo.copy(estado = EstadoEquipo.RESERVADO)

        return Result.success(Unit)
    }

    override suspend fun cancelarSolicitud(id: Int): Result<Unit> {
        val solicitudIndex = solicitudes.indexOfFirst { it.id == id }

        if (solicitudIndex == -1) {
            return Result.failure(PrestamoError.NotFoundError("La solicitud con ID $id no existe"))
        }

        val solicitud = solicitudes[solicitudIndex]
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(PrestamoError.BusinessError("Solo se pueden cancelar solicitudes en estado SOLICITADA. Estado actual: ${solicitud.estado}"))
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
        if (solicitudIndex == -1) return Result.failure(PrestamoError.NotFoundError("Solicitud no encontrada"))

        val solicitud = solicitudes[solicitudIndex]
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(PrestamoError.BusinessError("La solicitud ya ha sido procesada o cancelada"))
        }

        solicitudes[solicitudIndex] = solicitud.copy(estado = EstadoSolicitud.APROBADA)
        
        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex != -1) {
            equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.PRESTADO)
        }

        return Result.success(Unit)
    }

    override suspend fun rechazarSolicitud(id: Int): Result<Unit> {
        val solicitudIndex = solicitudes.indexOfFirst { it.id == id }
        if (solicitudIndex == -1) return Result.failure(PrestamoError.NotFoundError("Solicitud no encontrada"))

        val solicitud = solicitudes[solicitudIndex]
        if (solicitud.estado != EstadoSolicitud.SOLICITADA) {
            return Result.failure(PrestamoError.BusinessError("No se puede rechazar una solicitud ya procesada"))
        }

        solicitudes[solicitudIndex] = solicitud.copy(estado = EstadoSolicitud.RECHAZADA)

        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex != -1) {
            equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.DISPONIBLE)
        }

        return Result.success(Unit)
    }

    override suspend fun finalizarPrestamo(solicitudId: Int): Result<Unit> {
        val solicitudIndex = solicitudes.indexOfFirst { it.id == solicitudId }
        if (solicitudIndex == -1) return Result.failure(PrestamoError.NotFoundError("Solicitud no encontrada"))

        val solicitud = solicitudes[solicitudIndex]
        if (solicitud.estado != EstadoSolicitud.APROBADA) {
            return Result.failure(PrestamoError.BusinessError("Solo se pueden finalizar préstamos aprobados"))
        }

        solicitudes[solicitudIndex] = solicitud.copy(estado = EstadoSolicitud.DEVUELTA)

        val equipoIndex = equipos.indexOfFirst { it.id == solicitud.equipoId }
        if (equipoIndex != -1) {
            equipos[equipoIndex] = equipos[equipoIndex].copy(estado = EstadoEquipo.DISPONIBLE)
        }

        return Result.success(Unit)
    }
}
