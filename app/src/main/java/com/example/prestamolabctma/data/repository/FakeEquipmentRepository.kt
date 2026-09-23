package com.example.prestamolabctma.data.repository

import com.example.prestamolabctma.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeEquipmentRepository : EquipmentRepository {

    private val _equipos = MutableStateFlow(
        listOf(
            Equipo(1, "Multímetro Digital Fluke", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE, "Multímetro industrial de alta precisión.", listOf("Rango 1000V", "True RMS")),
            Equipo(2, "Osciloscopio Portátil", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE, "Herramienta compacta para visualización de señales.", listOf("2 Canales", "100 MHz")),
            Equipo(3, "Kit Arduino Uno", CategoriaEquipo.ELECTRONICA, EstadoEquipo.PRESTADO, "Kit completo para prototipado.", listOf("ATmega328P", "Cable USB")),
            Equipo(4, "Cámara Sony Alpha", CategoriaEquipo.PERIFERICOS, EstadoEquipo.DISPONIBLE, "Cámara mirrorless 4K.", listOf("Sensor APS-C", "Lente 16-50mm"))
        )
    )

    private val _solicitudes = MutableStateFlow<List<SolicitudPrestamo>>(emptyList())

    override fun getEquipments(): Flow<List<Equipo>> = _equipos.asStateFlow()

    override suspend fun getEquipmentById(id: Int): Equipo? {
        delay(500)
        return _equipos.value.find { it.id == id }
    }

    override fun getMyLoans(): Flow<List<SolicitudPrestamo>> = _solicitudes.asStateFlow()

    override suspend fun createLoanRequest(
        solicitud: SolicitudPrestamo,
        imageUri: String?
    ): Result<Unit> {
        delay(1000)
        val equipo = _equipos.value.find { it.id == solicitud.equipoId }
            ?: return Result.failure(Exception("Equipo no encontrado"))

        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            return Result.failure(Exception("El equipo no está disponible"))
        }

        _equipos.value = _equipos.value.map {
            if (it.id == solicitud.equipoId) it.copy(estado = EstadoEquipo.RESERVADO) else it
        }

        val nuevaSolicitud = solicitud.copy(id = (_solicitudes.value.maxOfOrNull { it.id } ?: 0) + 1)
        _solicitudes.value = _solicitudes.value + nuevaSolicitud

        return Result.success(Unit)
    }

    override suspend fun uploadImage(uri: String): Result<String> {
        delay(500)
        return Result.success(uri)
    }

    override suspend fun addEquipment(equipo: Equipo): Result<Unit> {
        delay(500)
        val nuevoId = (_equipos.value.maxOfOrNull { it.id } ?: 0) + 1
        _equipos.value = _equipos.value + equipo.copy(id = nuevoId)
        return Result.success(Unit)
    }

    override suspend fun updateEquipment(equipo: Equipo): Result<Unit> {
        delay(500)
        _equipos.value = _equipos.value.map { if (it.id == equipo.id) equipo else it }
        println("DEBUG_REPO: Equipo actualizado. Nueva lista -> ${_equipos.value.map { it.nombre }}")
        return Result.success(Unit)
    }

    override suspend fun deleteEquipment(id: Int): Result<Unit> {
        delay(500)
        _equipos.value = _equipos.value.filter { it.id != id }
        println("DEBUG_REPO: Equipo eliminado (ID: $id). Nueva lista -> ${_equipos.value.map { it.nombre }}")
        return Result.success(Unit)
    }

    override fun getAllLoansAdmin(): Flow<List<SolicitudPrestamo>> = _solicitudes.asStateFlow()

    override suspend fun updateLoanStatus(loanId: Int, newStatus: String): Result<Unit> {
        delay(500)
        val estadoEnum = try {
            EstadoSolicitud.valueOf(newStatus.uppercase().trim())
        } catch (e: Exception) {
            EstadoSolicitud.SOLICITADA
        }
        _solicitudes.value = _solicitudes.value.map {
            if (it.id == loanId) {
                it.copy(estado = estadoEnum)
            } else it
        }
        return Result.success(Unit)
    }

    override suspend fun refreshData(): Result<Unit> = Result.success(Unit)
}