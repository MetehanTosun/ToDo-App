package de.unistuttgart.iste.ese.api.Services;

import de.unistuttgart.iste.ese.api.Models.Assignee;
import de.unistuttgart.iste.ese.api.Repositories.AssigneeRepository;
import de.unistuttgart.iste.ese.api.Models.ToDo;
import de.unistuttgart.iste.ese.api.Repositories.ToDoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssigneeService {
    @Autowired
    private AssigneeRepository assigneeRepository;

    @Autowired
    private ToDoRepository toDoRepository;

    /**
     * Retrieves all assignees from the repository.
     *
     * @return List of all assignees
     */
    public List<Assignee> getAllAssignees() {
        return assigneeRepository.findAll();
    }

    /**
     * Retrieves a specific assignee by their ID.
     *
     * @param id The ID of the assignee to retrieve
     * @return The found assignee
     * @throws ResponseStatusException if assignee is not found
     */
    public Assignee getAssigneeById(long id) {
        Assignee assignee = assigneeRepository.findById(id);
        if (assignee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Assignee with ID %s not found!", id));
        }
        return assignee;
    }

    /**
     * Creates a new assignee after validating their university email address.
     *
     * @param assignee The assignee to create
     * @return The newly created assignee
     * @throws ResponseStatusException if email validation fails
     */
    public Assignee createAssignee(Assignee assignee) {
        validateUniversityEmail(assignee.getEmail());
        return assigneeRepository.save(assignee);
    }

    /**
     * Updates an existing assignee's information.
     *
     * @param id              The ID of the assignee to update
     * @param updatedAssignee The updated assignee information
     * @return The updated assignee
     * @throws ResponseStatusException if assignee is not found
     */
    public Assignee updateAssignee(long id, Assignee updatedAssignee) {
        Assignee existingAssignee = getAssigneeById(id);

        validateUniversityEmail(updatedAssignee.getEmail());

        existingAssignee.setPreName(updatedAssignee.getPreName());
        existingAssignee.setName(updatedAssignee.getName());
        existingAssignee.setEmail(updatedAssignee.getEmail());

        return assigneeRepository.save(existingAssignee);
    }

    /**
     * Deletes an assignee and removes their association from all todos.
     *
     * @param id The ID of the assignee to delete
     * @throws ResponseStatusException if assignee is not found
     */
    public void deleteAssignee(long id) {
        Assignee assigneeToDelete = getAssigneeById(id);

        // Remove assignee from all associated todos
        List<ToDo> associatedToDos = toDoRepository.findAll();
        for (ToDo toDo : associatedToDos) {
            toDo.getAssigneeList().removeIf(assignee -> assignee.getId().equals(id));
            toDoRepository.save(toDo);
        }

        assigneeRepository.deleteById(id);
    }

    /**
     * Validates that the email is a valid university email address.
     *
     * @param email The email address to validate
     * @throws ResponseStatusException if email is invalid
     */
    private void validateUniversityEmail(String email) {
        if (!email.contains("@") ||
            !email.endsWith("@iste.uni-stuttgart.de") ||
            email.equals("@iste.uni-stuttgart.de")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid university email");
        }
    }
}
