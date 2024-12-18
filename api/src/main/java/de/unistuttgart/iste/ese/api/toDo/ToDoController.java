package de.unistuttgart.iste.ese.api.toDo;

import de.unistuttgart.iste.ese.api.ApiVersion1;
import de.unistuttgart.iste.ese.api.assignee.Assignee;
import de.unistuttgart.iste.ese.api.assignee.AssigneeRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@ApiVersion1
public class ToDoController {

    @Autowired
    private ToDoRepository toDoRepository;

    @Autowired
    private AssigneeRepository assigneeRepository;

    //Get all todos
    @GetMapping("/todos")
    public List<GetDTO> getToDos() {
        List<ToDo> allToDos = (List<ToDo>) toDoRepository.findAll();
        List<GetDTO> getDTOs = new ArrayList<>();
        for (ToDo toDo : allToDos) {
            GetDTO getDTO = new GetDTO(
                    toDo.getId(),
                    toDo.getTitle(),
                    toDo.getDescription(),
                    toDo.isFinished(),
                    toDo.getAssigneeList(),
                    toDo.getCreatedDate().getTime(),
                    toDo.getDueDate() != null ? toDo.getDueDate().getTime() : null,
                toDo.getFinishedDate() != null ? toDo.getFinishedDate().getTime() : null
            );
            getDTOs.add(getDTO);
        }
        return getDTOs;
    }
    // Get a single todo
    @GetMapping("/todos/{id}")
    public GetDTO getToDo(@PathVariable("id") long id) {
        // Hole das gewünschte ToDo oder wirf eine 404-Fehlerantwort
        ToDo toDo = toDoRepository.findById(id);

        if (toDo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("ToDo with ID %d not found", id));
        }

        // Debugging-Assignee-Liste
        System.out.println("AssigneeList size: " + toDo.getAssigneeList().size());

        // Erstelle und gib das GetDTO für dieses ToDo zurück
        return new GetDTO(
                toDo.getId(),
                toDo.getTitle(),
                toDo.getDescription(),
                toDo.isFinished(),
                new ArrayList<>(toDo.getAssigneeList()), // Assignees als konkrete Liste
                toDo.getCreatedDate().getTime(),
                toDo.getDueDate() != null ? toDo.getDueDate().getTime() : null,
                toDo.getFinishedDate() != null ? toDo.getFinishedDate().getTime() : null
        );
    }

    @PostMapping("/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public ToDoDTO createToDo(@Valid @RequestBody PostDTO requestBody) {
        if (requestBody.getTitle() == null || requestBody.getTitle().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }
        if (requestBody.getAssigneeIdList() != null) {
            if (new HashSet<>(requestBody.getAssigneeIdList()).size() < requestBody.getAssigneeIdList().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate assignee IDs are not allowed");
            }
        }
        List<Assignee> assignees = new ArrayList<>();
        if (requestBody.getAssigneeIdList() != null) {
            for (Long assigneeId : requestBody.getAssigneeIdList()) {
                Assignee assignee = assigneeRepository.findById(assigneeId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
                assignees.add(assignee);
            }
        }
        Date dueDate = requestBody.getDueDate() != null ? new Date(requestBody.getDueDate()) : null;

        ToDo toDo = new ToDo(
                requestBody.getTitle(),
                requestBody.getDescription(),
                requestBody.isFinished(),
                assignees,
                new Date(),
                dueDate,
                null
        );

        toDoRepository.save(toDo);
        return new ToDoDTO(
                toDo.getId(),
                requestBody.getTitle(),
                requestBody.getDescription(),
                requestBody.isFinished(),
                assignees,
                toDo.getCreatedDate().getTime(),
                dueDate != null ? dueDate.getTime() : null
        );
    }

    @PutMapping("/todos/{id}")
    public GetDTO updateTodo(@PathVariable("id") long id, @Valid @RequestBody PostDTO requestBody) {
        ToDo existingTodo = toDoRepository.findById(id);
        if (existingTodo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ToDo with ID " + id + " not found");
        }

        // Validate Assignee IDs
        List<Assignee> assignees = new ArrayList<>();
        if (requestBody.getAssigneeIdList() != null) {
            Set<Long> uniqueIds = new HashSet<>();
            for (Long assigneeId : requestBody.getAssigneeIdList()) {
                if (!uniqueIds.add(assigneeId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate Assignee ID: " + assigneeId);
                }
                Assignee existingAssignee = assigneeRepository.findById(assigneeId).get();
                assignees.add(existingAssignee);

            }
        }
        // Update fields
        existingTodo.setTitle(requestBody.getTitle());
        existingTodo.setDescription(requestBody.getDescription());
        existingTodo.setAssigneeList(assignees);

        if (requestBody.getDueDate() != null) {
            Date dueDate = new Date(requestBody.getDueDate());
            existingTodo.setDueDate(dueDate);
        }

        existingTodo.setFinished(requestBody.isFinished());
        existingTodo.setFinishedDate(requestBody.isFinished() ? new Date() : null);
        toDoRepository.save(existingTodo);
        return new GetDTO(existingTodo.getId(), existingTodo.getTitle(), existingTodo.getDescription(), existingTodo.isFinished(),existingTodo.getAssigneeList(),existingTodo.getCreatedDate().getTime(),existingTodo.getDueDate().getTime(),existingTodo.isFinished() ? new Date().getTime(): null);
    }

    @DeleteMapping("/todos/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteToDo(@PathVariable("id") long id) {
        ToDo toDoToDelete = toDoRepository.findById(id);
        if (toDoToDelete == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ToDo with ID " + id + " not found");
        }

        // AssigneeList bereinigen, falls vorhanden
        if (toDoToDelete.getAssigneeList() != null) {
            toDoToDelete.getAssigneeList().clear();
        }

        toDoRepository.save(toDoToDelete);
        toDoRepository.deleteById(id);
    }
}
