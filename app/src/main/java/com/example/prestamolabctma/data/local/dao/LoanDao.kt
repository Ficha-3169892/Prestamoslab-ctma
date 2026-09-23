package com.example.prestamolabctma.data.local.dao

import androidx.room.*
import com.example.prestamolabctma.data.local.entities.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM solicitudes_prestamo")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Query("DELETE FROM solicitudes_prestamo")
    suspend fun deleteAll()

    @Transaction
    suspend fun refreshLoans(loans: List<LoanEntity>) {
        deleteAll()
        insertLoans(loans)
    }
}