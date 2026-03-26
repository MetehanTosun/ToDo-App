package de.unistuttgart.iste.ese.api.Services;

import de.unistuttgart.iste.ese.api.DTOs.TodoDTO;
import de.unistuttgart.iste.ese.api.Models.Assignee;
import de.unistuttgart.iste.ese.api.Models.ToDo;
import de.unistuttgart.iste.ese.api.Models.TodoModel;
import de.unistuttgart.iste.ese.api.Repositories.AssigneeRepository;
import de.unistuttgart.iste.ese.api.Repositories.ToDoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToDoService {
    @Autowired
    private ToDoRepository toDoRepository;

    @Autowired
    private AssigneeRepository assigneeRepository;

    @Autowired
    private TodoModel todoModel;

    /**
     * Retrieves all Todo items from the repository.
     *
     * @return List of all Todo items as DTOs
     */
    public List<TodoDTO> getAllToDos() {
        return ((List<ToDo>) toDoRepository.findAll()).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific Todo item by its ID.
     *
     * @param id The ID of the Todo item to retrieve
     * @return The found Todo item as DTO
     * @throws ResponseStatusException if Todo item is not found
     */
    public TodoDTO getToDoById(long id) {
        return convertToDTO(findToDoById(id));
    }

    /**
     * Creates a new Todo item. Processes assignees and predicts the category via AI model.
     *
     * @param todo The DTO containing Todo creation data
     * @return The newly created Todo item as DTO
     * @throws ResponseStatusException if validation fails
     */
    public TodoDTO createToDo(TodoDTO todo) {
        validateAssigneeIds(todo.getAssigneeIdList());
        List<Assignee> assignees = getAssignees(todo.getAssigneeIdList());
        Date dueDate = todo.getDueDate() != null ? new Date(todo.getDueDate()) : null;
        String category = todoModel.predictClass(todo.getTitle());

        ToDo toDo = new ToDo(
            todo.getTitle(),
            todo.getDescription(),
            todo.isFinished(),
            assignees,
            new Date(),
            dueDate,
            null,
            category
        );

        return convertToDTO(toDoRepository.save(toDo));
    }

    /**
     * Updates an existing Todo item. Updates the category prediction if the title changes.
     *
     * @param id   The ID of the Todo item to update
     * @param todo The DTO containing updated Todo data
     * @return The updated Todo item as DTO
     * @throws ResponseStatusException if Todo not found or validation fails
     */
    public TodoDTO updateToDo(long id, TodoDTO todo) {
        ToDo existingTodo = findToDoById(id);
        validateAssigneeIds(todo.getAssigneeIdList());
        List<Assignee> assignees = getAssignees(todo.getAssigneeIdList());
        String category = todoModel.predictClass(todo.getTitle());

        existingTodo.setTitle(todo.getTitle());
        existingTodo.setDescription(todo.getDescription());
        existingTodo.setAssigneeList(assignees);
        existingTodo.setCategory(category);
        if (todo.getDueDate() != null) {
            existingTodo.setDueDate(new Date(todo.getDueDate()));
        }
        existingTodo.setFinished(todo.isFinished());
        existingTodo.setFinishedDate(todo.isFinished() ? new Date() : null);

        return convertToDTO(toDoRepository.save(existingTodo));
    }

    /**
     * Deletes a Todo item by its ID.
     * Clears assignee relationships before deletion to maintain data consistency.
     *
     * @param id The ID of the Todo item to delete
     * @throws ResponseStatusException if Todo not found
     */
    public void deleteToDo(long id) {
        ToDo toDoToDelete = findToDoById(id);
        if (toDoToDelete.getAssigneeList() != null) {
            toDoToDelete.getAssigneeList().clear();
        }
        toDoRepository.save(toDoToDelete);
        toDoRepository.deleteById(id);
    }

    /**
     * Exports all Todo items to CSV format.
     *
     * @return ResponseEntity containing the CSV data and appropriate headers
     */
    public ResponseEntity<String> exportToCSV() {
        List<ToDo> allToDos = (List<ToDo>) toDoRepository.findAll();
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("id,title,description,finished,assignees,createdDate,dueDate,finishedDate,category\n");
        for (ToDo todo : allToDos) {
            appendTodoToCSV(csvContent, todo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=todos.csv");

        return new ResponseEntity<>(csvContent.toString(), headers, HttpStatus.OK);
    }

    /**
     * Converts a Todo entity to a TodoDTO for API responses.
     *
     * @param todo The Todo entity to convert
     * @return The Todo data as DTO
     */
    private TodoDTO convertToDTO(ToDo todo) {
        return new TodoDTO(
            todo.getId(),
            todo.getTitle(),
            todo.getDescription(),
            todo.isFinished(),
            null,
            new ArrayList<>(todo.getAssigneeList()),
            todo.getCreatedDate().getTime(),
            todo.getDueDate() != null ? todo.getDueDate().getTime() : null,
            todo.getFinishedDate() != null ? todo.getFinishedDate().getTime() : null,
            todo.getCategory()
        );
    }

    /**
     * Finds a Todo entity by ID or throws a 404 exception.
     *
     * @param id The ID to look up
     * @return The found Todo entity
     * @throws ResponseStatusException if not found
     */
    private ToDo findToDoById(long id) {
        ToDo todo = toDoRepository.findById(id);
        if (todo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("ToDo with ID %d not found", id));
        }
        return todo;
    }

    /**
     * Validates that there are no duplicate assignee IDs.
     *
     * @param assigneeIds List of assignee IDs to validate
     * @throws ResponseStatusException if duplicates are found
     */
    private void validateAssigneeIds(List<Long> assigneeIds) {
        if (assigneeIds != null && new HashSet<>(assigneeIds).size() < assigneeIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Duplicate assignee IDs are not allowed");
        }
    }

    /**
     * Retrieves Assignee objects by their IDs.
     *
     * @param assigneeIds List of assignee IDs to retrieve
     * @return List of found Assignee objects
     * @throws ResponseStatusException if any assignee is not found
     */
    private List<Assignee> getAssignees(List<Long> assigneeIds) {
        if (assigneeIds == null) return new ArrayList<>();
        return assigneeIds.stream()
            .map(id -> assigneeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Assignee not found")))
            .collect(Collectors.toList());
    }

    /**
     * Appends a single Todo item's data to the CSV content.
     *
     * @param csvContent StringBuilder to append the Todo data to
     * @param todo       The Todo item to append
     */
    private void appendTodoToCSV(StringBuilder csvContent, ToDo todo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        csvContent.append(todo.getId()).append(",")
            .append(skipCSV(todo.getTitle())).append(",")
            .append(skipCSV(todo.getDescription())).append(",")
            .append(todo.isFinished()).append(",");

        if (!todo.getAssigneeList().isEmpty()) {
            String assignees = todo.getAssigneeList().stream()
                .map(a -> a.getPreName() + " " + a.getName())
                .collect(Collectors.joining("+"));
            csvContent.append(skipCSV(assignees));
        }
        csvContent.append(",");

        csvContent.append(dateFormat.format(todo.getCreatedDate())).append(",")
            .append(todo.getDueDate() != null ? dateFormat.format(todo.getDueDate()) : "").append(",")
            .append(todo.getFinishedDate() != null ? dateFormat.format(todo.getFinishedDate()) : "").append(",")
            .append(skipCSV(todo.getCategory())).append("\n");
    }

    /**
     * Escapes a CSV field if it contains commas or quotes.
     *
     * @param field The string field to escape
     * @return The properly escaped CSV field
     */
    private String skipCSV(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
