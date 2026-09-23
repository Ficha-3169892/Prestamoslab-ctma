package com.example.prestamolabctma.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prestamolabctma.model.CategoriaEquipo
import com.example.prestamolabctma.model.EstadoEquipo

@Entity(tableName = "equipos")
data class EquipmentEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val categoria: CategoriaEquipo,
    val estado: EstadoEquipo,
    val descripcion: String,
    val especificaciones: List<String>,
    val imageUrl: String?
)
