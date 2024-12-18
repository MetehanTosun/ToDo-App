package de.unistuttgart.iste.ese.api.assignee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssigneeRepository extends JpaRepository<Assignee, Long> {
    Assignee findByName(String name);

    Assignee findById(long id);
}
