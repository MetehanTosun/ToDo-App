import { createRouter, createWebHashHistory  } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import CreateToDoView from "@/views/CreateToDoView.vue";
import AssigneeView from "@/views/AssigneeView.vue";
import ToDoView from '@/views/ToDoView.vue';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/createTodos',
      name: 'createTodo',
      component: CreateToDoView
    },
    {
      path: '/assignees',
      name: 'assignee',
      component: AssigneeView
    },
    {
      path: '/todos',
      name: 'todo',
      component: ToDoView
    }
  ]
})

export default router
