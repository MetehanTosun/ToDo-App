package de.unistuttgart.iste.ese.api.DTOs;

import de.unistuttgart.iste.ese.api.Models.Assignee;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ToDoDTO {
    private Long id;
    private String title;
    private String description;
    private boolean finished;
    private List<Assignee> assigneeList;
    private Long createdDate;
    private Long dueDate;
    private String category;

    public ToDoDTO(Long id, String title, String description, boolean finished, List<Assignee> assigneeList, Long createdDate, Long dueDate, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.finished = finished;
        this.assigneeList = assigneeList;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.category = category;
    }

    public ToDoDTO() {}

}
