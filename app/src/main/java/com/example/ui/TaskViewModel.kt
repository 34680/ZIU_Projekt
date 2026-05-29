package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Subtask
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SyncUiState {
    object Idle : SyncUiState
    object Loading : SyncUiState
    data class Success(val message: String) : SyncUiState
    data class Error(val errorMessage: String) : SyncUiState
}

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // Filtering flows
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Wszystkie")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedPriority = MutableStateFlow("Wszystkie")
    val selectedPriority = _selectedPriority.asStateFlow()

    private val _completionFilter = MutableStateFlow("Aktywne") // "Aktywne", "Ukończone", "Wszystkie"
    val completionFilter = _completionFilter.asStateFlow()

    // Sync operation state
    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState = _syncState.asStateFlow()

    // Mock network failure toggle (to show user handles errors)
    private val _simulateNetworkError = MutableStateFlow(false)
    val simulateNetworkError = _simulateNetworkError.asStateFlow()

    // Combined, filtered, and sorted tasks
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        repository.allTasks,
        _searchQuery,
        _selectedCategory,
        _selectedPriority,
        _completionFilter
    ) { tasks: List<TaskEntity>, query: String, cat: String, prio: String, completion: String ->
        var result = tasks
        // 1. Search Query
        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
        }
        // 2. Category
        if (cat != "Wszystkie") {
            result = result.filter { it.category == cat }
        }
        // 3. Priority
        if (prio != "Wszystkie") {
            result = result.filter { it.priority == prio }
        }
        // 4. Completion Status
        result = when (completion) {
            "Aktywne" -> result.filter { !it.isCompleted }
            "Ukończone" -> result.filter { it.isCompleted }
            else -> result
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedPriority(priority: String) {
        _selectedPriority.value = priority
    }

    fun setCompletionFilter(filter: String) {
        _completionFilter.value = filter
    }

    fun setSimulateNetworkError(simulate: Boolean) {
        _simulateNetworkError.value = simulate
    }

    fun resetSyncState() {
        _syncState.value = SyncUiState.Idle
    }

    // Database Actions
    fun addTask(
        title: String,
        description: String,
        dueDateLong: Long,
        category: String,
        priority: String,
        subtasks: List<Subtask>
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    description = description,
                    dueDateLong = dueDateLong,
                    category = category,
                    priority = priority,
                    subtasks = subtasks,
                    synced = false
                )
            )
        }
    }

    fun updateTaskFull(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(synced = false))
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    isCompleted = !task.isCompleted,
                    synced = false
                )
            )
        }
    }

    fun toggleSubtaskCompletion(task: TaskEntity, subtaskId: String) {
        viewModelScope.launch {
            val updatedSubtasks = task.subtasks.map {
                if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            repository.updateTask(
                task.copy(
                    subtasks = updatedSubtasks,
                    synced = false
                )
            )
        }
    }

    fun addSubtaskToTask(task: TaskEntity, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val nextId = "sub_${System.currentTimeMillis()}"
            val updatedSubtasks = task.subtasks + Subtask(nextId, title, false)
            repository.updateTask(task.copy(subtasks = updatedSubtasks, synced = false))
        }
    }

    fun removeSubtaskFromTask(task: TaskEntity, subtaskId: String) {
        viewModelScope.launch {
            val updatedSubtasks = task.subtasks.filter { it.id != subtaskId }
            repository.updateTask(task.copy(subtasks = updatedSubtasks, synced = false))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Cloud / Sync Simulations
    fun importTemplates() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading
            try {
                val templates = repository.fetchRemoteTemplates(_simulateNetworkError.value)
                templates.forEach { repository.insertTask(it) }
                _syncState.value = SyncUiState.Success("Pomyślnie zaimportowano 3 szablony zadań z serwera!")
            } catch (e: Exception) {
                _syncState.value = SyncUiState.Error(e.message ?: "Nieznany błąd połączenia.")
            }
        }
    }

    fun syncBackup() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading
            try {
                repository.syncLocalTasksWithCloud(_simulateNetworkError.value)
                _syncState.value = SyncUiState.Success("Kopia zapasowa gotowa! Zadania zostały pomyślnie zsynchronizowane z chmurą.")
            } catch (e: Exception) {
                _syncState.value = SyncUiState.Error(e.message ?: "Błąd podczas synchronizacji.")
            }
        }
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
