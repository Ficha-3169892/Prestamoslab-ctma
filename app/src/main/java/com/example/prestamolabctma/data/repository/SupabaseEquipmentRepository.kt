package com.example.prestamolabctma.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.prestamolabctma.data.local.dao.EquipmentDao
import com.example.prestamolabctma.data.local.dao.LoanDao
import com.example.prestamolabctma.data.mappers.toDomain
import com.example.prestamolabctma.data.mappers.toDto
import com.example.prestamolabctma.data.mappers.toEntity
import com.example.prestamolabctma.data.remote.dto.EquipmentDto
import com.example.prestamolabctma.data.remote.dto.LoanDto
import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.SolicitudPrestamo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseEquipmentRepository(
    private val supabase: SupabaseClient,
    private val equipmentDao: EquipmentDao,
    private val loanDao: LoanDao,
    private val context: Context 
) : EquipmentRepository {

    override fun getEquipments(): Flow<List<Equipo>> {
        return equipmentDao.getAllEquipments()
            .map { entities -> entities.map { it.toDomain() } }
            .onStart { refreshEquipments() }
    }

    private suspend fun refreshEquipments() {
        runCatching {
            val response = supabase.postgrest.from("equipments")
                .select()
                .decodeList<EquipmentDto>()
            equipmentDao.insertEquipments(response.map { it.toEntity() })
        }.onFailure { Log.e("REPO", "Error refreshEquipments: ${it.message}") }
    }

    override suspend fun getEquipmentById(id: Int): Equipo? {
        return equipmentDao.getEquipmentById(id.toLong())?.toDomain()
    }

    override suspend fun refreshData(): Result<Unit> = runCatching {
        refreshEquipments()
        refreshLoans()
    }

    override fun getMyLoans(): Flow<List<SolicitudPrestamo>> {
        return loanDao.getAllLoans()
            .map { entities -> 
                val userId = supabase.auth.currentUserOrNull()?.id
                entities.filter { it.usuarioId == userId }.map { it.toDomain() } 
            }
            .onStart { refreshLoans() }
    }

    private suspend fun refreshLoans() {
        runCatching {
            val response = supabase.postgrest.from("loans")
                .select()
                .decodeList<LoanDto>()
            
            loanDao.refreshLoans(response.map { it.toEntity() })
        }.onFailure { Log.e("REPO", "Error refreshLoans: ${it.message}") }
    }

    override suspend fun uploadImage(uriString: String): Result<String> = runCatching {
        val uri = Uri.parse(uriString)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } 
            ?: throw Exception("No se pudo leer la imagen")
        
        val fileName = "face_${System.currentTimeMillis()}.jpg"
        val bucket = supabase.storage.from("loan-evidences")
        
        bucket.upload(fileName, bytes)
        bucket.publicUrl(fileName)
    }

    override suspend fun createLoanRequest(solicitud: SolicitudPrestamo, imageUri: String?): Result<Unit> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("Usuario no autenticado")

        var finalPhotoUrl: String? = null
        
        if (!imageUri.isNullOrBlank()) {
            uploadImage(imageUri).onSuccess { remoteUrl ->
                finalPhotoUrl = remoteUrl
            }
        }

        val profileName = supabase.auth.currentUserOrNull()?.userMetadata?.get("full_name")?.toString() 
            ?: "Prestatario"

        val borrowerNameFinal = if (solicitud.borrowerName.isNullOrBlank() || solicitud.borrowerName == "Usuario PréstamoLab") {
            profileName
        } else {
            solicitud.borrowerName
        }

        val updatedSolicitud = solicitud.copy(
            borrowerName = borrowerNameFinal,
            fotoValidacionUrl = finalPhotoUrl
        )
        val dto = updatedSolicitud.toDto(userId)

        // 1. Guardar localmente de inmediato en Room
        val localLoanEntity = dto.toEntity().copy(id = System.currentTimeMillis())
        loanDao.insertLoans(listOf(localLoanEntity))
        
        // Actualizar estado del equipo localmente
        val currentEq = equipmentDao.getEquipmentById(solicitud.equipoId.toLong())
        currentEq?.let { eq ->
            equipmentDao.insertEquipments(listOf(eq.copy(estado = com.example.prestamolabctma.model.EstadoEquipo.RESERVADO)))
        }

        // 2. Insertar solicitud en Supabase
        runCatching {
            supabase.postgrest.from("loans").insert(dto)
        }.onFailure { Log.e("REPO", "Error al insertar prestamo en Supabase: ${it.message}") }

        // 3. Actualizar estado del equipo en Supabase a RESERVADO
        runCatching {
            supabase.postgrest.from("equipments").update(buildJsonObject {
                put("status", "RESERVADO")
            }) {
                filter { eq("id", solicitud.equipoId) }
            }
        }.onFailure { Log.e("REPO", "Error al actualizar estado equipo en Supabase: ${it.message}") }

        // 4. Refrescar datos remotos
        refreshLoans()
        refreshEquipments()
    }

    override suspend fun addEquipment(equipo: Equipo): Result<Unit> = runCatching {
        val dto = EquipmentDto(
            nombre = equipo.nombre,
            descripcion = equipo.descripcion,
            estado = equipo.estado.name,
            categoria = equipo.categoria.name,
            imageUrl = equipo.imageUrl
        )
        supabase.postgrest.from("equipments").insert(dto)
        refreshEquipments()
    }

    override suspend fun updateEquipment(equipo: Equipo): Result<Unit> = runCatching {
        val dto = EquipmentDto(
            id = equipo.id.toLong(),
            nombre = equipo.nombre,
            descripcion = equipo.descripcion,
            estado = equipo.estado.name,
            categoria = equipo.categoria.name,
            imageUrl = equipo.imageUrl
        )
        // 1. Actualizar inmediatamente localmente en Room
        equipmentDao.insertEquipments(listOf(dto.toEntity()))

        // 2. Actualizar en Supabase
        runCatching {
            supabase.postgrest.from("equipments").update(dto) {
                filter { eq("id", equipo.id) }
            }
        }.onFailure { Log.e("REPO", "Error updateEquipment remoto: ${it.message}") }

        refreshEquipments()
    }

    override suspend fun deleteEquipment(id: Int): Result<Unit> = runCatching {
        // 1. Eliminar inmediatamente localmente en Room
        equipmentDao.deleteEquipmentById(id.toLong())

        // 2. Eliminar préstamos asociados en Supabase primero (evita fallo de clave foránea)
        runCatching {
            supabase.postgrest.from("loans").delete {
                filter { eq("equipment_id", id) }
            }
        }.onFailure { Log.e("REPO", "Error delete loans remoto: ${it.message}") }

        // 3. Eliminar equipo en Supabase
        runCatching {
            supabase.postgrest.from("equipments").delete {
                filter { eq("id", id) }
            }
        }.onFailure { Log.e("REPO", "Error deleteEquipment remoto: ${it.message}") }

        refreshEquipments()
    }

    override fun getAllLoansAdmin(): Flow<List<SolicitudPrestamo>> {
        return loanDao.getAllLoans()
            .map { entities -> entities.map { it.toDomain() } }
            .onStart { refreshLoans() }
    }

    override suspend fun updateLoanStatus(loanId: Int, newStatus: String): Result<Unit> = runCatching {
        supabase.postgrest.from("loans").update(buildJsonObject {
            put("status", newStatus)
        }) {
            filter { eq("id", loanId) }
        }
        
        val allLoans = loanDao.getAllLoans().first()
        val targetLoan = allLoans.find { it.id == loanId.toLong() }
        
        targetLoan?.let { loan ->
            val newEqStatus = when (newStatus) {
                "APROBADA" -> "PRESTADO"
                "DEVUELTA", "RECHAZADA", "CANCELADA" -> "DISPONIBLE"
                else -> null
            }
            
            newEqStatus?.let { status ->
                supabase.postgrest.from("equipments").update(buildJsonObject {
                    put("status", status)
                }) {
                    filter { eq("id", loan.equipoId) }
                }
            }
        }

        refreshLoans()
        refreshEquipments()
    }
}