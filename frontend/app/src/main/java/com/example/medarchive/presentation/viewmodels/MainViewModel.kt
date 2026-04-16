package com.example.medarchive.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.medarchive.data.local.DatabaseRepository
import com.example.medarchive.domain.models.Document
import com.example.medarchive.domain.models.HealthEntry
import com.example.medarchive.domain.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(
    application: Application,
    private val repository: DatabaseRepository
) : AndroidViewModel(application) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    private val _healthEntries = MutableStateFlow<List<HealthEntry>>(emptyList())
    val healthEntries: StateFlow<List<HealthEntry>> = _healthEntries.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError.asStateFlow()

    // ========== Пользователи ==========
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.verifyUser(email, password)
            if (user != null) {
                _currentUser.value = user
                loadUserData(user.id)
            } else {
                _loginError.value = "Неверный email или пароль"
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _registrationError.value = null
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _registrationError.value = "Пользователь уже существует"
                return@launch
            }
            val user = repository.createUser(email, password, fullName)
            if (user != null) {
                _currentUser.value = user
            } else {
                _registrationError.value = "Ошибка регистрации"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _documents.value = emptyList()
        _healthEntries.value = emptyList()
        _loginError.value = null
        _registrationError.value = null
    }

    // ========== Документы ==========
    fun createDocument(imagePath: String?, title: String?, type: String?, text: String?) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.createDocument(userId, imagePath, title, type, text)
            loadDocuments(userId)
        }
    }

    fun loadDocuments(userId: Int) {
        viewModelScope.launch {
            repository.getAllDocumentsFlow(userId).collect { docs ->
                _documents.value = docs
            }
        }
    }

    fun deleteDocument(localId: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.deleteDocument(userId, localId)
            loadDocuments(userId)
        }
    }


    private val _resetPasswordError = MutableStateFlow<String?>(null)
    val resetPasswordError: StateFlow<String?> = _resetPasswordError.asStateFlow()

    private val _resetPasswordSuccess = MutableSharedFlow<Unit>()
    val resetPasswordSuccess: SharedFlow<Unit> = _resetPasswordSuccess.asSharedFlow()

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _resetPasswordError.value = null
            val user = repository.getUserByEmail(email)
            if (user != null) {
                _resetPasswordSuccess.emit(Unit)
            } else {
                _resetPasswordError.value = "Пользователь с таким email не найден"
            }
        }
    }

    private val _resetNewPasswordError = MutableStateFlow<String?>(null)
    val resetNewPasswordError: StateFlow<String?> = _resetNewPasswordError.asStateFlow()

    private val _resetNewPasswordSuccess = MutableStateFlow(false)
    val resetNewPasswordSuccess: StateFlow<Boolean> = _resetNewPasswordSuccess.asStateFlow()

    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            Log.d("ResetPassword", "Attempting to update password for: $email")
            _resetNewPasswordError.value = null
            _resetNewPasswordSuccess.value = false
            val updated = repository.updateUser(email, newPassword, null)
            if (updated != null) {
                Log.d("ResetPassword", "Password updated successfully")
                _resetNewPasswordSuccess.value = true
            } else {
                Log.e("ResetPassword", "Update returned null")
                _resetNewPasswordError.value = "Не удалось обновить пароль"
            }
        }
    }

    // ========== Health Entries ==========
    fun createHealthEntry(
        entryType: String,
        unit: String?,
        value1: Double?,
        value2: Double?,
        value3: Double?,
        notes: String?,
        entryDate: Date
    ) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.createEntry(userId, entryType, unit, value1, value2, value3, notes, entryDate)
            loadHealthEntries(userId)
        }
    }

    fun loadHealthEntries(userId: Int) {
        viewModelScope.launch {
            repository.getAllEntriesFlow(userId).collect { entries ->
                _healthEntries.value = entries
            }
        }
    }

    // ========== Вспомогательные ==========
    private fun loadUserData(userId: Int) {
        loadDocuments(userId)
        loadHealthEntries(userId)
    }
}