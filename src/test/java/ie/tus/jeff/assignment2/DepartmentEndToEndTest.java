package ie.tus.jeff.assignment2;

import ie.tus.jeff.assignment2.dto.DepartmentDTO;
import ie.tus.jeff.assignment2.dto.EmployeeDTO;
import ie.tus.jeff.assignment2.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DepartmentEndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private DepartmentDTO createDepartment(String name) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setName(name);
        ResponseEntity<DepartmentDTO> response =
                restTemplate.postForEntity("/departments", dto, DepartmentDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    // --- Full department lifecycle ---

    @Test
    void createGetDelete_departmentLifecycle() {
        // Create
        DepartmentDTO created = createDepartment("E2E-Dept-" + System.nanoTime());
        assertThat(created.getId()).isNotNull();

        // Get by id
        ResponseEntity<DepartmentDTO> fetched =
                restTemplate.getForEntity("/departments/" + created.getId(), DepartmentDTO.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().getName()).isEqualTo(created.getName());

        // Appears in list
        ResponseEntity<DepartmentDTO[]> all =
                restTemplate.getForEntity("/departments", DepartmentDTO[].class);
        assertThat(all.getBody()).extracting(DepartmentDTO::getId).contains(created.getId());

        // Delete
        restTemplate.delete("/departments/" + created.getId());
        ResponseEntity<ErrorResponse> gone =
                restTemplate.getForEntity("/departments/" + created.getId(), ErrorResponse.class);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createDepartment_withDuplicateName_returns409() {
        String name = "E2E-Dup-" + System.nanoTime();
        createDepartment(name);

        DepartmentDTO duplicate = new DepartmentDTO();
        duplicate.setName(name);
        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/departments", duplicate, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains(name);
    }

    @Test
    void deleteDepartment_whenHasEmployees_returns409() {
        DepartmentDTO dept = createDepartment("E2E-Busy-" + System.nanoTime());

        // Add an employee
        EmployeeDTO empDto = new EmployeeDTO();
        empDto.setName("Worker");
        restTemplate.postForEntity("/departments/" + dept.getId() + "/employees",
                empDto, EmployeeDTO.class);

        // Attempt delete
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/departments/" + dept.getId(), HttpMethod.DELETE,
                null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getDepartment_whenNotFound_returns404WithTimestamp() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity("/departments/999999", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }
}