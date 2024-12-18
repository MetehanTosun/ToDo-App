package de.unistuttgart.iste.ese.api.toDo;

import de.unistuttgart.iste.ese.api.assignee.Assignee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.*;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class ToDo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull (message = "Title cannot be null")
    private String title;
    private String description;
    private boolean finished;

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
        name = "todo_assignee_list",
        joinColumns = @JoinColumn(name = "todo_id"),
        inverseJoinColumns = @JoinColumn(name = "assignee_id")
    )
    private List<Assignee> assigneeList;
    private Date createdDate;
    private Date dueDate;
    private Date finishedDate;


    public ToDo ( String title, String description, boolean finished, List<Assignee> assigneeList, Date createdDate, Date dueDate, Date finishedDate){
        this.title = title;
        this.description = description;
        this.finished = finished;
        this.assigneeList = assigneeList;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.finishedDate = finishedDate;
    }


    public ToDo() {

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
        if(finished){
            this.setFinishedDate(new Date());
        }
        this.finished = finished;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public List<Assignee> getAssigneeList() {
        return assigneeList != null ? assigneeList : new ArrayList<>();
    }

    public void setAssigneeList(List<Assignee> assigneeList) {
        this.assigneeList = assigneeList;

    }

    public Date getCreatedDate() {
        return createdDate;

    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;

    }

    public Date getDueDate() {
        return dueDate;

    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;

    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }


}
