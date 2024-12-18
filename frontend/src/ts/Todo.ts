import type {Assignee} from "./Assignee";

export   

interface Todo {
    id: number;
    title: string;
    description: string;
    finished: boolean;
    assigneeList: Assignee[];
    createdDate: number;
    dueDate: number | null;
    finishedDate: Date | null;
}