package com.bansagar.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SlangDao {
    @Query("SELECT * FROM slangs WHERE status = 'approved' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getLatest(limit: Int, offset: Int): List<SlangEntity>

    @Query("SELECT * FROM slangs WHERE status = 'approved' ORDER BY upvotes DESC LIMIT :limit OFFSET :offset")
    suspend fun getTop(limit: Int, offset: Int): List<SlangEntity>

    @Query("SELECT * FROM slangs WHERE status = 'approved'")
    suspend fun getAll(): List<SlangEntity>

    @Query("SELECT * FROM slangs WHERE slug = :slug LIMIT 1")
    suspend fun getBySlug(slug: String): SlangEntity?

    @Query("SELECT * FROM slangs WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SlangEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slangs: List<SlangEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slang: SlangEntity)

    @Query("SELECT COUNT(*) FROM slangs WHERE status = 'approved'")
    suspend fun countApproved(): Int
}
