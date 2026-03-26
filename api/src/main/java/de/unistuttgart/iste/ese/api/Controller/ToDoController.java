package de.unistuttgart.iste.ese.api.Controller;

import de.unistuttgart.iste.ese.api.ApiVersion1;
import de.unistuttgart.iste.ese.api.DTOs.TodoDTO;
import de.unistuttgart.iste.ese.api.Services.ToDoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ApiVersion1
public class ToDoController {
    @Autowired
    private ToDoService toDoService;

    @GetMapping("/todos")
    public List<TodoDTO> getToDos() {
        return toDoService.getAllToDos();
    }

    @GetMapping("/todos/{id}")
    public TodoDTO getToDo(@PathVariable("id") long id) {
        return toDoService.getToDoById(id);
    }

    @GetMapping("/csv-downloads/todos")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getToDoCSV() {
        return toDoService.exportToCSV();
    }

    @PostMapping("/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public TodoDTO createToDo(@Valid @RequestBody TodoDTO todo) {
        return toDoService.createToDo(todo);
    }

    @PutMapping("/todos/{id}")
    public TodoDTO updateTodo(@PathVariable("id") long id,
                              @Valid @RequestBody TodoDTO todo) {
        return toDoService.updateToDo(id, todo);
    }

    @DeleteMapping("/todos/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteToDo(@PathVariable("id") long id) {
        toDoService.deleteToDo(id);
    }
}
