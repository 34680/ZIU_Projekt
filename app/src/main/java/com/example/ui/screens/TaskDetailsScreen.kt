package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.ui.TaskViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    viewModel: TaskViewModel,
    taskId: Int,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val task = remember(taskId, tasks) { tasks.find { it.id == taskId } }

    var newSubtaskTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły Zadania", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    if (task != null) {
                        IconButton(
                            onClick = { onNavigateToEdit(task.id) },
                            modifier = Modifier
                                .semantics { contentDescription = "Edytuj to zadanie" }
                                .testTag("edit_task_top_btn")
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = {
                                viewModel.deleteTask(task)
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .semantics { contentDescription = "Usuń to zadanie" }
                                .testTag("delete_task_top_btn")
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = PriorityHigh)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            if (task == null) {
                // If the task was deleted or not found
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Error, contentDescription = "Błąd", modifier = Modifier.size(64.dp), tint = PriorityHigh)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Zadanie nie zostało odnalezione", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Powrót")
                    }
                }
            } else {
                val categoryColor = when (task.category) {
                    "Praca" -> CategoryPraca
                    "Osobiste" -> CategoryOsobiste
                    "Nauka" -> CategoryNauka
                    "Zakupy" -> CategoryZakupy
                    else -> CategoryInne
                }

                val priorityColor = when (task.priority) {
                    "WYSOKI" -> PriorityHigh
                    "ŚREDNI" -> PriorityMedium
                    else -> PriorityLow
                }

                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val formattedDate = sdf.format(Date(task.dueDateLong))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Task Card Info Header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Top Row: Status, category, synced
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Category Bubble
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(categoryColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(task.category, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = categoryColor)
                                    }

                                    // Priority Bubble
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(priorityColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(task.priority, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = priorityColor)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (task.synced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                        contentDescription = "Sync state info",
                                        tint = if (task.synced) PriorityLow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (task.synced) "W chmurze" else "Lokalnie",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Main Title description
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Brak szczegółowego opisu.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            // Details block (Date / Progress)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TERMIN REALIZACJI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = "Kalendarz", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text(formattedDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("STAN UKOŃCZENIA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.toggleTaskCompletion(task) }
                                            .background(if (task.isCompleted) PriorityLow.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("details_completion_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Zmień status",
                                            tint = if (task.isCompleted) PriorityLow else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (task.isCompleted) "Ukończone" else "Do zrobienia",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (task.isCompleted) PriorityLow else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Checklist Section (Subtasks)
                    Text(
                        text = "Kroki do wykonania (${task.subtasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Subtask interactive list
                            if (task.subtasks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Checklist, contentDescription = "Subtasks", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.secondary)
                                        Text("Brak podzadań", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Dodaj poszczególne kroki poniżej, aby rozbić zadanie na części.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    task.subtasks.forEach { sub ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Checkbox(
                                                    checked = sub.isCompleted,
                                                    onCheckedChange = { viewModel.toggleSubtaskCompletion(task, sub.id) },
                                                    modifier = Modifier.testTag("subtask_checkbox_${sub.id}"),
                                                    colors = CheckboxDefaults.colors(checkedColor = PriorityLow)
                                                )
                                                Text(
                                                    text = sub.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
                                                    ),
                                                    color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.removeSubtaskFromTask(task, sub.id) },
                                                modifier = Modifier
                                                    .semantics { contentDescription = "Usuń krok ${sub.title}" }
                                                    .testTag("subtask_delete_btn_${sub.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete subtask", tint = PriorityHigh.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

                            // Inline add new subtask input form
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newSubtaskTitle,
                                    onValueChange = { newSubtaskTitle = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("new_subtask_input"),
                                    placeholder = { Text("Dodaj nowy krok...", fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        focusedContainerColor = MaterialTheme.colorScheme.background,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (newSubtaskTitle.isNotBlank()) {
                                            viewModel.addSubtaskToTask(task, newSubtaskTitle.trim())
                                            newSubtaskTitle = ""
                                        }
                                    },
                                    enabled = newSubtaskTitle.isNotBlank(),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (newSubtaskTitle.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                        .semantics { contentDescription = "Wprowadź nowy krok" }
                                        .testTag("new_subtask_add_btn"),
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Dodaj podzadanie")
                                }
                            }
                        }
                    }

                    // Delete Task Large Outlined Button
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteTask(task)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("delete_task_bottom_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PriorityHigh),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PriorityHigh.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Usuń Zadanie", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
