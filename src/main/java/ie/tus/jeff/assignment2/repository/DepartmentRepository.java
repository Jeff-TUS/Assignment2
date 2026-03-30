package ie.tus.jeff.assignment2.repository;

import ie.tus.jeff.assignment2.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByName(String name);
}