package de.unistuttgart.iste.ese.api.toDo;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

public class PutDTO {
    private String title;
    private String description;
    private boolean finished;
    private List<Long> assigneeIdList;
    private Long createdDate;
    private Long dueDate;
    private Long finishedDate;

    public PutDTO(String title, String description, boolean finished, List<Long> assigneeIdList, Long dueDate) {
        this.title = title;
        this.description = description;
        this.finished = finished;
        this.assigneeIdList = assigneeIdList;
        this.dueDate = dueDate;
    }

    public PutDTO() {
    }

    // Get and Set functions
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public List<Long> getAssigneeIdList() {
        return assigneeIdList;
    }

    public void setAssigneeIdList(List<Long> assigneeIdList) {
        this.assigneeIdList = assigneeIdList;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public Long getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Long finishedDate) {
        this.finishedDate = finishedDate;
    }
}
