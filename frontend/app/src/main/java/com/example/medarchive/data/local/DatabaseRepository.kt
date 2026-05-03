package com.example.medarchive.data.local

import android.content.Context
import android.util.Log
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.medarchive.data.local.entity.*
import com.example.medarchive.domain.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*

class DatabaseRepository(context: Context) {
    private val db = MedArchiveDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val documentDao = db.documentDao()
    private val entryDao = db.healthEntryDao()

    // ==================== Пользователи ====================
    suspend fun verifyUser(email: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            val userEntity = userDao.getUserByEmail(email) ?: return@withContext null
            val result = BCrypt.verifyer().verify(password.toCharArray(), userEntity.passwordHash)
            if (result.verified) {
                userEntity.toDomainModel()
            } else {
                null
            }
        }
    }

    suspend fun createUser(email: String, password: String, fullName: String): User? {
        return withContext(Dispatchers.IO) {
            val hashPwd = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            val now = Date()
            val userEntity = UserEntity(
                email = email,
                passwordHash = hashPwd,
                fullName = fullName,
                createdAt = now,
                updatedAt = now
            )
            try {
                userDao.insertUser(userEntity)
                userDao.getUserByEmail(email)?.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByEmail(email)?.toDomainModel()
        }
    }

    suspend fun updateUser(email: String, password: String?, fullName: String?): User? {
        return withContext(Dispatchers.IO) {
            val existing = userDao.getUserByEmail(email)
            if (existing == null) {
                Log.e("DatabaseRepository", "User not found: $email")
                return@withContext null
            }
            var updated = existing
            if (password != null) {
                try {
                    val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                    updated = updated.copy(passwordHash = hash, updatedAt = Date())
                } catch (e: Exception) {
                    Log.e("DatabaseRepository", "Bcrypt error", e)
                    return@withContext null
                }
            }
            if (fullName != null) {
                updated = updated.copy(fullName = fullName, updatedAt = Date())
            }
            userDao.updateUser(updated)
            updated.toDomainModel()
        }
    }

    suspend fun deleteUser(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            userDao.softDeleteUser(email, Date())
            true
        }
    }

    fun getUserFlow(userId: Int): Flow<User?> {
        return userDao.getUserFlow(userId).map { it?.toDomainModel() }
    }

    // ==================== Документы ====================

    suspend fun createDocument(
        userId: Int,
        imagePath: String?,
        title: String? = null,
        type: String? = null,
        text: String? = null
    ): Document? {
        return withContext(Dispatchers.IO) {
            val localId = UUID.randomUUID().toString()
            val now = Date()
            val doc = DocumentEntity(
                localId = localId,
                userId = userId,
                title = title,
                documentType = type,
                content = text,
                imagePath = imagePath,
                addImagePath = imagePath != null,
                createdAt = now,
                updatedAt = now
            )
            try {
                documentDao.insertDocument(doc)
                documentDao.getDocument(userId, localId)?.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }
    }



    suspend fun getDocument(userId: Int, localId: String): Document? {
        return withContext(Dispatchers.IO) {
            documentDao.getDocument(userId, localId)?.toDomainModel()
        }
    }

    suspend fun getRowDocuments(): List<Document> {
        return withContext(Dispatchers.IO) {
            documentDao.getRowDocuments().map { it.toDomainModel() }
        }
    }

    suspend fun updateDocument(
        userId: Int,
        localId: String,
        title: String?,
        documentType: String?,
        text: String?
    ): Document? {
        return withContext(Dispatchers.IO) {
            val existing = documentDao.getDocument(userId, localId) ?: return@withContext null
            var updated = existing
            title?.let { updated = updated.copy(title = it) }
            documentType?.let { updated = updated.copy(documentType = it) }
            text?.let { updated = updated.copy(content = it) }
            updated = updated.copy(updatedAt = Date())
            documentDao.updateDocument(updated)
            updated.toDomainModel()
        }
    }

    suspend fun deleteDocument(userId: Int, localId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val now = Date()
            documentDao.softDeleteDocument(userId, localId, now, now)
            true
        }
    }

    fun getAllDocumentsFlow(userId: Int): Flow<List<Document>> {
        return documentDao.getAllDocumentsFlow(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    // ==================== Health Entries ====================

    suspend fun createEntry(
        userId: Int,
        entryType: String,
        unit: String?,
        value1: Double?,
        value2: Double? = null,
        value3: Double? = null,
        notes: String? = null,
        entryDate: Date
    ): HealthEntry? {
        return withContext(Dispatchers.IO) {
            if (entryDate.after(Date())) {
                // дата не может быть в будущем (опционально)
                return@withContext null
            }
            val localId = UUID.randomUUID().toString()
            val now = Date()
            val entry = HealthEntryEntity(
                localId = localId,
                userId = userId,
                entryType = entryType,
                value1 = value1,
                value2 = value2,
                value3 = value3,
                unit = unit,
                notes = notes,
                entryDate = entryDate,
                createdAt = now,
                updatedAt = now
            )
            try {
                entryDao.insertEntry(entry)
                entryDao.getEntry(userId, localId)?.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getEntry(userId: Int, localId: String): HealthEntry? {
        return withContext(Dispatchers.IO) {
            entryDao.getEntry(userId, localId)?.toDomainModel()
        }
    }

    suspend fun updateEntry(
        userId: Int,
        localId: String,
        entryType: String?,
        value1: Double?,
        value2: Double?,
        value3: Double?,
        unit: String?,
        notes: String?,
        entryDate: Date?
    ): HealthEntry? {
        return withContext(Dispatchers.IO) {
            val existing = entryDao.getEntry(userId, localId) ?: return@withContext null
            var updated = existing
            entryType?.let { updated = updated.copy(entryType = it) }
            value1?.let { updated = updated.copy(value1 = it) }
            value2?.let { updated = updated.copy(value2 = it) }
            value3?.let { updated = updated.copy(value3 = it) }
            unit?.let { updated = updated.copy(unit = it) }
            notes?.let { updated = updated.copy(notes = it) }
            entryDate?.let {
                if (it.after(Date())) return@withContext null
                updated = updated.copy(entryDate = it)
            }
            updated = updated.copy(updatedAt = Date())
            entryDao.updateEntry(updated)
            updated.toDomainModel()
        }
    }

    suspend fun deleteEntry(userId: Int, localId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val now = Date()
            entryDao.softDeleteEntry(userId, localId, now, now)
            true
        }
    }

    suspend fun getAllEntriesList(userId: Int): List<HealthEntry> {
        return withContext(Dispatchers.IO) {
            entryDao.getAllEntries(userId).map { it.toDomainModel() }
        }
    }

    fun getAllEntriesFlow(userId: Int): Flow<List<HealthEntry>> {
        return entryDao.getAllEntriesFlow(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }
}

// Мапперы расширения
private fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        email = email,
        passwordHash = passwordHash,
        fullName = fullName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive
    )
}

private fun DocumentEntity.toDomainModel(): Document {
    return Document(
        localId = localId,
        userId = userId,
        title = title,
        documentType = documentType,
        content = content,
        imagePath = imagePath,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}

private fun HealthEntryEntity.toDomainModel(): HealthEntry {
    return HealthEntry(
        localId = localId,
        userId = userId,
        entryType = entryType,
        value1 = value1,
        value2 = value2,
        value3 = value3,
        unit = unit,
        notes = notes,
        entryDate = entryDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}