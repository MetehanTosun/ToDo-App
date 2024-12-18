<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import config from "@/config";
import { showToast, Toast } from "@/ts/toasts";
import { faXmark, faCheck } from "@fortawesome/free-solid-svg-icons"; // Import icons for toast notifications
import type { Assignee } from "@/ts/Assignee";
import type { Todo } from "@/ts/Todo";

// Reactive references for Todos and Assignees
const todos = ref<Todo[]>([]); // Holds all Todos
const assignees = ref<Assignee[]>([]); // Holds all available Assignees

// Search and filter states
const searchQuery = ref(""); // Search term for Todos
const todo = ref<Todo | null>(null); // A single Todo based on the search query
const filterTitle = ref(""); // Filter criteria for title
const filterDueDate = ref<string | null>(null); // Filter criteria for due date
const showCompletedTodos = ref(false); // Flag to display completed Todos

// States for the edit modal
const showEditModal = ref(false); // Shows or hides the edit modal
const currentTodo = ref<Todo | null>(null); // The currently edited Todo
const editTitle = ref(""); // Title of the Todo in the modal
const editDescription = ref(""); // Description of the Todo in the modal
const editDueDate = ref<string | null>(""); // Due date of the Todo in the modal
const selectedAssignees = ref<Assignee[]>([]); // Selected assignees in the modal

// Sorting options
const sortKey = ref<"title" | "dueDate" | null>(null); // Sorting key
const sortOrder = ref(1); // Sorting order: 1 = ascending, -1 = descending

// Fetch all Todos 
function fetchTodos() {
  fetch(`${config.apiBaseUrl}/todos`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => {
      if (!response.ok) throw new Error("Failed to fetch todos");
      return response.json();
    })
    .then((data) => {
      todos.value = data as Todo[];
    })
    .catch((error) => {
      console.error(error);
      showToast(new Toast("Error", error.message, "error", faXmark, 5));
    });
}

// Search for a Todo by name
function fetchTodoByName() {
  const query = searchQuery.value.trim();
  if (query === "") {
    todo.value = null; // Reset if no search string is entered
    return;
  }
  const matchedTodo = todos.value.find((todo) =>
    todo.title.toLowerCase().includes(query.toLowerCase())
  );
  if (!matchedTodo) {
    showToast(new Toast("Info", "Todo nicht gefunden", "info", faXmark, 5));
    todo.value = null;
    return;
  }
  // Fetch Todo details by ID
  fetch(`${config.apiBaseUrl}/todos/${matchedTodo.id}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to fetch todo");
      }
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

// Fetch all assignees 
function fetchAllAssignees() {
  fetch(`${config.apiBaseUrl}/assignees`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => response.json())
    .then((data) => {
      assignees.value = data as Assignee[];
    })
    .catch((error) => {
      showToast(new Toast("Error", error.message, "error", faXmark, 10));
    });
}

// Delete a Todo
function deleteTodo(id: number) {
  fetch(`${config.apiBaseUrl}/todos/${id}`, { method: "DELETE" })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to delete todo");
      }
      todos.value = todos.value.filter((todo) => todo.id !== id);
      showToast(new Toast("Erfolg!", "Todo erfolgreich gelöscht!", "success", faCheck, 5));
    })
    .catch((error) =>
      showToast(new Toast("Error", error.message, "error", faXmark, 5))
    );
}

// Computed property for filtered and sorted Todos
const filteredTodos = computed(() => {
  let filtered = todos.value;

  // Filter by title
  if (filterTitle.value) {
    filtered = filtered.filter((todo) =>
      todo.title.toLowerCase().includes(filterTitle.value.toLowerCase())
    );
  }

  // Filter by due date
  if (filterDueDate.value) {
    const dueDate = new Date(filterDueDate.value).getTime();
    filtered = filtered.filter((todo) => todo.dueDate === dueDate);
  }

  // Show only completed Todos
  if (showCompletedTodos.value) {
    filtered = filtered.filter((todo) => todo.finished && todo.finishedDate);
  }

  // Sort Todos
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


function updateTodo() {
  // Ensure a Todo is selected for updating
  if (!currentTodo.value) {
    showToast(
      new Toast("Error","Kein Todo zum Aktualisieren ausgewählt","error",faXmark,5)
    );
    return;
  }

  const updatedTodo: any = {};

  // Collect changes to the Todo
  if (editTitle.value && editTitle.value !== currentTodo.value.title) {
    updatedTodo.title = editTitle.value;
  } else {
    updatedTodo.title = currentTodo.value.title;
  }

  if (
    editDescription.value &&
    editDescription.value !== currentTodo.value.description
  ) {
    updatedTodo.description = editDescription.value;
  } else {
    updatedTodo.description = currentTodo.value.description;
  }

  updatedTodo.finished = currentTodo.value.finished; // Keep the existing completion status, if u do not change it

  if (
    editDueDate.value &&
    new Date(editDueDate.value).getTime() !== currentTodo.value.dueDate
  ) {
    updatedTodo.dueDate = new Date(editDueDate.value).getTime();
  } else {
    updatedTodo.dueDate = currentTodo.value.dueDate;
  }

  if (
    selectedAssignees.value &&
    JSON.stringify(selectedAssignees.value.map((a) => a.id)) !==
      JSON.stringify(currentTodo.value.assigneeList.map((a) => a.id))
  ) {
    updatedTodo.assigneeIdList = selectedAssignees.value.map((a) => a.id);
  } else {
    updatedTodo.assigneeIdList = currentTodo.value.assigneeList.map(
      (a) => a.id
    );
  }

  console.log("Updated Todo Payload:", updatedTodo);

  // API call to update the Todo
  fetch(`${config.apiBaseUrl}/todos/${currentTodo.value.id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(updatedTodo),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Fehler beim Aktualisieren des Todos");
      }
      return response.json();
    })
    .then((data) => {
      // Update the local list of Todos
      const index = todos.value.findIndex(
        (todo) => todo.id === currentTodo.value?.id
      );
      if (index !== -1) {
        todos.value[index] = { ...todos.value[index], ...data };
      }
      showToast(
        new Toast("Erfolg!","Todo erfolgreich aktualisiert!","success",faCheck,5)
      );
      showEditModal.value = false;

      // Reset form fields
      editTitle.value = "";
      editDescription.value = "";
      editDueDate.value = null;
      selectedAssignees.value = [];
      currentTodo.value = null;
    })
    .catch((error) => {
      console.error("Fehler beim Aktualisieren:", error);
      showToast(new Toast("Error", error.message, "error", faXmark, 5));
    });
}

function updateFinishedStatus(todo: Todo): Promise<void> {
  return new Promise((resolve, reject) => {
    // Find the current Todo in the list
    const currentTodoData = todos.value.find((t) => t.id === todo.id);

    if (!currentTodoData) {
      showToast(
        new Toast("Error", "Todo nicht gefunden", "error", faXmark, 5)
      );
      reject(new Error("Todo nicht gefunden"));
      return;
    }

    const updatedTodo: any = {
      title: currentTodoData.title,
      description: currentTodoData.description,
      finished: todo.finished,
      assigneeIdList: currentTodoData.assigneeList.map((a) => a.id),
      dueDate: currentTodoData.dueDate,
    };

    // API call to update the completion status
    fetch(`${config.apiBaseUrl}/todos/${todo.id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(updatedTodo),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Fehler beim Aktualisieren des Todos");
        }
        return response.json();
      })
      .then(() => {
        const index = todos.value.findIndex((t) => t.id === todo.id);
        if (index !== -1) {
          todos.value[index] = {
            ...todos.value[index],
            finished: todo.finished,
            finishedDate: todo.finished ? new Date() : null,
          };
        }
        showToast(
          new Toast("Erfolg","Todo Status erfolgreich aktualisiert!","success",faCheck,5)
        );
        resolve();
      })
      .catch((error) => {
        console.error("Fehler beim Aktualisieren:", error);
        showToast(new Toast("Error", error.message, "error", faXmark, 5));
        reject(error);
      });
  });
}

function toggleFinished(todo: Todo) {
  todo.finished = !todo.finished; // Toggle completion status locally
  todo.finishedDate = todo.finished ? new Date() : null; // Update finishedDate

  // Update the status via API
  updateFinishedStatus(todo)
    .then(() => {
      // Optionally reload Todos if the backend might be unreliable
      fetchTodos();
    })
    .catch((error) => {
      console.error("Fehler beim Aktualisieren:", error);
    });
}

function openEditModal(todo: Todo) {
  currentTodo.value = todo;
  editTitle.value = todo.title;
  editDescription.value = todo.description;
  editDueDate.value = todo.dueDate
    ? new Date(todo.dueDate).toLocaleDateString()
    : null;
  selectedAssignees.value = [...todo.assigneeList]; // Load assignees into the modal
  showEditModal.value = true;
}

// Computed property for filtered assignees
const filteredAssignees = computed(() => {
  return assignees.value.filter((assignee) => {
    const fullName = `${assignee.prename} ${assignee.name}`.toLowerCase();
    return fullName.includes(searchQuery.value.toLowerCase());
  });
});

// Fetch Todos and assignees on component mount
onMounted(() => {
  fetchTodos();
  fetchAllAssignees();
});

</script>

<template>
  <div class="todo-view">
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
      <label for="sortKey">Sort by:</label>
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
            v-model="searchQuery"
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
  padding: 20px;
}

.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.sort-options label {
  color: white; 
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
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


