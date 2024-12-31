package de.unistuttgart.iste.ese.api.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "assignees")
@Getter
@Setter
public class Assignee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @JsonProperty("prename") // Damit JSON korrekt zugeordnet wird
    @Column(name = "pre_name")
    private String preName;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    public Assignee(String preName, String name, String email) {
        this.preName = preName;
        this.name = name;
        this.email = email;
    }

    public Assignee() {}

}
