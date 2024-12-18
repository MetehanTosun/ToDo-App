package de.unistuttgart.iste.ese.api.assignee;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "assignees")
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

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPreName() {
        return preName;
    }

    public void setPreName(String preName) {
        this.preName = preName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
