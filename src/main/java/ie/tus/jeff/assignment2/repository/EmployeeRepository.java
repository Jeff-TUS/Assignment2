package ie.tus.jeff.assignment2.repository;

import ie.tus.jeff.assignment2.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByDepartmentId(Long departmentId);
    List<Employee> findByDepartmentId(Long departmentId);
}
