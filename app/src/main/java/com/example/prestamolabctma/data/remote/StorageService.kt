package com.example.prestamolabctma.data.remote

import io.github.jan.supabase.storage.storage
import java.util.UUID

object StorageService {
    suspend fun uploadEvidence(bytes: ByteArray, fileName: String): String {
        val uniqueName = "${UUID.randomUUID()}_$fileName"
        val bucket = SupabaseProvider.client.storage.from("loan-evidences")
        bucket.upload(uniqueName, bytes) {
            upsert = true
        }
        // Return public URL or path reference. Let's return the public URL or the filename/path.
        return bucket.publicUrl(uniqueName)
    }
}
