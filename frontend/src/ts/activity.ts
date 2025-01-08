// Activity store for tracking user actions and changes in the application
// src/stores/activityStore.ts

// Import Vue's ref for reactive state management
import { ref } from 'vue';

// Import required FontAwesome icons for different activity types
import { 
    faPlus,      // For new/reopen activities
    faCheck,     // For completion activities 
    faUsers,     // For team-related activities
    faTrash,     // For deletion activities
    faPencil     // For update activities
} from '@fortawesome/free-solid-svg-icons';

// Interface defining the structure of an activity entry
export interface Activity {
    // Type of activity - determines how it will be displayed and processed
    typ: 'neu' | 'abgeschlossen' | 'wieder-offen' | 'team' | 'update' | 'delete' | 'team-update' | 'team-delete';
    text: string;    // Description of the activity
    zeit: string;    // Formatted timestamp of when the activity occurred
    icon: typeof faPlus | typeof faCheck | typeof faUsers | typeof faTrash | typeof faPencil;  // Associated icon
}

// Reactive array to store all activities
const activities = ref<Activity[]>([]);

// Helper function to format timestamps into relative time strings (e.g., "5 minutes ago")
function formatTime(date: Date): string {
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / 1000 / 60);
    
    // Format different time ranges appropriately
    if (diffInMinutes < 1) return 'Gerade eben';
    if (diffInMinutes < 60) return `Vor ${diffInMinutes} Minuten`;
    
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `Vor ${diffInHours} Stunden`;
    
    const diffInDays = Math.floor(diffInHours / 24);
    return `Vor ${diffInDays} Tagen`;
}

// Core function to add a new activity to the store
export function addActivity(activity: Omit<Activity, 'zeit'>) {
    // Add new activity at the beginning of the array with current timestamp
    activities.value.unshift({
        ...activity,
        zeit: formatTime(new Date())
    });

    // Limit the number of stored activities to prevent memory issues
    if (activities.value.length > 10) {
        activities.value = activities.value.slice(0, 10);
    }
}

// Todo-related activity creators
export function addTodoCreated(title: string) {
    addActivity({
        typ: 'neu',
        text: `Neues Todo "${title}" wurde erstellt`,
        icon: faPlus
    });
}

export function addTodoUpdated(title: string) {
    addActivity({
        typ: 'update',
        text: `Todo "${title}" wurde aktualisiert`,
        icon: faPencil
    });
}

export function addTodoDeleted(title: string) {
    addActivity({
        typ: 'delete',
        text: `Todo "${title}" wurde gelöscht`,
        icon: faTrash
    });
}

export function addTodoStatusChanged(title: string, isCompleted: boolean) {
    addActivity({
        typ: isCompleted ? 'abgeschlossen' : 'wieder-offen',
        text: isCompleted 
            ? `Todo "${title}" wurde abgeschlossen`
            : `Todo "${title}" wurde wieder geöffnet`,
        icon: isCompleted ? faCheck : faPlus
    });
}

// Assignee-related activity creators
export function addAssigneeAdded(prename: string, name: string) {
    addActivity({
        typ: 'team',
        text: `Neuer Assignee "${prename} ${name}" wurde hinzugefügt`,
        icon: faUsers
    });
}

export function addAssigneeUpdated(prename: string, name: string) {
    addActivity({
        typ: 'team-update',
        text: `Assignee "${prename} ${name}" wurde aktualisiert`,
        icon: faPencil
    });
}

export function addAssigneeDeleted(prename: string, name: string) {
    addActivity({
        typ: 'team-delete',
        text: `Assignee "${prename} ${name}" wurde entfernt`,
        icon: faTrash
    });
}

// Getter function to access the activities array
export function getActivities() {
    return activities;
}