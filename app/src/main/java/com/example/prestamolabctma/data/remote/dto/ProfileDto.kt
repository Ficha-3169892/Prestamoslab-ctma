package com.example.prestamolabctma.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val nombre: String,
    @SerialName("cedula") val cedula: String,
    @SerialName("rol") val rol: String,
    @SerialName("foto_url") val fotoUrl: String? = null
)
