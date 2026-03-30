package ie.tus.jeff.assignment2.controller;

import ie.tus.jeff.assignment2.dto.*;
import ie.tus.jeff.assignment2.model.*;
import ie.tus.jeff.assignment2.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentService service;
    private final EmployeeService employeeService;

    public DepartmentController(DepartmentService service, EmployeeService employeeService) {
        this.service = service;
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> create(@RequestBody DepartmentDTO dto) {
        Department dept = new Department();
        dept.setName(dto.getName());
        Department saved = service.create(dept);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DepartmentDTO(saved.getId(), saved.getName()));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAll() {
        List<DepartmentDTO> result = service.getAll().stream()
                .map(d -> new DepartmentDTO(d.getId(), d.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getById(@PathVariable Long id) {
        Department d = service.getById(id);
        return ResponseEntity.ok(new DepartmentDTO(d.getId(), d.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{deptId}/employees")
    public ResponseEntity<EmployeeDTO> createEmployee(@PathVariable Long deptId, @RequestBody EmployeeDTO dto) {
        Employee emp = new Employee();
        emp.setName(dto.getName());
        Employee saved = employeeService.create(deptId, emp);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EmployeeDTO(saved.getId(), saved.getName(), saved.getDepartment().getId()));
    }

    @GetMapping("/{deptId}/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable Long deptId) {
        List<EmployeeDTO> result = employeeService.getByDepartment(deptId).stream()
                .map(e -> new EmployeeDTO(e.getId(), e.getName(), e.getDepartment().getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{deptId}/employees/{empId}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long deptId, @PathVariable Long empId) {
        Employee emp = employeeService.getByIdAndDepartment(empId, deptId);
        EmployeeDTO dto = new EmployeeDTO(emp.getId(), emp.getName(), emp.getDepartment().getId());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{deptId}/employees/{empId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long deptId, @PathVariable Long empId) {
        employeeService.delete(deptId, empId);
        return ResponseEntity.noContent().build();
    }
}