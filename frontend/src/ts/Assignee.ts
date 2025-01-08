import config from "@/config";
import { ref, type Ref, computed} from "vue";
import { showToast, Toast } from "@/ts/toasts";
import { faXmark } from "@fortawesome/free-solid-svg-icons";

export

interface Assignee {
    id: number;
    name: string;
    prename: string;
    email: string;
  }
  export const assignees: Ref<Assignee[]> = ref([]);
  export const searchAssignee = ref("");

  export function fetchAllAssignees() {
    fetch(`${config.apiBaseUrl}/assignees`)
      .then((response) => response.json())
      .then((data) => {
        assignees.value = data as Assignee[];
      })
      .catch((error) =>
        showToast(new Toast("Error", error.message, "error", faXmark, 10))
      );
  }

  // Computed property for filtered assignees based on the search query
  export const filteredAssignees = computed(() => {
    return assignees.value.filter(assignee => {
      const fullName = `${assignee.prename} ${assignee.name}`.toLowerCase();
      return fullName.includes(searchAssignee.value.toLowerCase());
    });
  });
