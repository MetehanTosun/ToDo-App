package de.unistuttgart.iste.ese.api.DTOs;

import de.unistuttgart.iste.ese.api.Models.Assignee;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetDTO {
    private Long id;
    private String title;
    private String description;
    private boolean finished;
    private List<Assignee> assigneeList;
    private Long createdDate;
    private Long dueDate;
    private Long finishedDate;
    private String category;

    public GetDTO(Long id, String title, String description, boolean finished, List<Assignee> assigneeList, Long createdDate, Long dueDate, Long finishedDate, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.finished = finished;
        this.assigneeList = assigneeList;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.finishedDate = finishedDate;
        this.category = category;
    }

    public GetDTO() {
    }
}
