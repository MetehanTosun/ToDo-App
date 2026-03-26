package de.unistuttgart.iste.ese.api.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.unistuttgart.iste.ese.api.Models.Assignee;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodoDTO {
    private Long id;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    private String description;
    private boolean finished;
    private List<Long> assigneeIdList;   // used in requests
    private List<Assignee> assigneeList; // used in responses
    private Long createdDate;
    private Long dueDate;
    private Long finishedDate;
    private String category;
}
