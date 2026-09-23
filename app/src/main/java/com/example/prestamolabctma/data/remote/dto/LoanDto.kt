package com.example.prestamolabctma.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoanDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("equipment_id") val equipoId: Long? = null,
    @SerialName("borrower_id") val usuarioId: String? = null,
    @SerialName("ambiente_destino") val ambienteDestino: String? = "",
    @SerialName("proposito") val proposito: String? = "",
    @SerialName("duracion_horas") val duracionHoras: Int? = 1,
    @SerialName("status") val estado: String? = "SOLICITADA",
    @SerialName("face_verification_url") val fotoValidacionUrl: String? = null,
    @SerialName("borrower_name") val borrowerName: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)