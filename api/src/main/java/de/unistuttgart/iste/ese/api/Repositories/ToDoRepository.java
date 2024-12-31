package de.unistuttgart.iste.ese.api.Repositories;

import de.unistuttgart.iste.ese.api.Models.ToDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToDoRepository extends JpaRepository<ToDo, Long> {
    ToDo findById(long id);
}
