package com.example.prestamolabctma.data.repository

import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.SolicitudPrestamo
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    fun getEquipments(): Flow<List<Equipo>>
    suspend fun getEquipmentById(id: Int): Equipo?
    fun getMyLoans(): Flow<List<SolicitudPrestamo>>
    suspend fun createLoanRequest(solicitud: SolicitudPrestamo, imageUri: String?): Result<Unit>
    suspend fun uploadImage(uri: String): Result<String>
    suspend fun addEquipment(equipo: Equipo): Result<Unit>
    suspend fun updateEquipment(equipo: Equipo): Result<Unit>
    suspend fun deleteEquipment(id: Int): Result<Unit>
    fun getAllLoansAdmin(): Flow<List<SolicitudPrestamo>>
    suspend fun updateLoanStatus(loanId: Int, newStatus: String): Result<Unit>
    suspend fun refreshData(): Result<Unit>
}