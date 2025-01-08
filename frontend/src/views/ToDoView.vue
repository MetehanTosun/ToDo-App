<script setup lang="ts">
// Import necessary Vue composition API functions and custom components/utilities
import { ref, computed, onMounted } from "vue";
import config from "@/config";
import { showToast, Toast } from "@/ts/toasts";
import { faXmark, faCheck } from "@fortawesome/free-solid-svg-icons";
import { fetchAllAssignees, searchAssignee, filteredAssignees } from "@/ts/Assignee";
import { type Todo, fetchTodos, deleteTodo, updateTodo, showEditModal, currentTodo, editTitle, editDescription, editDueDate, selectedAssignees } from "@/ts/Todo";
import { addTodoStatusChanged } from "@/ts/activity";

// Reactive references for Todos 
import { todos } from "@/ts/Todo"; // Holds all Todos

// Search and filtering state management
const searchQuery = ref("");           // For real-time todo search
const todo = ref<Todo | null>(null);   // Currently selected/searched todo
const filterTitle = ref("");           // Title filter
const filterDueDate = ref<string | null>(null);  // Due date filter
const showCompletedTodos = ref(false); // Toggle for completed todos view

// Sorting state management
const sortKey = ref<"title" | "dueDate" | null>(null);  // Current sort field
const sortOrder = ref(1);  // 1 for ascending, -1 for descending

// Search function to find a specific todo by name
function fetchTodoByName() {
  const query = searchQuery.value.trim();
  if (query === "") {
    todo.value = null;
    return;
  }
  // Find matching todo in current list
  const matchedTodo = todos.value.find((todo) =>
    todo.title.toLowerCase().includes(query.toLowerCase())
  );
  
  if (!matchedTodo) {
    showToast(new Toast("Info", "Todo nicht gefunden", "info", faXmark, 5));
    todo.value = null;
    return;
  }
  
  // Fetch detailed todo data from API
  fetch(`${config.apiBaseUrl}/todos/${matchedTodo.id}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => {
      if (!response.ok) throw new Error("Abrufen des Todos fehlgeschlagen");
      return response.json();
    })
    .then((data) => {
      todo.value = data;
    })
    .catch((error) => {
      console.error(error);
      showToast(new Toast("Error", error.message, "error", faXmark, 10));
    });
}

// Computed property that filters and sorts todos based on current criteria
const filteredTodos = computed(() => {
  let filtered = todos.value;

  // Apply title filter
  if (filterTitle.value) {
    filtered = filtered.filter((todo) =>
      todo.title.toLowerCase().includes(filterTitle.value.toLowerCase())
    );
  }

  // Apply due date filter
  if (filterDueDate.value) {
    const dueDate = new Date(filterDueDate.value).getTime();
    filtered = filtered.filter((todo) => todo.dueDate === dueDate);
  }

  // Filter completed todos if enabled
  if (showCompletedTodos.value) {
    filtered = filtered.filter((todo) => todo.finished && todo.finishedDate);
  }

  // Apply sorting if a sort key is selected
  if (sortKey.value) {
    filtered = filtered.slice().sort((a, b) => {
      const valueA = a[sortKey.value as keyof Todo];
      const valueB = b[sortKey.value as keyof Todo];
      if (valueA === null || valueA === undefined) return 1;
      if (valueB === null || valueB === undefined) return -1;
      if (valueA < valueB) return -1 * sortOrder.value;
      if (valueA > valueB) return 1 * sortOrder.value;
      return 0;
    });
  }

  return filtered;
});

// Function to update a todo's completion status
function updateFinishedStatus(todo: Todo): Promise<void> {
  return new Promise((resolve, reject) => {
    const currentTodoData = todos.value.find((t) => t.id === todo.id);

    if (!currentTodoData) {
      showToast(new Toast("Error", "Todo nicht gefunden", "error", faXmark, 5));
      reject(new Error("Todo nicht gefunden"));
      return;
    }

    // Prepare updated todo data
    const updatedTodo = {
      title: currentTodoData.title,
      description: currentTodoData.description,
      finished: todo.finished,
      assigneeIdList: currentTodoData.assigneeList.map((a) => a.id),
      dueDate: currentTodoData.dueDate,
    };

    // Send update request to API
    fetch(`${config.apiBaseUrl}/todos/${todo.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedTodo),
    })
      .then((response) => {
        if (!response.ok) throw new Error("Fehler beim Aktualisieren des Todos");
        return response.json();
      })
      .then(() => {
        // Update local todo state
        const index = todos.value.findIndex((t) => t.id === todo.id);
        if (index !== -1) {
          todos.value[index] = {
            ...todos.value[index],
            finished: todo.finished,
            finishedDate: todo.finished ? new Date() : null,
          };
        }
        showToast(new Toast("Erfolg", `Todo "${currentTodoData.title}" wurde ${todo.finished ? 'abgeschlossen' : 'wieder geöffnet'}`, "success", faCheck, 5));
        resolve();
      })
      .catch((error) => {
        console.error("Fehler beim Aktualisieren:", error);
        showToast(new Toast("Error", error.message, "error", faXmark, 5));
        reject(error);
      });
  });
}

// Toggle todo completion status
function toggleFinished(todo: Todo) {
  todo.finished = !todo.finished;
  todo.finishedDate = todo.finished ? new Date() : null;

  updateFinishedStatus(todo)
    .then(() => {
      addTodoStatusChanged(todo.title, todo.finished);
      fetchTodos();
    })
    .catch((error) => {
      console.error("Fehler beim Aktualisieren:", error);
    });
}

// Open edit modal with current todo data
function openEditModal(todo: Todo) {
  currentTodo.value = todo;
  editTitle.value = todo.title;
  editDescription.value = todo.description;
  editDueDate.value = todo.dueDate ? new Date(todo.dueDate).toLocaleDateString() : null;
  selectedAssignees.value = [...todo.assigneeList];
  showEditModal.value = true;
}

// Function to download todos as CSV file
function downloadCSV() {
  fetch(`${config.apiBaseUrl}/csv-downloads/todos`)
    .then((response) => {
      if (!response.ok) throw new Error('Download fehlgeschlagen');
      return response.blob();
    })
    .then((blob) => {
      // Create and trigger download
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'todos.csv';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      showToast(new Toast("Success", "CSV erfolgreich heruntergeladen", "success", faCheck, 5));
    })
    .catch((error) => {
      console.error("Fehler beim CSV-Download:", error);
      showToast(new Toast("Error", error.message, "error", faXmark, 10));
    });
}

// Initialize data on component mount
onMounted(() => {
  fetchTodos();
  fetchAllAssignees();
});
</script>

<template>
  <div class="todo-view">
    <!-- Export-Button über der Tabelle -->
    <button class="btn export-btn" @click="downloadCSV">Exportiere Todos als CSV</button>
    
    <!-- Search and filter options -->
    <div class="filters">
      <input
        v-model="searchQuery"
        @input="fetchTodoByName"
        type="text"
        placeholder="Nach Titel suchen..."
        class="search-input"
        id="searchQuery"  
        name="searchQuery"/>
      <button @click="showCompletedTodos = !showCompletedTodos" class="btn">
        {{ showCompletedTodos ? "Alle Todos anzeigen" : "Erledigte Todos" }}
      </button>
    </div>

    <!-- Sorting options -->
    <div class="sort-options">
      <label for="sortKey">Sortieren nach:</label>
      <select id="sortKey" v-model="sortKey" class="dropdown" name="sortKey">
        <option value="title">Titel</option>
        <option value="dueDate">Fälligkeitsdatum</option>
      </select>
      <button @click="sortOrder *= -1" class="btn sort-btn">
        {{ sortOrder === 1 ? "Aufsteigend" : "Absteigend" }}
      </button>
    </div>

    <!-- Todo table -->
    <table class="todo-table">
      <thead>
        <tr>
          <th>Titel</th>
          <th>Beschreibung</th>
          <th>Assignees</th>
          <th>Erstellt am</th>
          <th>Fällig am</th>
          <th v-if="showCompletedTodos">Abgeschlossen am</th>
          <th>Fertig</th>
          <th>Katogorie</th>
          <th>Aktionen</th>
        </tr>
      </thead>

      <tbody>
        <!-- Show a specific Todo when searching -->
        <tr v-if="todo">
          <td>{{ todo.title }}</td>
          <td>{{ todo.description }}</td>
          <td>
            <span v-for="assignee in todo.assigneeList" :key="assignee.id">
              {{ assignee.prename }} {{ assignee.name }}
            </span>
          </td>
          <td>{{ new Date(todo.createdDate).toLocaleDateString() }}</td>
          <td>
            {{ todo.dueDate ? new Date(todo.dueDate).toLocaleDateString() : "—" }}
          </td>
          <td v-if="showCompletedTodos">
            {{ todo.finishedDate ? new Date(todo.finishedDate).toLocaleDateString() : "—" }}
          </td>
          <td>
            <input type="checkbox" :checked="todo.finished" disabled :id="'todo-' + todo.id" :name="'todo-' + todo.id" />
          </td>
          <td>{{ todo.category }}</td>
          <td>
            <button class="btn edit-btn" @click="openEditModal(todo)">Bearbeiten</button>
            <button class="btn delete-btn" @click="deleteTodo(todo.id)">Löschen</button>
          </td>
        </tr>

        <!-- Show all Todos when not searching -->
        <tr v-else v-for="todoItem in filteredTodos" :key="todoItem.id">
          <td>{{ todoItem.title }}</td>
          <td>{{ todoItem.description }}</td>
          <td>
            <span v-for="assignee in todoItem.assigneeList" :key="assignee.id">
              {{ assignee.prename }} {{ assignee.name }}
            </span>
          </td>
          <td>{{ new Date(todoItem.createdDate).toLocaleDateString() }}</td>
          <td>
            {{ todoItem.dueDate ? new Date(todoItem.dueDate).toLocaleDateString() : "—" }}
          </td>
          <td v-if="showCompletedTodos">
            {{ todoItem.finishedDate ? new Date(todoItem.finishedDate).toLocaleDateString() : "—" }}
          </td>
          <td>
            <input type="checkbox" :checked="todoItem.finished" @change="toggleFinished(todoItem)" :id="'todo-' + todoItem.id" :name="'todo-' + todoItem.id" />
          </td>
          <td>{{ todoItem.category }}</td>
          <td>
            <button class="btn edit-btn" @click="openEditModal(todoItem)">Bearbeiten</button>
            <button class="btn delete-btn" @click="deleteTodo(todoItem.id)">Löschen</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Modal for edit -->
    <div v-if="showEditModal" class="modal">
      <div class="modal-content">
        <span class="close-btn" @click="showEditModal = false">&times;</span>
        <h2>Todo Bearbeiten</h2>

        <div class="form-group">
          <label for="editTitle">Titel</label>
          <input v-model="editTitle" type="text" id="editTitle" class="form-input" name="editTitle" />
        </div>

        <div class="form-group">
          <label for="editDescription">Beschreibung</label>
          <input v-model="editDescription" type="text" id="editDescription" class="form-input" name="editDescription" />
        </div>

        <div class="form-group">
          <label for="editDueDate">Fälligkeitsdatum</label>
          <input v-model="editDueDate" type="date" id="editDueDate" class="form-input" name="editDueDate" />
        </div>

        <div class="form-group">
          <label for="editAssignees">Assignees</label>
          <input
            v-model="searchAssignee"
            type="text"
            placeholder="Search for Assignee..."
            class="form-input search-assignees"
            id="editAssigneesSearch"
            name="editAssigneesSearch"/>
          <div id="editAssignees" class="assignee-list">
            <div
              v-for="assignee in filteredAssignees"
              :key="assignee.id"
              class="assignee-item">
              <input 
                type="checkbox" 
                :id="'assignee-' + assignee.id" 
                :value="assignee" 
                v-model="selectedAssignees" 
                :name="'assignee-' + assignee.id"/>
              <label :for="'assignee-' + assignee.id">
                {{ assignee.prename }} {{ assignee.name }}
              </label>
            </div>
          </div>
        </div>
        <div class="button-group">
          <button @click="updateTodo" class="btn btn-save">Speichern</button>
          <button @click="showEditModal = false" class="btn btn-cancel">Abbrechen</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.todo-view {
  position: relative;
  padding: 20px;
}

.filters {
  display: flex;
  position: relative;
  margin-bottom: 30px; 
  gap: 20px;
  z-index: 1000;
}

.sort-options label {
  color: white; 
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 20px; 
  margin-bottom: 30px;
  margin-left: 20px;
  color: #000;
}

.search-input,
.date-input,
.dropdown {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  color: #000;
}

.btn {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background-color: #2482ad;
  color: white;
}

.btn:hover {
  background-color: #0d0f88;
}

.todo-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.todo-table td span {
  margin-right: 10px; 
}

.todo-table th,
.todo-table td {
  padding: 10px;
  border: 1px solid #ddd;
}

.todo-table th {
  background-color: #5c9e88;
  color: white;
}

.edit-btn {
  background-color: #4caf50;
}

.edit-btn:hover {
  background-color: #45a049;
}

.delete-btn {
  background-color: #f44336;
}


.delete-btn:hover {
  background-color: #9e1614;
}


/* Csv Button*/
.export-btn {
  position: absolute;
  top: 50px; 
  right: 20px;
  background-color: #2482ad; 
  color: white;
  border: none;
  padding: 8px 16px;
  cursor: pointer;
  z-index: 1000;
  width: auto;   

}

.export-btn:hover {
  background-color: #0d0f88;
}


/* Modal Styles */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.modal-content {
  background-color: #222;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  color: white;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 20px;
  cursor: pointer;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
}

.form-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #444;
  border-radius: 4px;
  background-color: #333;
  color: white;
}

.search-assignees {
  margin-bottom: 10px;
}

.assignee-list {
  max-height: 150px;
  overflow-y: auto;
  border: 1px solid #444;
  border-radius: 4px;
  background-color: #333;
  padding: 8px;
}

.assignee-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.assignee-item input {
  margin-right: 10px;
}

.button-group {
  display: flex;
  justify-content: space-between;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-save {
  background-color: #4caf50;
  color: white;
}

.btn-save:hover {
  background-color: #45a049;
}

.btn-cancel {
  background-color: #f44336;
  color: white;
}

.btn-cancel:hover {
  background-color: #9e1614;
}
</style>


