package com.example.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class TaskRepository(private val taskDao: TaskDao) {

    // Local DB exposure
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Int): TaskEntity? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        taskDao.deleteTaskById(id)
    }

    suspend fun clearAllTasks() {
        taskDao.clearAllTasks()
    }

    /**
     * API integration simulation (GET request analogy).
     * Simulates downloading template tasks from a corporate/family shared task service.
     */
    suspend fun fetchRemoteTemplates(simulateFailure: Boolean = false): List<TaskEntity> {
        delay(1500) // Simulate network latency

        if (simulateFailure) {
            throw IOException("Nie udało się pobrać szablonów zadań: Błąd serwisu chmurowego (kod 503).")
        }

        // Return mock remote list
        val now = System.currentTimeMillis()
        return listOf(
            TaskEntity(
                title = "Przykładowe: Przygotować raport kwartalny",
                description = "Należy podsumować wyniki finansowe z Q1 i przesłać do zarządu.",
                dueDateLong = now + 86400000 * 2, // 2 days from now
                category = "Praca",
                priority = "WYSOKI",
                subtasks = listOf(
                    Subtask("s1", "Zebrać dane od działu sprzedaży", true),
                    Subtask("s2", "Zredagować dokument PDF", false),
                    Subtask("s3", "Wysłać e-mail do CEO", false)
                ),
                synced = true
            ),
            TaskEntity(
                title = "Przykładowe: Powtórka do kolokwium z UX/UI",
                description = "Przejrzeć 10 heurystyk Nielsena oraz zasady projektowania zorientowanego na użytkownika (UCD).",
                dueDateLong = now + 86400000 * 4,
                category = "Nauka",
                priority = "WYSOKI",
                subtasks = listOf(
                    Subtask("s4", "Przeczytać notatki z wykładu", true),
                    Subtask("s5", "Przeanalizować błędy WCAG", false)
                ),
                synced = true
            ),
            TaskEntity(
                title = "Przykładowe: Zakupy spożywcze",
                description = "Składniki na pizzę neapolitańską.",
                dueDateLong = now + 86400000,
                category = "Zakupy",
                priority = "NISKI",
                subtasks = listOf(
                    Subtask("s6", "Mąka Tipo 00", false),
                    Subtask("s7", "Drożdże instant", false),
                    Subtask("s8", "Pomidory San Marzano", false)
                ),
                synced = true
            )
        )
    }

    /**
     * API integration simulation (POST/PUT request analogy).
     * Gathers all local tasks, simulates pushing them up as a backup.
     */
    suspend fun syncLocalTasksWithCloud(simulateFailure: Boolean = false) {
        delay(2000) // Simulate network routing and database storage latency

        if (simulateFailure) {
            throw IOException("Połączenie przerwane: Serwer kopii zapasowej nie odpowiada (Limit czasu).")
        }

        // Get local unsynced tasks
        val unsynced = taskDao.getUnsyncedTasks()
        if (unsynced.isNotEmpty()) {
            val ids = unsynced.map { it.id }
            taskDao.markTasksAsSynced(ids)
        }
    }
}
