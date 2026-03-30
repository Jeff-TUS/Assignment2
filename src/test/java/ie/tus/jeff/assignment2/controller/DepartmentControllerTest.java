package ie.tus.jeff.assignment2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.tus.jeff.assignment2.exception.*;
import ie.tus.jeff.assignment2.model.Department;
import ie.tus.jeff.assignment2.model.Employee;
import ie.tus.jeff.assignment2.service.DepartmentService;
import ie.tus.jeff.assignment2.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private EmployeeService employeeService;

    // helpers
    private Department makeDept(Long id, String name) {
        Department d = new Department();
        d.setId(id);
        d.setName(name);
        return d;
    }

    private Employee makeEmp(Long id, String name, Department dept) {
        Employee e = new Employee();
        e.setId(id);
        e.setName(name);
        e.setDepartment(dept);
        return e;
    }

    // --- POST /departments ---

    @Test
    void createDepartment_validRequest_returns201WithBody() throws Exception {
        Department saved = makeDept(1L, "Engineering");
        when(departmentService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Engineering\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    void createDepartment_duplicateName_returns409() throws Exception {
        when(departmentService.create(any()))
                .thenThrow(new DepartmentAlreadyExistsException("Engineering"));

        mockMvc.perform(post("/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Engineering\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // --- GET /departments ---

    @Test
    void getAllDepartments_returnsListAnd200() throws Exception {
        when(departmentService.getAll()).thenReturn(List.of(makeDept(1L, "Eng"), makeDept(2L, "HR")));

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Eng"))
                .andExpect(jsonPath("$[1].name").value("HR"));
    }

    @Test
    void getAllDepartments_whenNone_returnsEmptyArray() throws Exception {
        when(departmentService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /departments/{id} ---

    @Test
    void getDepartmentById_whenFound_returns200WithBody() throws Exception {
        when(departmentService.getById(1L)).thenReturn(makeDept(1L, "Engineering"));

        mockMvc.perform(get("/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    void getDepartmentById_whenNotFound_returns404() throws Exception {
        when(departmentService.getById(99L)).thenThrow(new DepartmentNotFoundException(99L));

        mockMvc.perform(get("/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Department not found with id: 99"));
    }

    // --- DELETE /departments/{id} ---

    @Test
    void deleteDepartment_whenSuccessful_returns204() throws Exception {
        doNothing().when(departmentService).delete(1L);

        mockMvc.perform(delete("/departments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDepartment_whenNotFound_returns404() throws Exception {
        doThrow(new DepartmentNotFoundException(99L)).when(departmentService).delete(99L);

        mockMvc.perform(delete("/departments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDepartment_whenHasEmployees_returns409() throws Exception {
        doThrow(new DepartmentDeletionException(1L)).when(departmentService).delete(1L);

        mockMvc.perform(delete("/departments/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // --- POST /departments/{deptId}/employees ---

    @Test
    void createEmployee_validRequest_returns201WithBody() throws Exception {
        Department dept = makeDept(1L, "Engineering");
        Employee saved = makeEmp(10L, "Alice", dept);

        when(employeeService.create(eq(1L), any())).thenReturn(saved);

        mockMvc.perform(post("/departments/1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.departmentId").value(1));
    }

    @Test
    void createEmployee_whenDepartmentNotFound_returns404() throws Exception {
        when(employeeService.create(eq(99L), any()))
                .thenThrow(new DepartmentNotFoundException(99L));

        mockMvc.perform(post("/departments/99/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\"}"))
                .andExpect(status().isNotFound());
    }

    // --- GET /departments/{deptId}/employees ---

    @Test
    void getEmployeesByDepartment_returnsListAnd200() throws Exception {
        Department dept = makeDept(1L, "Engineering");
        when(employeeService.getByDepartment(1L))
                .thenReturn(List.of(makeEmp(10L, "Alice", dept), makeEmp(11L, "Bob", dept)));

        mockMvc.perform(get("/departments/1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getEmployeesByDepartment_whenDepartmentNotFound_returns404() throws Exception {
        when(employeeService.getByDepartment(99L))
                .thenThrow(new DepartmentNotFoundException(99L));

        mockMvc.perform(get("/departments/99/employees"))
                .andExpect(status().isNotFound());
    }

    // --- GET /departments/{deptId}/employees/{empId} ---

    @Test
    void getEmployeeById_whenMatch_returns200() throws Exception {
        Department dept = makeDept(1L, "Engineering");
        Employee emp = makeEmp(10L, "Alice", dept);
        when(employeeService.getByIdAndDepartment(10L, 1L)).thenReturn(emp);

        mockMvc.perform(get("/departments/1/employees/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.departmentId").value(1));
    }

    @Test
    void getEmployeeById_whenEmployeeNotFound_returns404() throws Exception {
        when(employeeService.getByIdAndDepartment(99L, 1L))
                .thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/departments/1/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeById_whenDepartmentMismatch_returns400() throws Exception {
        when(employeeService.getByIdAndDepartment(10L, 2L))
                .thenThrow(new EmployeeDepartmentMismatchException(10L, 2L));

        mockMvc.perform(get("/departments/2/employees/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- DELETE /departments/{deptId}/employees/{empId} ---

    @Test
    void deleteEmployee_whenSuccessful_returns204() throws Exception {
        doNothing().when(employeeService).delete(1L, 10L);

        mockMvc.perform(delete("/departments/1/employees/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployee_whenMismatch_returns400() throws Exception {
        doThrow(new EmployeeDepartmentMismatchException(10L, 2L))
                .when(employeeService).delete(2L, 10L);

        mockMvc.perform(delete("/departments/2/employees/10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployee_whenNotFound_returns404() throws Exception {
        doThrow(new EmployeeNotFoundException(99L)).when(employeeService).delete(1L, 99L);

        mockMvc.perform(delete("/departments/1/employees/99"))
                .andExpect(status().isNotFound());
    }
}