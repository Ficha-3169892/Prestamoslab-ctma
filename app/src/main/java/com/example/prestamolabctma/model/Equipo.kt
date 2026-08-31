package com.example.prestamolabctma.model

data class Equipo(
    val id: Int,
    val nombre: String,
    val descripcion: String = "",
    val categoria: CategoriaEquipo,
    val estado: EstadoEquipo
)