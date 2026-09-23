package com.example.prestamolabctma.data.local.dao

import androidx.room.*
import com.example.prestamolabctma.data.local.entities.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipos")
    fun getAllEquipments(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun getEquipmentById(id: Long): EquipmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipments(equipments: List<EquipmentEntity>)

    @Query("DELETE FROM equipos WHERE id = :id")
    suspend fun deleteEquipmentById(id: Long)

    @Query("DELETE FROM equipos")
    suspend fun deleteAll()
}
