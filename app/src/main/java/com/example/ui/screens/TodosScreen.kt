package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.TaskCategory
import com.example.data.models.TaskItem
import com.example.data.models.TaskPriority
import com.example.data.storage.CareerQuestData
import com.example.services.StreakService
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*

enum class TodoFilter(val displayName: String) {
    ALL("All"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    HIGH_PRIORITY("High Priority")
}

@Composable
fun TodosScreen(
    data: CareerQuestData,
    onToggleTask: (String) -> Unit,
    onAddTask: (title: String, desc: String, category: TaskCategory, priority: TaskPriority, dueDate: String) -> Unit,
    onEditTask: (id: String, title: String, desc: String, category: TaskCategory, priority: TaskPriority, dueDate: String) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }
    var selectedFilter by remember { mutableStateOf(TodoFilter.ALL) }
    var selectedCategoryFilter by remember { mutableStateOf<TaskCategory?>(null) }

    val today = StreakService.getTodayDate()

    val filteredTasks = data.tasks.filter { task ->
        val matchesCategory = selectedCategoryFilter == null || task.category == selectedCategoryFilter
        val matchesFilter = when (selectedFilter) {
            TodoFilter.ALL -> true
            TodoFilter.TODAY -> task.dueDate == today || task.dueDate.isBlank()
            TodoFilter.UPCOMING -> task.dueDate > today
            TodoFilter.PENDING -> !task.completed
            TodoFilter.COMPLETED -> task.completed
            TodoFilter.HIGH_PRIORITY -> task.priority == TaskPriority.HIGH
        }
        matchesCategory && matchesFilter
    }

    val completedCount = data.tasks.count { it.completed }
    val totalCount = data.tasks.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("todos_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeritageOrnamentalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Daily & Career To-Do",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockBlueDark
                        )
                        Text(
                            "$completedCount / $totalCount tasks completed (+10 XP each)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Task", fontSize = 13.sp)
                    }
                }
            }
        }

        // Quick Filter Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TodoFilter.entries) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.displayName, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Category Filter Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All Categories", fontSize = 11.sp) }
                    )
                }
                items(TaskCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text(cat.displayName, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Tasks list
        if (filteredTasks.isEmpty()) {
            item {
                EmptyStateView(
                    message = "No tasks found in this view. Keep moving forward!",
                    iconSymbol = "🎯",
                    actionText = "Add New Task",
                    onAction = { showAddDialog = true }
                )
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                DetailedTaskCard(
                    task = task,
                    onToggle = { onToggleTask(task.id) },
                    onEdit = { taskToEdit = task },
                    onDelete = { taskToDelete = task }
                )
            }
        }
    }

    if (showAddDialog) {
        TaskFormDialog(
            task = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, cat, prio, due ->
                onAddTask(title, desc, cat, prio, due)
                showAddDialog = false
            }
        )
    }

    if (taskToEdit != null) {
        TaskFormDialog(
            task = taskToEdit,
            onDismiss = { taskToEdit = null },
            onSave = { title, desc, cat, prio, due ->
                taskToEdit?.let { onEditTask(it.id, title, desc, cat, prio, due) }
                taskToEdit = null
            }
        )
    }

    if (taskToDelete != null) {
        ConfirmationDialog(
            title = "Delete Task",
            message = "Are you sure you want to remove '${taskToDelete?.title}'?",
            onConfirm = {
                taskToDelete?.let { onDeleteTask(it.id) }
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }
}

@Composable
fun DetailedTaskCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> HardRed
        TaskPriority.MEDIUM -> GoldTrim
        TaskPriority.LOW -> TurquoiseAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (task.completed) EmeraldMint.copy(alpha = 0.3f) else CreamCardBorder,
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed) Color(0xFFFAFEFC) else Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldMint,
                    uncheckedColor = TextMuted
                )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.completed) TextMuted else PeacockBlueDark,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            task.priority.displayName,
                            fontSize = 10.sp,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TurquoiseSoft)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            task.category.displayName,
                            fontSize = 10.sp,
                            color = PeacockBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (task.dueDate.isNotBlank()) {
                        Text("📅 ${task.dueDate}", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun TaskFormDialog(
    task: TaskItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, category: TaskCategory, priority: TaskPriority, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var category by remember { mutableStateOf(task?.category ?: TaskCategory.CODING) }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: StreakService.getTodayDate()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    if (task == null) "Add New Task" else "Edit Task",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PeacockBlueDark
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Text("Category", fontSize = 12.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TaskCategory.entries) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Priority", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1E1E),
                        unfocusedTextColor = Color(0xFF1E1E1E),
                        cursorColor = PeacockBlue,
                        focusedBorderColor = PeacockBlue,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(title, description, category, priority, dueDate) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
