package de.unistuttgart.iste.ese.api.Controller;

import de.unistuttgart.iste.ese.api.ApiVersion1;
import de.unistuttgart.iste.ese.api.Services.AssigneeService;
import de.unistuttgart.iste.ese.api.Models.Assignee;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@ApiVersion1
public class AssigneeController {

    @Autowired
    private AssigneeService assigneeService;

    /**
     * Retrieves all assignees.
     *
     * @return List of all assignees
     */
    @GetMapping("/assignees")
    public List<Assignee> getAssignees() {
        return assigneeService.getAllAssignees();
    }

    /**
     * Retrieves a specific assignee by ID.
     *
     * @param id The ID of the assignee
     * @return The requested assignee
     */
    @GetMapping("/assignees/{id}")
    public Assignee getAssignee(@PathVariable("id") long id) {
        return assigneeService.getAssigneeById(id);
    }

    /**
     * Creates a new assignee.
     *
     * @param requestBody The assignee data
     * @return The created assignee
     */
    @PostMapping("/assignees")
    @ResponseStatus(HttpStatus.CREATED)
    public Assignee createAssignee(@Valid @RequestBody Assignee requestBody) {
        return assigneeService.createAssignee(requestBody);
    }

    /**
     * Updates an existing assignee.
     *
     * @param id The ID of the assignee to update
     * @param requestBody The updated assignee data
     * @return The updated assignee
     */
    @PutMapping("/assignees/{id}")
    public Assignee updateAssignee(@PathVariable("id") long id,
                                   @Valid @RequestBody Assignee requestBody) {
        return assigneeService.updateAssignee(id, requestBody);
    }

    /**
     * Deletes an assignee.
     *
     * @param id The ID of the assignee to delete
     */
    @DeleteMapping("/assignees/{id}")
    public void deleteAssignee(@PathVariable("id") long id) {
        assigneeService.deleteAssignee(id);
    }
}
