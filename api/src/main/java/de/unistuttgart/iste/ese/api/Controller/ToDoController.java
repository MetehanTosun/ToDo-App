package de.unistuttgart.iste.ese.api.Controller;

import de.unistuttgart.iste.ese.api.ApiVersion1;
import de.unistuttgart.iste.ese.api.Services.ToDoService;
import de.unistuttgart.iste.ese.api.DTOs.GetDTO;
import de.unistuttgart.iste.ese.api.DTOs.PostDTO;
import de.unistuttgart.iste.ese.api.Models.ToDo;
import de.unistuttgart.iste.ese.api.DTOs.ToDoDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@ApiVersion1
public class ToDoController {
    @Autowired
    private ToDoService toDoService;

    /**
     * Retrieves all todos.
     * Converts the todo entities to GetDTO objects for client response.
     *
     * @return List of todos in GetDTO format
     */
    @GetMapping("/todos")
    public List<GetDTO> getToDos() {
        return toDoService.getAllToDos().stream()
            .map(this::convertToGetDTO)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific todo by ID.
     *
     * @param id The ID of the todo to retrieve
     * @return The requested todo in GetDTO format
     */
    @GetMapping("/todos/{id}")
    public GetDTO getToDo(@PathVariable("id") long id) {
        return convertToGetDTO(toDoService.getToDoById(id));
    }

    /**
     * Exports all todos to CSV format.
     *
     * @return ResponseEntity containing CSV data with appropriate headers
     */
    @GetMapping("/csv-downloads/todos")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getToDoCSV() {
        return toDoService.exportToCSV();
    }

    /**
     * Creates a new todo.
     *
     * @param requestBody The todo data in PostDTO format
     * @return The created todo in ToDoDTO format
     */
    @PostMapping("/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public ToDoDTO createToDo(@Valid @RequestBody PostDTO requestBody) {
        ToDo createdToDo = toDoService.createToDo(requestBody);
        return convertToToDoDTO(createdToDo);
    }

    /**
     * Updates an existing todo.
     *
     * @param id The ID of the todo to update
     * @param requestBody The updated todo data in PostDTO format
     * @return The updated todo in GetDTO format
     */
    @PutMapping("/todos/{id}")
    public GetDTO updateTodo(@PathVariable("id") long id,
                             @Valid @RequestBody PostDTO requestBody) {
        ToDo updatedToDo = toDoService.updateToDo(id, requestBody);
        return convertToGetDTO(updatedToDo);
    }

    /**
     * Deletes a todo.
     *
     * @param id The ID of the todo to delete
     */
    @DeleteMapping("/todos/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteToDo(@PathVariable("id") long id) {
        toDoService.deleteToDo(id);
    }

    /**
     * Converts a Todo entity to GetDTO format.
     * Handles null date fields and creates a new ArrayList for assignees.
     *
     * @param toDo The Todo entity to convert
     * @return The todo data in GetDTO format
     */
    private GetDTO convertToGetDTO(ToDo toDo) {
        return new GetDTO(
            toDo.getId(),
            toDo.getTitle(),
            toDo.getDescription(),
            toDo.isFinished(),
            new ArrayList<>(toDo.getAssigneeList()),
            toDo.getCreatedDate().getTime(),
            toDo.getDueDate() != null ? toDo.getDueDate().getTime() : null,
            toDo.getFinishedDate() != null ?
                toDo.getFinishedDate().getTime() : null,
            toDo.getCategory()
        );
    }

    /**
     * Converts a Todo entity to ToDoDTO format.
     * Used specifically for the create todo response.
     *
     * @param toDo The Todo entity to convert
     * @return The todo data in ToDoDTO format
     */
    private ToDoDTO convertToToDoDTO(ToDo toDo) {
        return new ToDoDTO(
            toDo.getId(),
            toDo.getTitle(),
            toDo.getDescription(),
            toDo.isFinished(),
            toDo.getAssigneeList(),
            toDo.getCreatedDate().getTime(),
            toDo.getDueDate() != null ? toDo.getDueDate().getTime() : null,
            toDo.getCategory()
        );
    }
}
