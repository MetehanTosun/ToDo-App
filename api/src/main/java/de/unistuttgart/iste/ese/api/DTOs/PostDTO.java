package de.unistuttgart.iste.ese.api.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostDTO {
    private String title;
    private String description;
    private boolean finished;
    private List<Long> assigneeIdList;
    private Long createdDate;
    private Long dueDate;
    private Long finishedDate;

    public PostDTO(String title, String description, boolean finished, List<Long> assigneeIdList, Long createdDate, Long dueDate) {
        this.title = title;
        this.description = description;
        this.finished = finished;
        this.assigneeIdList = assigneeIdList;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
    }

    public PostDTO() {
    }
}
