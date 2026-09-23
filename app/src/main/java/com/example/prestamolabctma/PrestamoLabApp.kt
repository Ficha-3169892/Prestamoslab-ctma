package com.example.prestamolabctma

import android.app.Application
import com.example.prestamolabctma.data.repository.SupabaseEquipmentRepository
import com.example.prestamolabctma.data.local.DatabaseProvider
import com.example.prestamolabctma.data.remote.SupabaseProvider

class PrestamoLabApp : Application() {
    
    val database by lazy { DatabaseProvider.getDatabase(this) }
    
    val repository by lazy { 
        SupabaseEquipmentRepository(
            supabase = SupabaseProvider.client,
            equipmentDao = database.equipmentDao(),
            loanDao = database.loanDao(),
            context = this
        )
    }
}