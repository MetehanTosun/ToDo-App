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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@ApiVersion1
public class ToDoController {

    @Autowired
    private ToDoRepository toDoRepository;

    @Autowired
    private AssigneeRepository assigneeRepository;


    @GetMapping("/todos")
    public List<GetDTO> getToDos() {
        List<ToDo> allToDos = (List<ToDo>) toDoRepository.findAll();
        List<GetDTO> getDTOs = new ArrayList<>();
        for (ToDo toDos: allToDos){
            GetDTO getDTO = new GetDTO(toDos.getId(), toDos.getTitle(), toDos.getDescription(),  toDos.isFinished(), toDos.getAssigneeList(), toDos.getCreatedDate().getTime(),toDos.getDueDate().getTime(), null);
            getDTOs.add(getDTO);

        }
        return getDTOs;
    }

    @GetMapping("/todos/{id}")
    public GetDTO getToDo(@PathVariable("id") long id) {
        ToDo toDo = toDoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "To do not found"));
        GetDTO getDTO = new GetDTO(toDo.getId(), toDo.getTitle(), toDo.getDescription(), toDo.isFinished(), toDo.getAssigneeList(), toDo.getCreatedDate().getTime(), toDo.getDueDate().getTime(), null);
        return getDTO;
    }

    @PostMapping("/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public ToDoDTO createToDo(@Valid @RequestBody PostDTO requestBody) {
        List<Long> idList = requestBody.getAssigneeIdList();
        List<Assignee> assignees = new ArrayList<>();
        if (idList != null) {
            for (Long assigneeId : idList) {
                Assignee assignee = assigneeRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
                assignees.add(assignee);
            }
        }
        ToDo toDo = new ToDo(requestBody.getTitle(), requestBody.getDescription(), requestBody.isFinished(), assignees, new Date(), new Date(requestBody.getDueDate()), null);
        toDoRepository.save(toDo);
        ToDoDTO toDoDTO = new ToDoDTO(toDo.getId(), requestBody.getTitle(), requestBody.getDescription(), requestBody.isFinished(), assignees, new Date().getTime(), requestBody.getDueDate());
        return toDoDTO;
    }

    @PutMapping("/todos/{id}")
    public ToDoDTO updateToDo(@PathVariable("id") long id, @Valid @RequestBody PutDTO requestBody) {
        ToDo existingToDo = toDoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ToDo not found"));

        // Assignees verarbeiten
        List<Long> idList = requestBody.getAssigneeIdList();
        List<Assignee> assignees = new ArrayList<>();
        if (idList != null) {
            for (Long assigneeId : idList) {
                Assignee assignee = assigneeRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
                assignees.add(assignee);
            }
        }

        existingToDo.setTitle(requestBody.getTitle());
        existingToDo.setDescription(requestBody.getDescription());
        existingToDo.setFinished(requestBody.isFinished());
        existingToDo.setAssigneeList(assignees);
        if (requestBody.getDueDate() != null) {
            existingToDo.setDueDate(new Date(requestBody.getDueDate()));
        }

        toDoRepository.save(existingToDo);

        // DTO für die Antwort erstellen
        ToDoDTO toDoDTO = new ToDoDTO(
            existingToDo.getId(),
            existingToDo.getTitle(),
            existingToDo.getDescription(),
            existingToDo.isFinished(),
            existingToDo.getAssigneeList(),
            existingToDo.getCreatedDate().getTime(),
            existingToDo.getDueDate() != null ? existingToDo.getDueDate().getTime() : null
        );

        return toDoDTO;
    }

    @DeleteMapping("/todos/{id}")
    public void deleteToDo(@PathVariable("id") long id) {

        ToDo toDoToDelete = toDoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ToDo not found"));
        if (toDoToDelete == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ToDo with ID " + id + " not found");
        }
        toDoRepository.deleteById(id);
    }
}
