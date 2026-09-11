package com.example.prestamolabctma.model

data class Equipo(
    val id: Int,
    val nombre: String,
    val categoria: CategoriaEquipo,
    val estado: EstadoEquipo,
    val descripcion: String = "",
    val especificaciones: List<String> = emptyList(),
    val imageUrl: String? = null
)