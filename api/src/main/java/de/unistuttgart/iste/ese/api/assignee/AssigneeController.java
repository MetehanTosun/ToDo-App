package de.unistuttgart.iste.ese.api.assignee;

import de.unistuttgart.iste.ese.api.ApiVersion1;
import de.unistuttgart.iste.ese.api.toDo.ToDo;
import de.unistuttgart.iste.ese.api.toDo.ToDoRepository;
import de.unistuttgart.iste.ese.api.toDo.ToDoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@ApiVersion1
public class AssigneeController {

    @Autowired
    private AssigneeRepository assigneeRepository;

    @Autowired
    private ToDoRepository toDoRepository;

    @GetMapping("/assignees")
    public List<Assignee> getAssignees() {
        return assigneeRepository.findAll();
    }
    @GetMapping("/assignees/{id}")
    public Assignee getAssignee(@PathVariable("id") long id) {

        Assignee searchedAssignee = assigneeRepository.findById(id);
        if (searchedAssignee != null) {
            return searchedAssignee;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Assignee with ID %s not found!", id));
    }


    @PostMapping("/assignees")
    @ResponseStatus(HttpStatus.CREATED)
    public Assignee createAssignee(@Valid @RequestBody Assignee requestBody) {
        if (!requestBody.getEmail().contains("@") ||
            !requestBody.getEmail().endsWith("@iste.uni-stuttgart.de") ||
            requestBody.getEmail().equals("@iste.uni-stuttgart.de")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid university email");
        }

        Assignee savedAssignee = assigneeRepository.save(requestBody);
        return savedAssignee;
    }

    @PutMapping("/assignees/{id}")
    public Assignee updateAssignee(@PathVariable("id") long id, @Valid @RequestBody Assignee requestBody) {
        Assignee assigneeToUpdate = assigneeRepository.findById(id);
            if(assigneeToUpdate == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Duplicate Assignee ID: ");
            }

        assigneeToUpdate.setPreName(requestBody.getPreName());
        assigneeToUpdate.setName(requestBody.getName());
        assigneeToUpdate.setEmail(requestBody.getEmail());

        return assigneeRepository.save(assigneeToUpdate);
    }

    @DeleteMapping("/assignees/{id}")
    public void deleteAssignee(@PathVariable("id") long id) {
        Assignee assigneeToDelete = assigneeRepository.findById(id);
        if(assigneeToDelete == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Duplicate Assignee ID: ");
        }

        List<ToDo> associatedToDos = toDoRepository.findAll();
        for (ToDo toDo : associatedToDos) {
            toDo.getAssigneeList().removeIf(assignee -> assignee.getId().equals(id));
            toDoRepository.save(toDo);
        }

        assigneeRepository.deleteById(id);
    }
}
