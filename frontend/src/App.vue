<script setup lang="ts">
// Import core Vue Router functionality
import { RouterView } from 'vue-router';

// Import UI components from agnostic-vue library
import { Close, Toast, Toasts } from 'agnostic-vue';

// Import toast state management
import { activeToasts } from '@/ts/toasts';

// Import required styles from agnostic-vue
import 'agnostic-vue/dist/common.min.css';
import 'agnostic-vue/dist/index.css';
</script>

<template>
  <!-- Main application container -->
  <div id="app">
    <!-- Navigation header -->
    <nav class="custom-header">
      <div class="header-nav">
        <!-- Navigation links using Vue Router -->
        <RouterLink to="/" class="nav-link">Home</RouterLink>
        <RouterLink to="/createTodos" class="nav-link">Todo erstellen</RouterLink>
        <RouterLink to="/assignees" class="nav-link">Assignees</RouterLink>
        <RouterLink to="/todos" class="nav-link">Todos</RouterLink>
      </div>
    </nav>

    <!-- Main content area -->
    <div class="main">
      <RouterView />
    </div>
  </div>

  <!-- Toast notification system -->
  <Toasts vertical-position="top" horizontal-position="end">
    <template v-for="toast of activeToasts" :key="toast.key">
      <!-- Individual toast notification -->
      <Toast :type="toast.type" class="alert alert-border-left alert-info">
        <div class="flex-fill flex flex-column">
          <!-- Toast header -->
          <div class="flex">
            <h3 class="flex-fill">{{ toast.title }}</h3>
            <Close @click="toast.close()" />
          </div>
          <!-- Toast content -->
          <div class="flex">
            <font-awesome-icon :icon="toast.icon" size="xl" class="mie8 pbs2"></font-awesome-icon>
            <div class="flex-fill">
              {{ toast.message }}
            </div>
          </div>
        </div>
      </Toast>
      <!-- Spacing between multiple toasts -->
      <div class="mbe14" />
    </template>
  </Toasts>
</template>

<style>
/* Header styling */
.custom-header {
  background-color: #1e1e1e;
  border-bottom: 1px solid #333;
  padding: 0.5rem 2rem;
}

/* Navigation container styling */
.header-nav {
  display: flex;
  gap: 1rem;
  justify-content: flex-start;
  width: 100%;
}

/* Navigation link styling */
.nav-link {
  color: #888;
  text-decoration: none;
  padding: 0.5rem 1rem;
  transition: color 0.3s ease;
  font-weight: 500;
  position: relative;
}

/* Navigation link hover effect */
.nav-link:hover {
  color: #fff;
}

/* Active navigation link styling */
.nav-link.router-link-active {
  color: #3b82f6;
}

/* Active navigation link underline indicator */
.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #3b82f6;
  border-radius: 2px;
}

/* Main content area styling */
.main {
  padding: 20px;
}
</style>