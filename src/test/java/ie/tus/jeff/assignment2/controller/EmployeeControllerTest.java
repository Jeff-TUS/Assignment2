package ie.tus.jeff.assignment2.controller;

import ie.tus.jeff.assignment2.model.Department;
import ie.tus.jeff.assignment2.model.Employee;
import ie.tus.jeff.assignment2.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    private Employee makeEmp(Long id, String name, Long deptId) {
        Department d = new Department();
        d.setId(deptId);
        d.setName("Dept");
        Employee e = new Employee();
        e.setId(id);
        e.setName(name);
        e.setDepartment(d);
        return e;
    }

    @Test
    void getAllEmployees_returnsListAnd200() throws Exception {
        when(employeeService.getAll()).thenReturn(
                List.of(makeEmp(1L, "Alice", 10L), makeEmp(2L, "Bob", 20L)));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].departmentId").value(10))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void getAllEmployees_whenNone_returnsEmptyArray() throws Exception {
        when(employeeService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}