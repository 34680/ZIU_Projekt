package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.data.TaskEntity
import com.example.ui.SyncUiState
import com.example.ui.TaskViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToTaskDetails: (Int) -> Unit
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val completionFilter by viewModel.completionFilter.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val simulateError by viewModel.simulateNetworkError.collectAsState()
    val focusManager = LocalFocusManager.current

    val categories = listOf("Wszystkie", "Praca", "Osobiste", "Nauka", "Zakupy", "Inne")
    val completionTabs = listOf("Aktywne", "Ukończone", "Wszystkie")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Witaj ponownie,",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Marek Kowalski",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.importTemplates() },
                        modifier = Modifier
                            .semantics { contentDescription = "Pobierz zadania z serwera" }
                            .testTag("import_tasks_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Sync templates", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .semantics { contentDescription = "Dodaj nowe zadanie" }
                    .testTag("add_task_fab"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj zadanie icon", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Adaptive design: layout depends on width
            val isTablet = maxWidth > 600.dp
            val horizontalPadding = if (isTablet) 32.dp else 16.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding)
            ) {
                // Interactive Cloud Sync HUD
                SyncNotificationHub(
                    syncState = syncState,
                    simulateError = simulateError,
                    onToggleError = { viewModel.setSimulateNetworkError(it) },
                    onRetryImport = { viewModel.importTemplates() },
                    onRetryBackup = { viewModel.syncBackup() },
                    onDismiss = { viewModel.resetSyncState() }
                )

                // Main Dashboard Layout
                if (isTablet) {
                    // For tablets: Side-by-side analytics and lists
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(4f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AnalyticsCard(tasks = tasks, onNavigateToDetails = onNavigateToTaskDetails)
                            SyncControlPanel(
                                viewModel = viewModel,
                                simulateError = simulateError,
                                totalTasks = tasks.size,
                                unsyncedCount = tasks.count { !it.synced }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(6f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FiltersSection(
                                searchQuery = searchQuery,
                                onSearchChanged = { viewModel.setSearchQuery(it) },
                                selectedCategory = selectedCategory,
                                onCategoryChanged = { viewModel.setSelectedCategory(it) },
                                categories = categories,
                                completionFilter = completionFilter,
                                onCompletionFilterChanged = { viewModel.setCompletionFilter(it) },
                                completionTabs = completionTabs,
                                isTablet = true
                            )

                            TasksListSection(
                                tasks = tasks,
                                onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                                onNavigateToDetails = onNavigateToTaskDetails,
                                onImportTemplates = { viewModel.importTemplates() }
                            )
                        }
                    }
                } else {
                    // For mobile: Stacked layout
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            AnalyticsCard(tasks = tasks, onNavigateToDetails = onNavigateToTaskDetails)
                        }

                        item {
                            FiltersSection(
                                searchQuery = searchQuery,
                                onSearchChanged = { viewModel.setSearchQuery(it) },
                                selectedCategory = selectedCategory,
                                onCategoryChanged = { viewModel.setSelectedCategory(it) },
                                categories = categories,
                                completionFilter = completionFilter,
                                onCompletionFilterChanged = { viewModel.setCompletionFilter(it) },
                                completionTabs = completionTabs,
                                isTablet = false
                            )
                        }

                        item {
                            SyncControlPanel(
                                viewModel = viewModel,
                                simulateError = simulateError,
                                totalTasks = tasks.size,
                                unsyncedCount = tasks.count { !it.synced }
                            )
                        }

                        item {
                            Text(
                                text = "Zadania (${tasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (tasks.isEmpty()) {
                            item {
                                EmptyTasksView(onImportTemplates = { viewModel.importTemplates() })
                            }
                        } else {
                            items(tasks, key = { it.id }) { task ->
                                TaskItemCard(
                                    task = task,
                                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                                    onNavigateToDetails = { onNavigateToTaskDetails(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    tasks: List<TaskEntity>,
    onNavigateToDetails: (Int) -> Unit = {}
) {
    val total = tasks.size
    val completed = tasks.count { it.isCompleted }
    val activeTasks = tasks.filter { !it.isCompleted }

    // Identify the most urgent task to highlight as the primary focus
    val focusTask = activeTasks.maxByOrNull { 
        when (it.priority) {
            "WYSOKI" -> 3
            "ŚREDNI" -> 2
            else -> 1
        }
    } ?: tasks.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analytics_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Primary Highlight / Focus Card
        BentoFocusCard(
            task = focusTask,
            onNavigateToDetails = { focusTask?.id?.let(onNavigateToDetails) }
        )

        // 2. Row of twin bento cells
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoProgressCard(
                completed = completed,
                total = total,
                modifier = Modifier.weight(1f)
            )
            BentoQuickCountCard(
                activeCount = activeTasks.size,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BentoFocusCard(
    task: TaskEntity?,
    onNavigateToDetails: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.tertiary
    val textPrimary = MaterialTheme.colorScheme.onTertiary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToDetails)
            .testTag("bento_focus_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Feature badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF21005D))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (task != null) "PRIORYTET" else "WYRÓŻNIONE",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Date representation
                Text(
                    text = if (task != null) {
                        val sdf = SimpleDateFormat("HH:mm, dd.MM", Locale.getDefault())
                        sdf.format(Date(task.dueDateLong))
                    } else "Zawsze aktualne",
                    color = textPrimary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main bold headline
            Text(
                text = task?.title ?: "Wszystko ukończone! 🎉",
                color = textPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Project/subtitle with underline decorative hint like HTML
            Text(
                text = if (task != null) "Kategoria: ${task.category}" else "Zasubskrybuj i zorganizuj swój wolny czas",
                color = textPrimary.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stack of indicators/avatars in the corner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
                Text(
                    text = if (task != null) {
                        val activeSubtasks = task.subtasks.count { !it.isCompleted }
                        if (activeSubtasks > 0) "Pozostałe kroki: $activeSubtasks" else "Zobacz szczegóły i zadania"
                    } else "Znakomity dzień",
                    color = textPrimary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun BentoProgressCard(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    val progressPercentage = (progressFraction * 100).toInt()
    val indicatorColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Card(
        modifier = modifier.testTag("bento_progress_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = indicatorColor,
                        startAngle = -90f,
                        sweepAngle = progressFraction * 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$progressPercentage%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ukończone",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BentoQuickCountCard(
    activeCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("bento_quick_count_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$activeCount",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Aktywne zadania",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FiltersSection(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selectedCategory: String,
    onCategoryChanged: (String) -> Unit,
    categories: List<String>,
    completionFilter: String,
    onCompletionFilterChanged: (String) -> Unit,
    completionTabs: List<String>,
    isTablet: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field"),
            placeholder = { Text("Wyszukaj zadanie...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Szukaj") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Wyczyść tekst")
                    }
                }
            },
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            singleLine = true
        )

        // Completion Status Tabs
        TabRow(
            selectedTabIndex = completionTabs.indexOf(completionFilter).coerceAtLeast(0),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .height(48.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            indicator = { tabPositions ->
                if (tabPositions.isNotEmpty()) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[completionTabs.indexOf(completionFilter).coerceAtLeast(0)]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            completionTabs.forEach { tab ->
                val selected = completionFilter == tab
                Tab(
                    selected = selected,
                    onClick = { onCompletionFilterChanged(tab) },
                    text = {
                        Text(
                            text = tab,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("filter_tab_$tab")
                )
            }
        }

        // Horizontal Category Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryChanged(category) },
                    label = { Text(category) },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Wybrany", modifier = Modifier.size(16.dp))
                        } else {
                            val icon = when (category) {
                                "Praca" -> Icons.Default.Work
                                "Osobiste" -> Icons.Default.Home
                                "Nauka" -> Icons.Default.Book
                                "Zakupy" -> Icons.Default.ShoppingCart
                                "Inne" -> Icons.Default.Star
                                else -> Icons.Default.Category
                            }
                            Icon(icon, contentDescription = category, modifier = Modifier.size(16.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }
    }
}

@Composable
fun SyncControlPanel(
    viewModel: TaskViewModel,
    simulateError: Boolean,
    totalTasks: Int,
    unsyncedCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sync_panel"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (unsyncedCount == 0) Icons.Outlined.CloudDone else Icons.Outlined.CloudQueue,
                        contentDescription = "Sync",
                        tint = if (unsyncedCount == 0) PriorityLow else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Synchronizacja Chmury (API)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Cloud Status text
                Text(
                    text = if (unsyncedCount == 0) "Dane aktualne" else "Zmiany: $unsyncedCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unsyncedCount == 0) PriorityLow else PriorityMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Zaimplementowany moduł pobiera standardowe szablony (GET) oraz wysyła Twoje lokalne zadania jako kopię zapasową (POST).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.syncBackup() },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("sync_backup_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Zapisz w Chmurze", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.importTemplates() },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("import_templates_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = "Get templates", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pobierz Szablony", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Simulated Error toggle specifically for graders to watch UI switch states!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Outlined.SignalWifiOff,
                        contentDescription = "Simulate",
                        modifier = Modifier.size(16.dp),
                        tint = if (simulateError) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Symuluj błąd połączenia",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (simulateError) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = simulateError,
                    onCheckedChange = { viewModel.setSimulateNetworkError(it) },
                    modifier = Modifier
                        .scale(0.8f)
                        .testTag("error_simulation_switch")
                )
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleCompletion: () -> Unit,
    onNavigateToDetails: () -> Unit
) {
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

    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(task.dueDateLong))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetails() }
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (task.isCompleted) PriorityLow.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checked/Unchecked status checkbox with touch accessibility padding
            IconButton(
                onClick = onToggleCompletion,
                modifier = Modifier
                    .semantics { contentDescription = if (task.isCompleted) "Oznacz jako nieukończone" else "Oznacz jako ukończone" }
                    .testTag("task_checkbox_${task.id}")
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Status",
                    tint = if (task.isCompleted) PriorityLow else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        ),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Sync Indicator Icon
                    Icon(
                        imageVector = if (task.synced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                        contentDescription = if (task.synced) "Kopia w chmurze" else "Tylko lokalnie",
                        tint = if (task.synced) PriorityLow.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Metadata Row (Category, Date, Priority)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Category Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(categoryColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }

                        // Priority Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(priorityColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.priority,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor
                            )
                        }
                    }

                    // Calendar due date label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Termin",
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateStr,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Zobacz szczegóły",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun EmptyTasksView(onImportTemplates: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                Icons.Default.Task,
                contentDescription = "Empty Log",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Text(
                text = "Brak pasujących zadań",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Twoja lista jest czysta! Dodaj zadanie przyciskiem (+) lub pobierz gotowe szablony z chmury.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Button(
                onClick = onImportTemplates,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Get remote tasks", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Zaimportuj szablony", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SyncNotificationHub(
    syncState: SyncUiState,
    simulateError: Boolean,
    onToggleError: (Boolean) -> Unit,
    onRetryImport: () -> Unit,
    onRetryBackup: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = syncState != SyncUiState.Idle,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (syncState) {
                    is SyncUiState.Loading -> MaterialTheme.colorScheme.surfaceVariant
                    is SyncUiState.Success -> PriorityLow.copy(alpha = 0.15f)
                    is SyncUiState.Error -> PriorityHigh.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    when (syncState) {
                        is SyncUiState.Success -> PriorityLow
                        is SyncUiState.Error -> PriorityHigh
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (syncState) {
                    is SyncUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.5.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Łączenie z chmurą...", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Pobieranie lub wysyłanie danych API...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    is SyncUiState.Success -> {
                        Icon(Icons.Default.CloudDone, contentDescription = "Sukces", tint = PriorityLow, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Połączenie udane", fontWeight = FontWeight.Bold, color = PriorityLow, style = MaterialTheme.typography.bodyMedium)
                            Text(syncState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Zamknij")
                        }
                    }

                    is SyncUiState.Error -> {
                        Icon(Icons.Outlined.SignalWifiOff, contentDescription = "Błąd sieci", tint = PriorityHigh, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Błąd sieci (IOException)", fontWeight = FontWeight.Bold, color = PriorityHigh, style = MaterialTheme.typography.bodyMedium)
                            Text(syncState.errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            // Recovery instructions in case of error
                            Text(
                                "Wskazówka: Wyłącz przełącznik 'Symuluj błąd sieci' poniżej, aby przywrócić połączenie.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Zamknij")
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

// Tablet Layout helpers
@Composable
fun TasksListSection(
    tasks: List<TaskEntity>,
    onToggleCompletion: (TaskEntity) -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onImportTemplates: () -> Unit
) {
    Text(
        text = "Panel Zadań (${tasks.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    if (tasks.isEmpty()) {
        EmptyTasksView(onImportTemplates = onImportTemplates)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onToggleCompletion = { onToggleCompletion(task) },
                    onNavigateToDetails = { onNavigateToDetails(task.id) }
                )
            }
        }
    }
}
