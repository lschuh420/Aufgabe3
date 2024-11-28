package com.example.aufgabe3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aufgabe3.ui.theme.Aufgabe3Theme

/**
 * MainActivity.kt: Haupt-Einstiegspunkt der App.
 * Aktiviert die "Edge-to-Edge"-Anzeige und setzt den Inhalt der App.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Aktiviert die "Edge-to-Edge"-Anzeige für ein modernes UI-Design
        setContent {
            Aufgabe3Theme { // Setzt das Theme der App
                Scaffold(
                    content = { paddingValues ->
                        TodoListScreen(
                            modifier = Modifier.padding(paddingValues),
                            viewModel = TodoViewModel() // Übergibt das ViewModel an die View
                        )
                    }
                )
            }
        }
    }
}

/**
 * Datenmodell für ein ToDo-Item.
 *
 * @param title Der Titel des ToDo-Items.
 * @param isCompleted Der Status des ToDo-Items (erledigt oder nicht).
 * @param priority Die Priorität des ToDo-Items.
 */
data class TodoItem(
    val title: String,       // Titel des ToDo
    val isCompleted: Boolean, // Status: erledigt oder nicht
    val priority: Priority   // Priorität (HIGH, MEDIUM, LOW)
)

/**
 * Enum für die Prioritäten der ToDos.
 */
enum class Priority {
    HIGH, MEDIUM, LOW
}

/**
 * UI-Komponente, die den ToDo-Bildschirm definiert.
 *
 * Zeigt eine Liste von ToDo-Items an und ermöglicht das Hinzufügen neuer Items.
 *
 * @param modifier Modifier zur Anpassung des Layouts.
 * @param viewModel Das ViewModel, das die Logik und die Daten für die Liste verwaltet.
 */
@Composable
fun TodoListScreen(modifier: Modifier, viewModel: TodoViewModel) {
    var showDialog by remember { mutableStateOf(false) } // Steuert die Sichtbarkeit des Dialogs
    var todoText by remember { mutableStateOf("") }      // Hält den Text für ein neues ToDo

    val todos by viewModel.todos.observeAsState(emptyList()) // Beobachtet die LiveData-Liste
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) } // Standardpriorität

    Box(modifier = modifier.fillMaxSize()) { // Container für das gesamte Layout
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Todo List",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            )
            // Scrollbare Liste aller ToDos
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(todos) { todoItem -> // Für jedes ToDo-Item
                    TodoItemCard(
                        item = todoItem,
                        onItemClicked = { viewModel.toggleTodoItem(it) }, // Abhaken
                        onDeletClicked = { viewModel.deleteTodoItem(it) } // Löschen
                    )
                }
            }
        }

        // Schaltfläche zum Hinzufügen eines neuen ToDos
        FloatingActionButton(
            onClick = { showDialog = true }, // Öffnet den Dialog
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Text(text = "Add")
        }

        // Dialog zum Hinzufügen eines neuen ToDos
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Add a new ToDo") },
                text = {
                    Column {
                        // Eingabefeld für den Titel
                        OutlinedTextField(
                            value = todoText,
                            onValueChange = { todoText = it },
                            label = { Text("Enter ToDo title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dropdown für Priorität
                        var expanded by remember { mutableStateOf(false) }
                        Text("Priority: ${selectedPriority.name}", modifier = Modifier.padding(bottom = 8.dp))
                        IconButton(onClick = { expanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            Priority.values().forEach { priority ->
                                DropdownMenuItem(
                                    text = { Text(priority.name) },
                                    onClick = {
                                        selectedPriority = priority
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (todoText.isNotBlank()) {
                                viewModel.addItem(todoText, selectedPriority) // Fügt neues ToDo hinzu
                                todoText = "" // Reset des Textfelds
                                showDialog = false // Schließt den Dialog
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Vorschau der `TodoListScreen`-Komponente für die UI-Entwicklung.
 */
@Preview(showBackground = true)
@Composable
fun TodoListScreenPreview(){
    Aufgabe3Theme {
        TodoListScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = TodoViewModel()
        )
    }
}

/**
 * UI-Komponente für das ToDo-Item, das in der Liste angezeigt wird.
 *
 * Zeigt den Titel, den Status (abgehakt oder nicht), und die Priorität des ToDos an.
 *
 * @param item Das ToDo-Item, das angezeigt wird.
 * @param onItemClicked Callback, wenn das ToDo abgehakt wird.
 * @param onDeletClicked Callback, wenn das ToDo gelöscht wird.
 */
@Composable
fun TodoItemCard(
    item: TodoItem,
    onItemClicked: (TodoItem) -> Unit,
    onDeletClicked: (TodoItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { onItemClicked(item) }
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Priority: ${item.priority.name}",
                style = MaterialTheme.typography.bodySmall,
                color = when (item.priority) {
                    Priority.HIGH -> MaterialTheme.colorScheme.error
                    Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                    Priority.LOW -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
        IconButton(onClick = { onDeletClicked(item) }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

/**
 * ViewModel, das die Daten und Logik für die ToDo-Liste verwaltet.
 *
 * @property todos Die Liste der ToDos, die im UI angezeigt werden.
 */
class TodoViewModel : ViewModel() {
    private val _todos = MutableLiveData<List<TodoItem>>(emptyList())
    val todos: LiveData<List<TodoItem>> get() = _todos

    /**
     * Fügt ein neues ToDo-Item hinzu.
     *
     * @param title Der Titel des ToDo-Items.
     * @param priority Die Priorität des ToDo-Items.
     */
    fun addItem(title: String, priority: Priority) {
        val currentList = _todos.value ?: emptyList()
        val updatedList = (currentList + TodoItem(title = title, isCompleted = false, priority = priority))
            .sortedBy { it.priority }
        _todos.value = updatedList
    }

    /**
     * Löscht ein ToDo-Item aus der Liste.
     *
     * @param todoItem Das zu löschende ToDo-Item.
     */
    fun deleteTodoItem(todoItem: TodoItem) {
        val currentList = _todos.value ?: emptyList()
        val updatedList = currentList - todoItem
        _todos.value = updatedList
    }

    /**
     * Ändert den Status eines ToDo-Items (abgehakt/nicht abgehakt).
     *
     * @param todoItem Das ToDo-Item, dessen Status geändert werden soll.
     */
    fun toggleTodoItem(todoItem: TodoItem) {
        val currentList = _todos.value ?: emptyList()
        val updatedList = currentList.map {
            if (it == todoItem) {
                it.copy(isCompleted = !it.isCompleted)
            } else {
                it
            }
        }
        _todos.value = updatedList
    }
}
