package ie.tus.jeff.assignment2.service;

import ie.tus.jeff.assignment2.exception.DepartmentNotFoundException;
import ie.tus.jeff.assignment2.exception.EmployeeDepartmentMismatchException;
import ie.tus.jeff.assignment2.exception.EmployeeNotFoundException;
import ie.tus.jeff.assignment2.model.Department;
import ie.tus.jeff.assignment2.model.Employee;
import ie.tus.jeff.assignment2.repository.DepartmentRepository;
import ie.tus.jeff.assignment2.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public Employee create(Long deptId, Employee emp) {
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new DepartmentNotFoundException(deptId));

        emp.setDepartment(dept);
        return employeeRepository.save(emp);
    }

    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    public List<Employee> getByDepartment(Long deptId) {
        if (!departmentRepository.existsById(deptId)) {
            throw new DepartmentNotFoundException(deptId);
        }
        return employeeRepository.findByDepartmentId(deptId);
    }

    public Employee getByIdAndDepartment(Long empId, Long deptId) {
        Employee emp = employeeRepository.findById(empId)
                .orElseThrow(() -> new EmployeeNotFoundException(empId));
        if (!emp.getDepartment().getId().equals(deptId)) {
            throw new EmployeeDepartmentMismatchException(empId, deptId);
        }
        return emp;
    }

    public void delete(Long deptId, Long empId) {
        Employee emp = employeeRepository.findById(empId)
                .orElseThrow(() -> new EmployeeNotFoundException(empId));

        if (!emp.getDepartment().getId().equals(deptId)) {
            throw new EmployeeDepartmentMismatchException(empId, deptId);
        }

        employeeRepository.delete(emp);
    }
}