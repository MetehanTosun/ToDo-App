import type {Assignee} from "./Assignee";
import config from "@/config";
import { ref } from "vue";
import { showToast, Toast } from "@/ts/toasts";
import { faXmark, faCheck } from "@fortawesome/free-solid-svg-icons";
import { addTodoUpdated, addTodoDeleted } from "./activity";

// Interface definition for Todo
export interface Todo {
    id: number;
    title: string;
    description: string;
    finished: boolean;
    assigneeList: Assignee[];
    createdDate: number;
    dueDate: number | null;
    finishedDate: Date | null;
    category: string
}

// State management
export const todos = ref<Todo[]>([]);
export const showEditModal = ref(false);
export const currentTodo = ref<Todo | null>(null);
export const editTitle = ref("");
export const editDescription = ref("");
export const editDueDate = ref<string | null>("");
export const selectedAssignees = ref<Assignee[]>([]);

// Fetch all todos from API
export function fetchTodos() {
  fetch(`${config.apiBaseUrl}/todos`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => {
      if (!response.ok) throw new Error("Abrufen der Todos fehlgeschlagen");
      return response.json();
    })
    .then((data: Todo[]) => {
      todos.value = data;
    })
    .catch((error) => {
      console.error(error);
      showToast(new Toast("Error", error.message, "error", faXmark, 5));
    });
}

// Delete a todo by ID
export function deleteTodo(id: number) {
   // Find todo to be deleted for activity logging
  const todoToDelete = todos.value.find(todo => todo.id === id);
  const todoTitle = todoToDelete?.title || "Unbekannt";

  return fetch(`${config.apiBaseUrl}/todos/${id}`, { 
    method: "DELETE" 
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`Das Todo ${todoTitle} konnte nicht gelöscht werden`);
      }
      todos.value = todos.value.filter((todo) => todo.id !== id);
      showToast(new Toast("Erfolg!", `Das Todo "${todoTitle}" wurde erfolgreich gelöscht.`, "success", faCheck, 5));
      // Log the deletion activity
      addTodoDeleted(todoTitle);
    })
    .catch((error) => {
      showToast(new Toast("Error", error.message, "error", faXmark, 5));
      throw error;
    });
}

// Reset edit form fields
function resetEditForm() {
  editTitle.value = "";
  editDescription.value = "";
  editDueDate.value = null;
  selectedAssignees.value = [];
  currentTodo.value = null;
  showEditModal.value = false;
}

// Prepare todo data for update
function prepareTodoUpdate(currentTodo: Todo): any {
  return {
    title: editTitle.value || currentTodo.title,
    description: editDescription.value || currentTodo.description,
    finished: currentTodo.finished,
    dueDate: editDueDate.value ? new Date(editDueDate.value).getTime() : currentTodo.dueDate,
    assigneeIdList: selectedAssignees.value.length > 0 
      ? selectedAssignees.value.map(a => a.id)
      : currentTodo.assigneeList.map(a => a.id)
  };
}

// Update a todo
export function updateTodo() {
  if (!currentTodo.value) {
    showToast(new Toast("Error", "Kein Todo zum Aktualisieren ausgewählt", "error", faXmark, 5));
    return;
  }

  const updatedTodo = prepareTodoUpdate(currentTodo.value);

  return fetch(`${config.apiBaseUrl}/todos/${currentTodo.value.id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(updatedTodo)
  })
    .then(response => {
      if (!response.ok) throw new Error(`Das Todo "${currentTodo.value!.title}" konnte nicht aktualisiert werden.`);
      return response.json();
    })
    .then(data => {
      // Update the todo in the local state
      const index = todos.value.findIndex(todo => todo.id === currentTodo.value?.id);
      if (index !== -1) {
        todos.value[index] = { ...todos.value[index], ...data };
      }
      
      showToast(new Toast("Erfolg", `Das Todo "${currentTodo.value!.title}" wurde erfolgreich aktualisiert.`, "success", faCheck, 5));
      
      // Log the update activity
      if (currentTodo.value) {
        addTodoUpdated(currentTodo.value.title);
      }

      resetEditForm();
    })
    .catch(error => {
      console.error("Fehler beim Aktualisieren:", error);
      showToast(new Toast("Error", error.message, "error", faXmark, 5));
      throw error;
    });
}