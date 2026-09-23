package com.example.prestamolabctma.data.local.converters

import androidx.room.TypeConverter
import com.example.prestamolabctma.model.CategoriaEquipo
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.model.EstadoSolicitud
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromCategoria(value: CategoriaEquipo): String = value.name

    @TypeConverter
    fun toCategoria(value: String): CategoriaEquipo = CategoriaEquipo.valueOf(value)

    @TypeConverter
    fun fromEstadoEquipo(value: EstadoEquipo): String = value.name

    @TypeConverter
    fun toEstadoEquipo(value: String): EstadoEquipo = EstadoEquipo.valueOf(value)

    @TypeConverter
    fun fromEstadoSolicitud(value: EstadoSolicitud): String = value.name

    @TypeConverter
    fun toEstadoSolicitud(value: String): EstadoSolicitud = EstadoSolicitud.valueOf(value)
}
