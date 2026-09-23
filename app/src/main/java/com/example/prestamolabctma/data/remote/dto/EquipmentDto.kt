package com.example.prestamolabctma.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EquipmentDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val nombre: String,
    @SerialName("description") val descripcion: String? = null,
    @SerialName("status") val estado: String = "DISPONIBLE",
    val categoria: String = "ELECTRONICA",
    val especificaciones: List<String> = emptyList(),
    val imageUrl: String? = null
)