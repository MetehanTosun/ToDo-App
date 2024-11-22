package de.unistuttgart.iste.ese.api.assignee;

import de.unistuttgart.iste.ese.api.ApiVersion1;
import de.unistuttgart.iste.ese.api.toDo.ToDo;
import de.unistuttgart.iste.ese.api.toDo.ToDoRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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
        return assigneeRepository.findById(id);
    }

    @PostMapping("/assignees")
    @ResponseStatus(HttpStatus.CREATED)
    public Assignee createAssignee(@Valid @RequestBody Assignee requestBody) {

        Assignee assignee = new Assignee(requestBody.getPreName(), requestBody.getName(), requestBody.getEmail());
        if (!assignee.getEmail().contains("@") || !assignee.getEmail().endsWith("@iste.uni-stuttgart.de" ) || assignee.getEmail().equals("@iste.uni-stuttgart.de"))
         {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Assignee savedAssignee = assigneeRepository.save(assignee);
        return savedAssignee;
    }

    @PutMapping("/assignees/{id}")
    public Assignee updateAssignee(@PathVariable("id") long id, @Valid @RequestBody Assignee requestBody) {
        Assignee assigneeToUpdate = assigneeRepository.findById(id);
        Assignee savedAssignee;
        if (assigneeToUpdate != null) {
            assigneeToUpdate.setEmail(requestBody.getEmail());
            assigneeToUpdate.setName(requestBody.getName());
            assigneeToUpdate.setPrename(requestBody.getPreName());
            savedAssignee = assigneeRepository.save(assigneeToUpdate);
            return savedAssignee;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Assignee with ID %s not found!", id));
    }

    // delete a Assignee
    @DeleteMapping("/assignees/{id}")
    public void deleteAssignee(@PathVariable("id") long id) {
        Assignee assigneeToDelete = assigneeRepository.findById(id);


        if (assigneeToDelete == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee with ID " + id + " not found");
        }

        List<ToDo> associatedToDos = toDoRepository.findAll(); // Optimieren Sie dies bei Bedarf
        for (ToDo toDo : associatedToDos) {
            toDo.getAssigneeList().removeIf(assignee -> assignee.getId().equals(id));
            toDoRepository.save(toDo); // Änderungen speichern
        }

        assigneeRepository.deleteById(id);

    }
}
