<script setup lang="ts">
import { computed } from "vue";
import { RouterLink } from "vue-router";

// Import FontAwesome icons for UI elements
import { 
  faPlus,      // New todo icon
  faCheck,     // Completed todos icon
  faUsers,     // Team management icon
  faCalendar,  // Calendar/due date icon
  faList,      // Todo list icon
  faHistory    // Activity history icon
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

// Import state management for todos, assignees and activities
import { todos } from "@/ts/Todo";
import { assignees } from "@/ts/Assignee";
import { getActivities } from "@/ts/activity";

const statistics = computed(() => ({
  openTodos: todos.value.filter(todo => !todo.finished).length, // Count of open todos
  completedTodos: todos.value.filter(todo => todo.finished).length, // Count of completed todos
  teamMembers: assignees.value.length, // Total number of team members
  
  // Count todos due until end of current week
  dueThisWeek: todos.value.filter(todo => {
    if (!todo.dueDate) return false;
    
    const today = new Date();
    const weekEnd = new Date();
    
    // Calculate days until Sunday (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
    const daysUntilSunday = 7 - today.getDay();
    
    // Set date to next Sunday and time to end of day
    weekEnd.setDate(today.getDate() + daysUntilSunday);
    weekEnd.setHours(23, 59, 59, 999);

    const todoDueDate = new Date(todo.dueDate);
    
    return todoDueDate <= weekEnd && !todo.finished;
  }).length
}));

// Get activity feed data from store
const aktivitaeten = getActivities();
</script>

<template>
  <!-- Main dashboard container -->
  <main class="dashboard">
    <!-- Welcome section with dashboard title -->
    <div class="welcome-section">
      <h1>Willkommen im Todo-Management</h1>
      <p>Verwalten Sie Ihre Aufgaben und Teams effizient</p>
    </div>

    <!-- Quick access navigation cards -->
    <div class="quick-actions">
      <!-- Create new todo navigation card -->
      <RouterLink to="/createTodos" class="action-card">
        <div class="card-header">
          <FontAwesomeIcon :icon="faPlus" class="icon blue"/>
          <h2>Neue Todo erstellen</h2>
        </div>
        <p>Erstellen Sie schnell eine neue Aufgabe und weisen Sie sie zu</p>
      </RouterLink>

      <!-- Todo management navigation card -->
      <RouterLink to="/todos" class="action-card">
        <div class="card-header">
          <FontAwesomeIcon :icon="faList" class="icon green"/>
          <h2>Todos verwalten</h2>
        </div>
        <p>Übersicht und Verwaltung aller Aufgaben</p>
      </RouterLink>

      <!-- Team management navigation card -->
      <RouterLink to="/assignees" class="action-card">
        <div class="card-header">
          <FontAwesomeIcon :icon="faUsers" class="icon purple"/>
          <h2>Team verwalten</h2>
        </div>
        <p>Assignees hinzufügen und verwalten</p>
      </RouterLink>
    </div>

    <!-- Statistics overview section -->
    <div class="statistics">
      <!-- Open todos statistic card -->
      <div class="stat-card blue">
        <div class="stat-header">
          <h3>Offene Todos</h3>
          <FontAwesomeIcon :icon="faList"/>
        </div>
        <p class="stat-number">{{ statistics.openTodos }}</p>
      </div>

      <!-- Completed todos statistic card -->
      <div class="stat-card green">
        <div class="stat-header">
          <h3>Erledigte Todos</h3>
          <FontAwesomeIcon :icon="faCheck"/>
        </div>
        <p class="stat-number">{{ statistics.completedTodos }}</p>
      </div>

      <!-- Team members statistic card -->
      <div class="stat-card purple">
        <div class="stat-header">
          <h3>Team-Mitglieder</h3>
          <FontAwesomeIcon :icon="faUsers"/>
        </div>
        <p class="stat-number">{{ statistics.teamMembers }}</p>
      </div>

      <!-- Due this week statistic card -->
      <div class="stat-card yellow">
        <div class="stat-header">
          <h3>Diese Woche fällig</h3>
          <FontAwesomeIcon :icon="faCalendar"/>
        </div>
        <p class="stat-number">{{ statistics.dueThisWeek }}</p>
      </div>
    </div>

    <!-- Activity feed section -->
    <div class="activity-feed">
      <div class="feed-header">
        <h2>Letzte Aktivitäten</h2>
        <FontAwesomeIcon :icon="faHistory"/>
      </div>
      <!-- Activity items list with dynamic styling -->
      <div class="feed-items">
        <div v-for="(aktivitaet, index) in aktivitaeten" 
             :key="index"
             class="feed-item">
             <FontAwesomeIcon 
             :icon="aktivitaet.icon" 
             :class="{
              'green': aktivitaet.typ === 'abgeschlossen',  // Completed activity
              'blue': aktivitaet.typ === 'neu',             // New activity
              'purple': aktivitaet.typ === 'team',          // Team-related activity
              'red': aktivitaet.typ === 'delete' || aktivitaet.typ === 'team-delete',  // Deletion activity
              'orange': aktivitaet.typ === 'update' || aktivitaet.typ === 'team-update' // Update activity
              }"/>
          <span>{{ aktivitaet.text }}</span>
          <span class="time">{{ aktivitaet.zeit }}</span>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* Main dashboard container styles */
.dashboard {
  padding: 20px;
  background-color: #1a1a1a;  /* Dark theme background */
  color: #ffffff;             /* Light text for contrast */
}

/* Welcome section styles */
.welcome-section {
  margin-bottom: 30px;
}

.welcome-section h1 {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.welcome-section p {
  color: #888;  /* Subdued text color for description */
}

/* Quick action cards container */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Individual action card styling */
.action-card {
  background-color: #2d2d2d;
  padding: 20px;
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;  /* Smooth hover effects */
  margin-bottom: 20px; 
  color: #ffffff;
}

/* Hover effect for action cards */
.action-card:hover {
  transform: translateY(-2px);  /* Slight lift effect */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

/* Action card header styling */
.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h2 {
  margin-left: 15px;
  font-size: 1.25rem;
}

/* Statistics section container */
.statistics {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

/* Individual statistic card styling */
.stat-card {
  padding: 20px;
  border-radius: 8px;
  transition: transform 0.3s;
  color: #ffffff !important; 
}

/* Hover effect for statistic cards */
.stat-card:hover {
  transform: scale(1.02);
}

/* Statistic card header styling */
.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

/* Statistic number styling */
.stat-number {
  font-size: 2rem;
  font-weight: bold;
}

/* Activity feed container styling */
.activity-feed {
  background-color: #2d2d2d;
  padding: 20px;
  border-radius: 8px;
}

/* Activity feed header styling */
.feed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* Individual activity item styling */
.feed-item {
  display: flex;
  align-items: center;
  padding: 10px;
  transition: background-color 0.2s;
}

/* Hover effect for activity items */
.feed-item:hover {
  background-color: #363636;
  border-radius: 4px;
}

/* Activity item text spacing */
.feed-item span {
  margin-left: 10px;
}

/* Activity timestamp styling */
.feed-item .time {
  margin-left: auto;
  color: #888;
}

/* Color scheme for different elements */
.blue { color: #3b82f6; }    /* Primary blue */
.green { color: #22c55e; }   /* Success green */
.purple { color: #8b5cf6; }  /* Team purple */
.yellow { color: #eab308; }  /* Warning yellow */
.red { color: #ef4444; }     /* Error/delete red */
.orange { color: #f97316; }  /* Update orange */

/* Background colors for statistic cards */
.stat-card.blue { background-color: #1d4ed8; }
.stat-card.green { background-color: #15803d; }
.stat-card.purple { background-color: #7e22ce; }
.stat-card.yellow { background-color: #b45309; }

/* Animation for statistics numbers */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-number {
  animation: fadeIn 0.5s ease-out;
}
</style>