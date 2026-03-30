package ie.tus.jeff.assignment2.controller;

import ie.tus.jeff.assignment2.dto.EmployeeDTO;
import ie.tus.jeff.assignment2.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAll() {
        List<EmployeeDTO> result = service.getAll().stream()
                .map(e -> new EmployeeDTO(e.getId(), e.getName(), e.getDepartment().getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}