package ie.tus.jeff.assignment2;

import ie.tus.jeff.assignment2.dto.DepartmentDTO;
import ie.tus.jeff.assignment2.dto.EmployeeDTO;
import ie.tus.jeff.assignment2.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EmployeeEndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private DepartmentDTO dept;

    @BeforeEach
    void setUp() {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setName("E2E-EmpTest-" + System.nanoTime());
        dept = restTemplate.postForEntity("/departments", dto, DepartmentDTO.class).getBody();
    }

    private EmployeeDTO createEmployee(Long deptId, String name) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName(name);
        return restTemplate.postForEntity(
                "/departments/" + deptId + "/employees", dto, EmployeeDTO.class).getBody();
    }

    // --- Employee lifecycle under a department ---

    @Test
    void createGetDelete_employeeLifecycle() {
        EmployeeDTO created = createEmployee(dept.getId(), "Alice");
        assertThat(created.getId()).isNotNull();
        assertThat(created.getDepartmentId()).isEqualTo(dept.getId());

        // Get by dept+id
        ResponseEntity<EmployeeDTO> fetched = restTemplate.getForEntity(
                "/departments/" + dept.getId() + "/employees/" + created.getId(),
                EmployeeDTO.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().getName()).isEqualTo("Alice");

        // Appears in department employee list
        ResponseEntity<EmployeeDTO[]> list = restTemplate.getForEntity(
                "/departments/" + dept.getId() + "/employees", EmployeeDTO[].class);
        assertThat(list.getBody()).extracting(EmployeeDTO::getId).contains(created.getId());

        // Appears in global /employees list
        ResponseEntity<EmployeeDTO[]> all =
                restTemplate.getForEntity("/employees", EmployeeDTO[].class);
        assertThat(all.getBody()).extracting(EmployeeDTO::getId).contains(created.getId());

        // Delete
        restTemplate.delete("/departments/" + dept.getId() + "/employees/" + created.getId());
        ResponseEntity<ErrorResponse> gone = restTemplate.getForEntity(
                "/departments/" + dept.getId() + "/employees/" + created.getId(),
                ErrorResponse.class);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getEmployee_withWrongDepartment_returns400() {
        // Create a second department
        DepartmentDTO other = new DepartmentDTO();
        other.setName("Other-" + System.nanoTime());
        DepartmentDTO otherDept = restTemplate
                .postForEntity("/departments", other, DepartmentDTO.class).getBody();

        EmployeeDTO emp = createEmployee(dept.getId(), "Mismatch");

        // Try to fetch emp through otherDept
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/departments/" + otherDept.getId() + "/employees/" + emp.getId(),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains(String.valueOf(emp.getId()));
    }

    @Test
    void createEmployee_whenDepartmentNotFound_returns404() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("Ghost");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/departments/999999/employees", dto, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteEmployee_withWrongDepartment_returns400() {
        DepartmentDTO other = new DepartmentDTO();
        other.setName("OtherDel-" + System.nanoTime());
        DepartmentDTO otherDept = restTemplate
                .postForEntity("/departments", other, DepartmentDTO.class).getBody();

        EmployeeDTO emp = createEmployee(dept.getId(), "CrossDept");

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/departments/" + otherDept.getId() + "/employees/" + emp.getId(),
                HttpMethod.DELETE, null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}