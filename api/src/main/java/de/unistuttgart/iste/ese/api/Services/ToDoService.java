package de.unistuttgart.iste.ese.api.Services;

import de.unistuttgart.iste.ese.api.Models.Assignee;
import de.unistuttgart.iste.ese.api.Repositories.AssigneeRepository;
import de.unistuttgart.iste.ese.api.DTOs.PostDTO;
import de.unistuttgart.iste.ese.api.Models.ToDo;
import de.unistuttgart.iste.ese.api.Repositories.ToDoRepository;
import de.unistuttgart.iste.ese.api.Models.TodoModel;
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
     * @return List of all Todo items
     */
    public List<ToDo> getAllToDos() {
        return (List<ToDo>) toDoRepository.findAll();
    }

    /**
     * Retrieves a specific Todo item by its ID.
     *
     * @param id The ID of the Todo item to retrieve
     * @return The found Todo item
     * @throws ResponseStatusException if Todo item is not found
     */
    public ToDo getToDoById(long id) {
        ToDo todo = toDoRepository.findById(id);
        if (todo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("ToDo with ID %d not found", id));
        }
        return todo;
    }

    /**
     * Creates a new Todo item based on the provided request data.
     * Validates the request, processes assignees, and predicts the category using AI model.
     *
     * @param requestBody The DTO containing Todo creation data
     * @return The newly created Todo item
     * @throws ResponseStatusException if validation fails
     */
    public ToDo createToDo(PostDTO requestBody) {
        validateToDoRequest(requestBody);
        List<Assignee> assignees = getAssignees(requestBody.getAssigneeIdList());
        Date dueDate = requestBody.getDueDate() != null ?
            new Date(requestBody.getDueDate()) : null;

        String category = todoModel.predictClass(requestBody.getTitle());

        ToDo toDo = new ToDo(
            requestBody.getTitle(),
            requestBody.getDescription(),
            requestBody.isFinished(),
            assignees,
            new Date(),
            dueDate,
            null,
            category
        );

        return toDoRepository.save(toDo);
    }

    /**
     * Updates an existing Todo item with new data.
     * Processes assignees and updates the category prediction if title changes.
     *
     * @param id The ID of the Todo item to update
     * @param requestBody The DTO containing updated Todo data
     * @return The updated Todo item
     * @throws ResponseStatusException if Todo not found or validation fails
     */
    public ToDo updateToDo(long id, PostDTO requestBody) {
        ToDo existingTodo = getToDoById(id);
        List<Assignee> assignees = getAssignees(requestBody.getAssigneeIdList());

        String category = todoModel.predictClass(requestBody.getTitle());

        existingTodo.setTitle(requestBody.getTitle());
        existingTodo.setDescription(requestBody.getDescription());
        existingTodo.setAssigneeList(assignees);
        existingTodo.setCategory(category);

        if (requestBody.getDueDate() != null) {
            existingTodo.setDueDate(new Date(requestBody.getDueDate()));
        }

        existingTodo.setFinished(requestBody.isFinished());
        existingTodo.setFinishedDate(requestBody.isFinished() ? new Date() : null);

        return toDoRepository.save(existingTodo);
    }

    /**
     * Deletes a Todo item by its ID.
     * Clears assignee relationships before deletion to maintain data consistency.
     *
     * @param id The ID of the Todo item to delete
     * @throws ResponseStatusException if Todo not found
     */
    public void deleteToDo(long id) {
        ToDo toDoToDelete = getToDoById(id);
        if (toDoToDelete.getAssigneeList() != null) {
            toDoToDelete.getAssigneeList().clear();
        }
        toDoRepository.save(toDoToDelete);
        toDoRepository.deleteById(id);
    }

    /**
     * Validates the Todo request data.
     * Checks for empty title and duplicate assignee IDs.
     *
     * @param requestBody The DTO to validate
     * @throws ResponseStatusException if validation fails
     */
    private void validateToDoRequest(PostDTO requestBody) {
        if (requestBody.getTitle() == null || requestBody.getTitle().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Title cannot be empty");
        }
        if (requestBody.getAssigneeIdList() != null) {
            if (new HashSet<>(requestBody.getAssigneeIdList()).size()
                < requestBody.getAssigneeIdList().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Duplicate assignee IDs are not allowed");
            }
        }
    }

    /**
     * Retrieves a list of Assignee objects based on their IDs.
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
     * Exports all Todo items to a CSV format.
     * Includes all Todo properties and formats dates and assignees appropriately.
     *
     * @return ResponseEntity containing the CSV data and appropriate headers
     */
    public ResponseEntity<String> exportToCSV() {
        List<ToDo> allToDos = getAllToDos();
        StringBuilder csvContent = new StringBuilder();

        // Header
        csvContent.append("id,title,description,finished,assignees,createdDate,dueDate,finishedDate,category\n");

        // Data rows
        for (ToDo todo : allToDos) {
            appendTodoToCSV(csvContent, todo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=todos.csv");

        return new ResponseEntity<>(csvContent.toString(), headers, HttpStatus.OK);
    }

    /**
     * Appends a single Todo item's data to the CSV content.
     * Formats dates and handles assignee names concatenation.
     *
     * @param csvContent StringBuilder to append the Todo data to
     * @param todo The Todo item to append
     */
    private void appendTodoToCSV(StringBuilder csvContent, ToDo todo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        csvContent.append(todo.getId()).append(",")
            .append(skipCSV(todo.getTitle())).append(",")
            .append(skipCSV(todo.getDescription())).append(",")
            .append(todo.isFinished()).append(",");

        // Assignees
        if (!todo.getAssigneeList().isEmpty()) {
            String assignees = todo.getAssigneeList().stream()
                .map(a -> a.getPreName() + " " + a.getName())
                .collect(Collectors.joining("+"));
            csvContent.append(skipCSV(assignees));
        }
        csvContent.append(",");

        // Dates
        csvContent.append(dateFormat.format(todo.getCreatedDate())).append(",")
            .append(todo.getDueDate() != null ? dateFormat.format(todo.getDueDate()) : "").append(",")
            .append(todo.getFinishedDate() != null ? dateFormat.format(todo.getFinishedDate()) : "").append(",")
            .append(skipCSV(todo.getCategory())).append("\n");
    }

    /**
     * Handles CSV field escaping for special characters.
     * Ensures proper CSV formatting when fields contain commas or quotes.
     *
     * @param field The string field to process
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
