<script setup lang="ts">
import config from "@/config";
import { showToast, Toast } from "@/ts/toasts";
import { faCheck, faXmark } from "@fortawesome/free-solid-svg-icons";
import { onMounted, ref } from "vue";
import { fetchAllAssignees, searchAssignee, filteredAssignees} from "@/ts/Assignee";
import type { Todo } from "@/ts/Todo";
import { addTodoCreated } from "@/ts/activity";

// Reactive state for Todos 
const todos = ref<Todo[]>([]); // List of todos

// Reactive variables for creating a new Todo
const selectedAssigneeIds = ref<number[]>([]); 
const title = ref('');
const description = ref('');
const finished = ref(false); 
const createdDate = ref('');
const dueDate = ref('');
const finishedDate = ref('');

// Fetch assignees on component mount
onMounted(() => fetchAllAssignees());

// Create a new Todo 
function createTodo() {
  fetch(`${config.apiBaseUrl}/todos`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      title: title.value,
      description: description.value,
      finished: finished.value,
      assigneeIdList: selectedAssigneeIds.value,
      createdDate: createdDate.value ? new Date(createdDate.value).getTime() : null, // Convert to Unix timestamp
      dueDate: dueDate.value ? new Date(dueDate.value).getTime() : null, // Convert to Unix timestamp
      finishedDate: finishedDate.value ? new Date(finishedDate.value).getTime() : null // Convert to Unix timestamp
    })
  })
    .then(response => {
      if (!response.ok) {
        throw new Error("Todo konnte nicht erstellt werden");
      }
      return response.json();
    })
    .then(data => {
      const createdTodo = data as Todo;
      todos.value.push(createdTodo); // Add the created todo to the list
    })
    .then(() => {
      showToast(new Toast("Erfolg", `Todo "${title.value}" wurde erfolgreich erstellt`, "success", faCheck, 5)); 
    })
    .catch(error => {
      showToast(new Toast("Fehler", error.message, "error", faXmark, 5)); 
    });
    
    addTodoCreated(title.value);
}
</script>

<template>
  <div class="todo-form">
    <!-- Title input field -->
    <div class="form-group">
      <label>
        Titel:
        <input
          type="text"
          id="title"
          v-model="title"
          placeholder="Titel eingeben"
          class="form-control"
          name="title"
          autocomplete="off"/>
      </label>
    </div>

    <!-- Description textarea -->
    <div class="form-group">
      <label>
        Beschreibung:
        <textarea
          id="description"
          v-model="description"
          placeholder="Beschreibung eingeben"
          class="form-control"
          name="description"
          autocomplete="off"
        ></textarea>
      </label>
    </div>

    <!-- Assignees selection with search functionality -->
    <div class="form-group">
      <label>
        Zugewiesene Personen:
        <!-- Search bar for filtering assignees -->
        <input
          type="text"
          v-model="searchAssignee"
          placeholder="Personen suchen..."
          class="search-bar"
          name="searchQuery"
          autocomplete="off"/>
      </label>

      <!-- Scrollable list of assignees with checkboxes -->
      <div class="scrollable-list">
        <div
          class="checkbox-item"
          v-for="assignee in filteredAssignees"
          :key="assignee.id"
        >
          <input
            type="checkbox"
            :id="'assignee-' + assignee.id"
            :value="assignee.id"
            v-model="selectedAssigneeIds"/>
          <label :for="'assignee-' + assignee.id">
            {{ assignee.prename }} {{ assignee.name }}
          </label>
        </div>
      </div>
    </div>

    <!-- Due date input field -->
    <div class="form-group">
      <label>
        Fälligkeitsdatum:
        <input
          type="date"
          id="dueDate"
          v-model="dueDate"
          class="form-control"
          name="dueDate"
          autocomplete="off"
        />
      </label>
    </div>

    <!-- Submit button -->
    <div class="form-group">
      <button @click="createTodo" class="btn btn-create">ToDo erstellen</button>
    </div>
  </div>
</template>

<style scoped>

.search-bar {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 4px;
  color: #000;
}
.todo-form {
  width: 400px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #42b983;
  border-radius: 10px;
  background-color: #222;
  color: #fff;
}

.form-group input,
.form-group textarea {
  color: black; 
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
  font-size: 14px;
}

input,
textarea,
button {
  width: 100%;
  padding: 8px;
  margin-top: 5px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

textarea {
  resize: none;
  height: 60px;
}

button.btn {
  background-color: #42b983;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 16px;
}

button.btn:hover {
  background-color: #358b61;
}

.scrollable-list {
  max-height: 120px;
  overflow-y: auto;
  background-color: #333;
  border: 1px solid #444;
  padding: 8px;
  border-radius: 4px;
}

.checkbox-item {
  display: flex; 
  align-items: center; 
  margin-bottom: 6px;
  margin-left: 0px;
}

.checkbox-item input {
  margin: 0; 
  padding: 0;
  width: auto; 
  height: auto;
}

.checkbox-item label {
  margin: 0; 
  font-size: 14px;
}
</style>





