package com.example.medarchive.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.medarchive.R
import com.example.medarchive.data.local.DatabaseRepository
import com.example.medarchive.domain.models.Document
import com.example.medarchive.domain.models.HealthCategory
import com.example.medarchive.domain.models.HealthEntry
import com.example.medarchive.domain.models.User
import com.example.medarchive.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(
    application: Application,
    private val repository: DatabaseRepository
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

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

    private val _resetPasswordError = MutableStateFlow<String?>(null)
    val resetPasswordError: StateFlow<String?> = _resetPasswordError.asStateFlow()

    private val _resetPasswordSuccess = MutableSharedFlow<Unit>()
    val resetPasswordSuccess: SharedFlow<Unit> = _resetPasswordSuccess.asSharedFlow()

    private val _resetNewPasswordError = MutableStateFlow<String?>(null)
    val resetNewPasswordError: StateFlow<String?> = _resetNewPasswordError.asStateFlow()

    private val _resetNewPasswordSuccess = MutableStateFlow(false)
    val resetNewPasswordSuccess: StateFlow<Boolean> = _resetNewPasswordSuccess.asStateFlow()

    // Используем mutableStateListOf для эффективного точечного обновления
    private val _healthCategories = MutableStateFlow<List<HealthCategory>>(emptyList())
    val healthCategories: StateFlow<List<HealthCategory>> = _healthCategories.asStateFlow()

    private val defaultCategories = listOf(
        HealthCategory("1", "Давление", "мм рт.ст.", R.drawable.ic_doctor, Color(0xFFE1BEE7)),
        HealthCategory("2", "Сахар крови", "ммоль/л", R.drawable.ic_journal, Color(0xFFC8E6C9)),
        HealthCategory("3", "Пульс", "уд/мин", R.drawable.ic_doctor, Color(0xFFBBDEFB)),
        HealthCategory("4", "Вес", "кг", R.drawable.ic_add, Color(0xFFFFF9C4))
    )

    init {
        // При создании ViewModel пытаемся восстановить сессию
        restoreSession()
    }

    // ========== Пользователи ==========
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.verifyUser(email, password)
            if (user != null) {
                _currentUser.value = user
                sessionManager.saveUserSession(user.id, user.email)
                loadUserData(user.id)
            } else {
                _loginError.value = "Неверный email или пароль"
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _registrationError.value = null

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _registrationError.value = "Некорректный формат email"
                return@launch
            }

            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _registrationError.value = "Пользователь уже существует"
                return@launch
            }
            val user = repository.createUser(email, password, fullName)
            if (user != null) {
                _currentUser.value = user
                sessionManager.saveUserSession(user.id, user.email)
            } else {
                _registrationError.value = "Ошибка регистрации"
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _currentUser.value = null
        _documents.value = emptyList()
        _healthEntries.value = emptyList()
        _healthCategories.value = emptyList()
        _loginError.value = null
        _registrationError.value = null
    }

    fun restoreSession() {
        viewModelScope.launch {
            if (sessionManager.isLoggedIn()) {
                val userId = sessionManager.getUserId()
                val email = sessionManager.getUserEmail()
                if (userId != -1 && email != null) {
                    val user = repository.getUserByEmail(email)
                    if (user != null) {
                        _currentUser.value = user
                        loadUserData(user.id)
                    } else {
                        sessionManager.clearSession()
                    }
                } else {
                    sessionManager.clearSession()
                }
            }
        }
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

    // ========== Восстановление пароля ==========
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

    // ========== Категории здоровья ==========
    fun loadHealthCategories() {
        viewModelScope.launch {
            val userId = _currentUser.value?.id ?: return@launch
            val entries = repository.getAllEntriesList(userId)
            val categoriesWithLast = defaultCategories.map { cat ->
                val catEntries = entries.filter { it.entryType == cat.name }
                val lastValue = catEntries.maxByOrNull { it.entryDate }?.value1
                cat.copy(lastValue = lastValue)
            }
            _healthCategories.value = categoriesWithLast
        }
    }

    fun addHealthEntry(categoryId: String, value: Double, notes: String?, date: Date) {
        viewModelScope.launch {
            val userId = _currentUser.value?.id ?: return@launch
            val category = _healthCategories.value.find { it.id == categoryId } ?: return@launch
            repository.createEntry(
                userId = userId,
                entryType = category.name,
                unit = category.unit,
                value1 = value,
                notes = notes,
                entryDate = date
            )
            // Обновляем lastValue, создавая новый список
            _healthCategories.value = _healthCategories.value.map { cat ->
                if (cat.id == categoryId) cat.copy(lastValue = value) else cat
            }
        }
    }

//    fun getCategory(categoryId: String): StateFlow<HealthCategory?> {
//        return _healthCategories
//            .map { list -> list.find { it.id == categoryId } }
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = null
//            )
//    }

    fun getEntriesForCategory(categoryId: String): StateFlow<List<HealthEntry>> {
        val userId = _currentUser.value?.id
        if (userId == null) return MutableStateFlow(emptyList())
        val categoryName = _healthCategories.value.find { it.id == categoryId }?.name ?: ""  // ← .value
        return repository.getAllEntriesFlow(userId)
            .map { entries -> entries.filter { it.entryType == categoryName } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
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
        loadHealthCategories()
    }
}