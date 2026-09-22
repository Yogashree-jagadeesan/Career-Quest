package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.NoteCategory
import com.example.data.models.NoteItem
import com.example.data.storage.CareerQuestData
import com.example.services.StreakService
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HeritageOrnamentalCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(
    data: CareerQuestData,
    onAddNote: (title: String, content: String, category: NoteCategory, tags: List<String>, reviewDate: String?) -> Unit,
    onEditNote: (id: String, title: String, content: String, category: NoteCategory, tags: List<String>, reviewDate: String?) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onDuplicateNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteItem?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<NoteCategory?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showRevisionOnly by remember { mutableStateOf(false) }

    val today = StreakService.getTodayDate()

    val filteredNotes = data.notes.filter { note ->
        val matchesCategory = selectedCategoryFilter == null || note.category == selectedCategoryFilter
        val matchesFav = !showFavoritesOnly || note.favorite
        val matchesRev = !showRevisionOnly || (!note.reviewDate.isNullOrBlank() && note.reviewDate <= today)
        val matchesQuery = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true) ||
                note.tags.any { it.contains(searchQuery, ignoreCase = true) }

        matchesCategory && matchesFav && matchesRev && matchesQuery
    }.sortedWith(
        compareByDescending<NoteItem> { it.pinned }
            .thenByDescending { it.updatedAt }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notes_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeritageOrnamentalCard(borderColor = DivineGold) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Knowledge Garden & Notes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DivineGold
                        )
                        Text(
                            "Technical insights & revision (+5 XP per note)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DivineGold, contentColor = PeacockBlueDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Note", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by title, content, or #tags...", color = Color.White.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DivineGold) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xD9061A2D), RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = DivineGold,
                    focusedBorderColor = DivineGold,
                    unfocusedBorderColor = GlassGoldBorder
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Filter chips row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = !showFavoritesOnly && !showRevisionOnly && selectedCategoryFilter == null,
                        onClick = {
                            showFavoritesOnly = false
                            showRevisionOnly = false
                            selectedCategoryFilter = null
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DivineGold,
                            selectedLabelColor = PeacockBlueDark,
                            containerColor = Color(0x66061A2D),
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = !showFavoritesOnly && !showRevisionOnly && selectedCategoryFilter == null, borderColor = GlassGoldBorder),
                        label = { Text("All (${data.notes.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = showFavoritesOnly,
                        onClick = { showFavoritesOnly = !showFavoritesOnly },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = DivineGold, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DivineGold,
                            selectedLabelColor = PeacockBlueDark,
                            containerColor = Color(0x66061A2D),
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = showFavoritesOnly, borderColor = GlassGoldBorder),
                        label = { Text("Favorites") }
                    )
                }
                item {
                    FilterChip(
                        selected = showRevisionOnly,
                        onClick = { showRevisionOnly = !showRevisionOnly },
                        leadingIcon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = TurquoiseAccent, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DivineGold,
                            selectedLabelColor = PeacockBlueDark,
                            containerColor = Color(0x66061A2D),
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = showRevisionOnly, borderColor = GlassGoldBorder),
                        label = { Text("Due for Revision") }
                    )
                }
                items(NoteCategory.entries) { cat ->
                    val count = data.notes.count { it.category == cat }
                    val isSel = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DivineGold,
                            selectedLabelColor = PeacockBlueDark,
                            containerColor = Color(0x66061A2D),
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSel, borderColor = GlassGoldBorder),
                        label = { Text("${cat.displayName} ($count)") }
                    )
                }
            }
        }

        // Notes items
        if (filteredNotes.isEmpty()) {
            item {
                EmptyStateView(
                    message = "No notes found. Record your technical insights and algorithm discoveries!",
                    iconSymbol = "📝",
                    actionText = "Create Note",
                    onAction = { showAddDialog = true }
                )
            }
        } else {
            items(filteredNotes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onEdit = { noteToEdit = note },
                    onToggleFavorite = { onToggleFavorite(note.id) },
                    onTogglePin = { onTogglePin(note.id) },
                    onDuplicate = { onDuplicateNote(note.id) },
                    onDelete = { noteToDelete = note }
                )
            }
        }
    }

    if (showAddDialog) {
        NoteFormDialog(
            note = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, content, cat, tags, revDate ->
                onAddNote(title, content, cat, tags, revDate)
                showAddDialog = false
            }
        )
    }

    if (noteToEdit != null) {
        NoteFormDialog(
            note = noteToEdit,
            onDismiss = { noteToEdit = null },
            onSave = { title, content, cat, tags, revDate ->
                noteToEdit?.let { onEditNote(it.id, title, content, cat, tags, revDate) }
                noteToEdit = null
            }
        )
    }

    if (noteToDelete != null) {
        ConfirmationDialog(
            title = "Delete Note",
            message = "Are you sure you want to remove '${noteToDelete?.title}'?",
            onConfirm = {
                noteToDelete?.let { onDeleteNote(it.id) }
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }
}

@Composable
fun NoteCard(
    note: NoteItem,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() }
            .background(Color(0xD9061A2D))
            .border(
                1.2.dp,
                if (note.pinned) DivineGold else GlassGoldBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (note.pinned) {
                        Text("📌", fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x3338BDF8))
                            .border(1.dp, TurquoiseAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(note.category.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TurquoiseAccent)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (note.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (note.favorite) DivineGold else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (note.pinned) DivineGold else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DivineGold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 4
            )

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    note.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x26FBBF24))
                                .border(0.8.dp, DivineGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("#$tag", fontSize = 10.sp, color = GoldYellowWarm)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Updated ${note.updatedAt}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))

                if (!note.reviewDate.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33FBBF24))
                            .border(1.dp, DivineGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("🔔 Review: ${note.reviewDate}", fontSize = 10.sp, color = DivineGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NoteFormDialog(
    note: NoteItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: NoteCategory, tags: List<String>, reviewDate: String?) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var category by remember { mutableStateOf(note?.category ?: NoteCategory.CODING) }
    var tagsStr by remember { mutableStateOf(note?.tags?.joinToString(", ") ?: "") }
    var reviewDate by remember { mutableStateOf(note?.reviewDate ?: "") }

    fun addDaysToToday(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    if (note == null) "New Learning Note" else "Edit Note",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PeacockBlueDark
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
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
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note Content / Code / Formulae") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
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
                    items(NoteCategory.entries) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = tagsStr,
                    onValueChange = { tagsStr = it },
                    label = { Text("Tags (comma separated, e.g. DP, Greedy, Math)") },
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

                // Revision reminders helper buttons
                Text("Spaced Repetition / Revision Schedule", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PeacockBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(
                        onClick = { reviewDate = addDaysToToday(1) },
                        label = { Text("Tomorrow", fontSize = 10.sp) }
                    )
                    SuggestionChip(
                        onClick = { reviewDate = addDaysToToday(3) },
                        label = { Text("+3 Days", fontSize = 10.sp) }
                    )
                    SuggestionChip(
                        onClick = { reviewDate = addDaysToToday(7) },
                        label = { Text("+7 Days", fontSize = 10.sp) }
                    )
                    SuggestionChip(
                        onClick = { reviewDate = addDaysToToday(30) },
                        label = { Text("+30 Days", fontSize = 10.sp) }
                    )
                }

                OutlinedTextField(
                    value = reviewDate,
                    onValueChange = { reviewDate = it },
                    label = { Text("Review Date (YYYY-MM-DD)") },
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
                        onClick = {
                            val tagsList = tagsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onSave(title, content, category, tagsList, reviewDate.ifBlank { null })
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save (+10 XP)")
                    }
                }
            }
        }
    }
}
