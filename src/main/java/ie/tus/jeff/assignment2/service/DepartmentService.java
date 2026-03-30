package ie.tus.jeff.assignment2.service;

import ie.tus.jeff.assignment2.model.*;
import ie.tus.jeff.assignment2.repository.*;
import ie.tus.jeff.assignment2.exception.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    public Department create(Department dept) {
        if (departmentRepository.existsByName(dept.getName())) {
            throw new DepartmentAlreadyExistsException(dept.getName());
        }

        return departmentRepository.save(dept);
    }

    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new DepartmentNotFoundException(id);
        }

        if (employeeRepository.existsByDepartmentId(id)) {
            throw new DepartmentDeletionException(id);
        }

        departmentRepository.deleteById(id);
    }
}