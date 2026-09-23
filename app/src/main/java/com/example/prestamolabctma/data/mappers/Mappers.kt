package com.example.prestamolabctma.data.mappers

import com.example.prestamolabctma.data.local.entities.EquipmentEntity
import com.example.prestamolabctma.data.local.entities.LoanEntity
import com.example.prestamolabctma.data.remote.dto.EquipmentDto
import com.example.prestamolabctma.data.remote.dto.LoanDto
import com.example.prestamolabctma.model.*

// DTO -> Entity (Convertir de la red hacia almacenamiento Room local)
fun EquipmentDto.toEntity() = EquipmentEntity(
    id = id ?: 0L,
    nombre = nombre,
    categoria = try { CategoriaEquipo.valueOf(categoria.uppercase().trim()) } catch (e: Exception) { CategoriaEquipo.ELECTRONICA },
    estado = try { EstadoEquipo.valueOf(estado.uppercase().trim()) } catch (e: Exception) { EstadoEquipo.DISPONIBLE },
    descripcion = descripcion ?: "",
    especificaciones = especificaciones,
    imageUrl = imageUrl
)

fun LoanDto.toEntity() = LoanEntity(
    id = id ?: 0L,
    equipoId = equipoId ?: 0L,
    usuarioId = usuarioId ?: "",
    ambienteDestino = ambienteDestino ?: "",
    proposito = proposito ?: "",
    duracionHoras = duracionHoras ?: 1,
    estado = try { EstadoSolicitud.valueOf((estado ?: "SOLICITADA").uppercase().trim()) } catch (e: Exception) { EstadoSolicitud.SOLICITADA },
    fotoValidacionUrl = fotoValidacionUrl,
    borrowerName = borrowerName,
    createdAt = createdAt
)

// Entity -> Domain (Convertir de la base de datos Room a la capa de UI)
fun EquipmentEntity.toDomain() = Equipo(
    id = id.toInt(),
    nombre = nombre,
    categoria = categoria,
    estado = estado,
    descripcion = descripcion,
    especificaciones = especificaciones,
    imageUrl = imageUrl
)

fun LoanEntity.toDomain() = SolicitudPrestamo(
    id = id.toInt(),
    equipoId = equipoId.toInt(),
    ambienteDestino = ambienteDestino,
    proposito = proposito,
    duracionHoras = duracionHoras,
    estado = estado,
    fotoValidacionUrl = fotoValidacionUrl,
    borrowerName = borrowerName,
    createdAt = createdAt
)

// Domain -> DTO (Convertir del formulario de UI hacia Supabase)
fun SolicitudPrestamo.toDto(userId: String) = LoanDto(
    id = if (id == 0) null else id.toLong(),
    equipoId = equipoId.toLong(),
    usuarioId = userId,
    ambienteDestino = ambienteDestino,
    proposito = proposito,
    duracionHoras = duracionHoras,
    estado = estado.name,
    fotoValidacionUrl = fotoValidacionUrl,
    borrowerName = borrowerName,
    createdAt = createdAt
)