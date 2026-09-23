package com.example.prestamolabctma.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.prestamolabctma.data.local.converters.Converters
import com.example.prestamolabctma.data.local.dao.EquipmentDao
import com.example.prestamolabctma.data.local.dao.LoanDao
import com.example.prestamolabctma.data.local.entities.EquipmentEntity
import com.example.prestamolabctma.data.local.entities.LoanEntity

@Database(
    entities = [EquipmentEntity::class, LoanEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    abstract fun loanDao(): LoanDao
}