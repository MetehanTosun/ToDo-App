<script setup lang="ts">
import config from "@/config";
import { showToast, Toast } from "@/ts/toasts";
import { faCheck, faXmark } from "@fortawesome/free-solid-svg-icons";
import { onMounted, ref, type Ref } from "vue";
import { type Assignee, assignees, fetchAllAssignees } from "@/ts/Assignee";
import { addAssigneeAdded, addAssigneeUpdated, addAssigneeDeleted } from "@/ts/activity";

// Initialize reactive state variables for form management
const prename = ref('');           // Store first name input
const name = ref('');              // Store last name input
const email = ref('');             // Store email input
const searchQuery = ref('');       // Store search query
const selectedAssignee: Ref<Assignee | null> = ref(null);  // Store currently selected assignee for editing

// Function to search and fetch specific assignee
function fetchAssignee() {
  if (searchQuery.value.trim() === '') {
    fetchAllAssignees();  // If search is empty, fetch all assignees
    return;
  }

  // API call to fetch specific assignee
  fetch(`${config.apiBaseUrl}/assignees/${searchQuery.value}`)
    .then((response) => response.json())
    .then((data) => {
      assignees.value = [data as Assignee];
    })
    .catch((error) =>
      showToast(new Toast("Error", error.message, "error", faXmark, 10))
    );
}

// Function to create new assignee
function createAssignee() {
  fetch(`${config.apiBaseUrl}/assignees`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ prename: prename.value, name: name.value, email: email.value }),
  })
    .then((response) => {
      if (!response.ok) throw new Error("Assignee konnte nicht erstellt werden");
      return response.json();
    })
    .then((data) => {
      // Update local state and show success message
      assignees.value.push(data as Assignee);
      showToast(new Toast("Erfolg!", `Erfolgreich Assignee erstellt mit dem Namen: ${prename.value} ${name.value}`, "success", faCheck, 5));
      // Reset form fields
      prename.value = '';
      name.value = '';
      email.value = '';
    })
    .catch((error) =>
      showToast(new Toast("Error", error.message, "error", faXmark, 5))
    );
    addAssigneeAdded(prename.value, name.value);
}

// Function to update existing assignee
function updateAssignee(id: number) {
  // Create object with only changed fields
  const updatedAssignee: any = {};
  if (prename.value) updatedAssignee.prename = prename.value;
  if (name.value) updatedAssignee.name = name.value;
  if (email.value) updatedAssignee.email = email.value;

  // API call to update assignee
  fetch(`${config.apiBaseUrl}/assignees/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updatedAssignee),
  })
    .then(response => {
      if (!response.ok) throw new Error('Assignee konnte nicht aktualisiert werden');
      return response.json();
    })
    .then((data) => {
      // Update local state and show success message
      const index = assignees.value.findIndex(assignee => assignee.id === id);
      if (index !== -1) assignees.value[index] = data;
      showToast(new Toast("Erfolg", `Assignee "${updatedAssignee.prename} ${updatedAssignee.name}" wurde aktualisiert`, "success", faCheck, 5));

      // Record activity
      const updatedPrename = data.prename || prename.value;
      const updatedName = data.name || name.value;
      addAssigneeUpdated(updatedPrename, updatedName);

      // Reset form and selection
      prename.value = '';
      name.value = '';
      email.value = '';
      selectedAssignee.value = null;
    })
    .catch((error) => {
      showToast(new Toast("Error", error.message, "error", faXmark, 5));
    });
}

// Function to delete assignee
function deleteAssignee(id: number) {
  // Store assignee details before deletion for activity logging
  const assigneeToDelete = assignees.value.find(assignee => assignee.id === id);
  const assigneePrename = assigneeToDelete?.prename || "Unbekannt";
  const assigneeName = assigneeToDelete?.name || "Unbekannt";

  // API call to delete assignee
  fetch(`${config.apiBaseUrl}/assignees/${id}`, { method: "DELETE" })
    .then((response) => {
      if (!response.ok) throw new Error("Assignee konnte nicht gelöscht werden");
      // Update local state and show success message
      assignees.value = assignees.value.filter((a) => a.id !== id);
      showToast(new Toast("Erfolg", `Assignee "${assigneePrename} ${assigneeName}" wurde gelöscht`, "success", faCheck, 5));
    })
    .catch((error) =>
      showToast(new Toast("Error", error.message, "error", faXmark, 5))
    );
    
    // Record deletion activity
    addAssigneeDeleted(assigneePrename, assigneeName);
}

// Function to select assignee for editing
function selectAssignee(assignee: Assignee) {
  selectedAssignee.value = assignee;
  // Populate form fields with selected assignee's data
  prename.value = assignee.prename;
  name.value = assignee.name;
  email.value = assignee.email;
}

// Initialize component by fetching all assignees
onMounted(() => fetchAllAssignees());
</script>


<template>
  <div class="container">
    <!-- Assignee Creation Section -->
    <div class="assigneeBox">
      <h2 class="login-header">Neuer Assignee</h2>
      <div class="input-group">
        <label for="prename">Vorname</label>
        <input
          type="text"
          id="prename"
          v-model="prename"
          required
          name="prename"
        />
      </div>

      <div class="input-group">
        <label for="name">Name</label>
        <input
          type="text"
          id="name"
          v-model="name"
          required
          name="name"/>
      </div>

      <div class="input-group">
        <label for="email">Email</label>
        <input
          type="email"
          id="email"
          v-model="email"
          required
          name="email"/>
      </div>

      <button @click="createAssignee" class="btn create-btn">Erstellen</button>
    </div>

    <!-- Assignees Display Section -->
    <div class="table-section">
      <div class="search-bar">
        <!-- Search Input for Filtering Assignees -->
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Suchen"
          @input="fetchAssignee"
          autocomplete="off"
          id="searchQuery"
          name="searchQuery"/>
      </div>

      <div class="table-container">
        <!-- Table to Display Assignees -->
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Vorname</th>
              <th>Name</th>
              <th>Email</th>
              <th>Aktionen</th>
            </tr>
          </thead>
          <tbody>
            <!-- Loop through each assignee and display their details -->
            <tr v-for="assignee in assignees" :key="assignee.id">
              <td>{{ assignee.id }}</td>
              <td>{{ assignee.prename }}</td>
              <td>{{ assignee.name }}</td>
              <td>{{ assignee.email }}</td>
              <td>
                <!-- Edit and Delete Buttons -->
                <button @click="selectAssignee(assignee)" class="btn edit-btn">Bearbeiten</button>
                <button @click="deleteAssignee(assignee.id)" class="btn delete-btn">Löschen</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal for Editing Assignee -->
    <div v-if="selectedAssignee" class="modal">
      <div class="modal-content">
        <h2>Assignee Bearbeiten</h2>
        <div class="input-group">
          <label for="edit-prename">Vorname</label>
          <input
            type="text"
            id="edit-prename"
            v-model="prename"
            autocomplete="given-name"/>
        </div>
        
        <div class="input-group">
          <label for="edit-name">Name</label>
          <input
            type="text"
            id="edit-name"
            v-model="name"
            autocomplete="family-name"/>
        </div>

        <div class="input-group">
          <label for="edit-email">Email</label>
          <input
            type="email"
            id="edit-email"
            v-model="email"
            autocomplete="email"/>
        </div>

        <div class="modal-buttons">
          <button @click="updateAssignee(selectedAssignee.id)" class="btn update-btn" :disabled="!selectedAssignee">Aktualisieren</button>
          <button @click="selectedAssignee = null" class="btn cancel-btn">Abbrechen</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  justify-content: space-between;
  padding: 20px;
  color: #fff;
}

.assigneeBox {
  padding: 15px;
  margin: 3px;
  border: 1px solid #42b983;
  background-color: #121212;
  min-width: 280px;
  border-radius: 8px;
}

.input-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.input-group label {
  font-weight: bold;
}

.input-group input{
  color: #000;
}

.btn {
  padding: 10px;
  background-color: #1a73e8;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.create-btn {
  background-color: #42b983;
}

.create-btn:hover {
  background-color: #358b61;
}

.edit-btn {
  background-color: #34a853;
}

.edit-btn:hover {
  background-color: #2c8a44;
}

.delete-btn {
  background-color: #f44336;
}

.delete-btn:hover {
  background-color: #9e1614;
}

.update-btn {
  background-color: #4caf50;
}

.update-btn:hover {
  background-color: #45a049;
}

.cancel-btn {
  background-color: #d32f2f;
}

.cancel-btn:hover {
  background-color: #c62828;
}

.search-bar {
  width: 150px; 
  margin-left: 20px;
  margin-right: 20px; 
  margin-top: 20px; 
  margin-bottom: 50px; 
}

.search-bar input {
  padding: 8px;
  border-radius: 5px;
  border: 1px solid #ddd;
  width: 100%;
  color: #000; 
}

.table-section {
  display: flex;
  width: 100%;
}

.table-container {
  flex-grow: 1;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 10px;
  text-align: left;
}

th {
  background-color: #333;
}

tr:nth-child(even) {
  background-color: #444;
}

tr:hover {
  background-color: #555;
}

/* Modal Styles */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.modal-content {
  background-color: #2c2c2c;
  padding: 20px;
  border-radius: 8px;
  min-width: 300px;
}

.modal input, .modal select, .modal textarea {
  color: #000; 
}

.create-box input {
  color: #000; 
}

.modal-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
}
</style>







