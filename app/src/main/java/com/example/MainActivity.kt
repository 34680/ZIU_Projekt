package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.ui.TaskViewModel
import com.example.ui.TaskViewModelFactory
import com.example.ui.screens.AddEditTaskScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TaskDetailsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local persistence (Room Database)
        val database = TaskDatabase.getDatabase(applicationContext)
        val taskDao = database.taskDao()
        val repository = TaskRepository(taskDao)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Instantiate task ViewModel with shared factory
                    val taskViewModel: TaskViewModel = viewModel(
                        factory = TaskViewModelFactory(repository)
                    )

                    // Navigation Host orchestrating transitions between the 3 views
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard"
                    ) {
                        // View 1: Dashboard Home
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = taskViewModel,
                                onNavigateToAddTask = {
                                    navController.navigate("add_edit_task/-1")
                                },
                                onNavigateToTaskDetails = { taskId ->
                                    navController.navigate("task_details/$taskId")
                                }
                            )
                        }

                        // View 2: Add / Edit Form (takes taskId argument, negative one means creation mode)
                        composable(
                            route = "add_edit_task/{taskId}",
                            arguments = listOf(
                                navArgument("taskId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getInt("taskId")
                            AddEditTaskScreen(
                                viewModel = taskViewModel,
                                taskId = taskId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // View 3: Task Details view
                        composable(
                            route = "task_details/{taskId}",
                            arguments = listOf(
                                navArgument("taskId") {
                                    type = NavType.IntType
                                }
                            )
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
                            TaskDetailsScreen(
                                viewModel = taskViewModel,
                                taskId = taskId,
                                onNavigateToEdit = { editId ->
                                    navController.navigate("add_edit_task/$editId") {
                                        // Pop the details screeen so we return smoothly to dashboard when saving
                                        popUpTo("dashboard")
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
