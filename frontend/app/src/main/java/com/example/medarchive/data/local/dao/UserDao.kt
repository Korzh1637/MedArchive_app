package com.example.medarchive.data.local.dao

import androidx.room.*
import com.example.medarchive.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isActive = 0, updatedAt = :updatedAt WHERE email = :email")
    suspend fun softDeleteUser(email: String, updatedAt: java.util.Date)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: Int): Flow<UserEntity?>
}