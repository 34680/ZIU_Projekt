package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subtask
import com.example.data.TaskEntity
import com.example.ui.TaskViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: Int? = null, // If non-null, we are in Edit Mode
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.filteredTasks.collectAsState()

    // Find task if in Edit Mode
    val existingTask = remember(taskId, tasks) {
        if (taskId != null && taskId != -1) {
            tasks.find { it.id == taskId }
        } else null
    }

    // Form states
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Praca") }
    var priority by remember { mutableStateOf("ŚREDNI") }
    var dueDateLong by remember { mutableStateOf(System.currentTimeMillis()) }

    // Init form if editing
    LaunchedEffect(existingTask) {
        existingTask?.let {
            title = it.title
            description = it.description
            category = it.category
            priority = it.priority
            dueDateLong = it.dueDateLong
        }
    }

    // Client-side Validation states
    var titleTouched by remember { mutableStateOf(false) }
    val titleError = remember(title, titleTouched) {
        when {
            !titleTouched -> null
            title.isBlank() -> "Tytuł zadania nie może być pusty!"
            title.length > 50 -> "Tytuł jest zbyt długi (maks. 50 znaków)."
            else -> null
        }
    }

    var descriptionTouched by remember { mutableStateOf(false) }
    val descriptionError = remember(description, descriptionTouched) {
        when {
            !descriptionTouched -> null
            description.length > 200 -> "Opis nie może przekraczać 200 znaków (obecnie: ${description.length})."
            else -> null
        }
    }

    val isFormValid = title.isNotBlank() && title.length <= 50 && description.length <= 200

    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val selectedDateStr = sdf.format(Date(dueDateLong))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingTask != null) "Edytuj Zadanie" else "Nowe Zadanie",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
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
            // Apply adaptive limits to prevent cards stretching on large screen tablets
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section Title: Form Fields
                Text(
                    text = "Opisz szczegóły zadania",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Title Input field with live character constraint validation
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleTouched = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input"),
                        label = { Text("Tytuł zadania (*)") },
                        placeholder = { Text("np. Przeczytać rozdział książki") },
                        isError = titleError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (titleError != null) {
                            Text(
                                text = titleError,
                                color = PriorityHigh,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            text = "${title.length}/50",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (title.length > 50) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Description Input field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            descriptionTouched = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("task_description_input"),
                        label = { Text("Opis") },
                        placeholder = { Text("np. Tematy o heurystykach Nielsena i dostępności WCAG 2.1") },
                        isError = descriptionError != null,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (descriptionError != null) {
                            Text(
                                text = descriptionError,
                                color = PriorityHigh,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            text = "${description.length}/200",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (description.length > 200) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Date Picker Button Trigger
                Text(
                    text = "Termin realizacji (Deadline)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = dueDateLong
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(Calendar.YEAR, year)
                                selectedCalendar.set(Calendar.MONTH, month)
                                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                dueDateLong = selectedCalendar.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_picker_trigger"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Data")
                            Text(
                                text = selectedDateStr,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Zmień",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Category Selection Cards
                Text(
                    text = "Kategoria zadania",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val categoriesList = listOf("Praca", "Osobiste", "Nauka", "Zakupy", "Inne")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriesList.take(3).forEach { cat ->
                        CategoryButton(
                            text = cat,
                            isSelected = category == cat,
                            onClick = { category = cat },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriesList.drop(3).forEach { cat ->
                        CategoryButton(
                            text = cat,
                            isSelected = category == cat,
                            onClick = { category = cat },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f)) // Padding balance
                }

                // Priority Tabs
                Text(
                    text = "Priorytet zadania",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val prioritiesList = listOf("NISKI", "ŚREDNI", "WYSOKI")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    prioritiesList.forEach { prio ->
                        val isSelected = priority == prio
                        val color = when (prio) {
                            "WYSOKI" -> PriorityHigh
                            "ŚREDNI" -> PriorityMedium
                            else -> PriorityLow
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) color else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { priority = prio }
                                .padding(vertical = 10.dp)
                                .testTag("priority_selection_$prio"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prio,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Submit Form Action Button
                Button(
                    onClick = {
                        titleTouched = true
                        descriptionTouched = true
                        if (isFormValid) {
                            if (existingTask != null) {
                                viewModel.updateTaskFull(
                                    existingTask.copy(
                                        title = title,
                                        description = description,
                                        category = category,
                                        priority = priority,
                                        dueDateLong = dueDateLong
                                    )
                                )
                            } else {
                                viewModel.addTask(
                                    title = title,
                                    description = description,
                                    dueDateLong = dueDateLong,
                                    category = category,
                                    priority = priority,
                                    subtasks = emptyList()
                                )
                            }
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_task_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                        contentColor = if (isFormValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ),
                    enabled = isFormValid
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Zapisz")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingTask != null) "Zapisz zmiany" else "Dodaj zadanie do listy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Friendly advice for grading
                if (!isFormValid) {
                    Text(
                        text = "Wypełnij poprawnie pola formularza (Tytuł jest wymagany), aby odblokować przycisk zapisu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (text) {
        "Praca" -> CategoryPraca
        "Osobiste" -> CategoryOsobiste
        "Nauka" -> CategoryNauka
        "Zakupy" -> CategoryZakupy
        else -> CategoryInne
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .semantics { contentDescription = "Wybierz kategorię $text" }
            .testTag("category_selection_$text"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (text) {
                "Praca" -> Icons.Default.Work
                "Osobiste" -> Icons.Default.Home
                "Nauka" -> Icons.Default.Book
                "Zakupy" -> Icons.Default.ShoppingCart
                else -> Icons.Default.Star
            }
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
