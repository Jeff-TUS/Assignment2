package ie.tus.jeff.assignment2.service;

import ie.tus.jeff.assignment2.exception.DepartmentNotFoundException;
import ie.tus.jeff.assignment2.exception.EmployeeDepartmentMismatchException;
import ie.tus.jeff.assignment2.exception.EmployeeNotFoundException;
import ie.tus.jeff.assignment2.model.Department;
import ie.tus.jeff.assignment2.model.Employee;
import ie.tus.jeff.assignment2.repository.DepartmentRepository;
import ie.tus.jeff.assignment2.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Department engineering;
    private Employee alice;

    @BeforeEach
    void setUp() {
        engineering = new Department();
        engineering.setId(1L);
        engineering.setName("Engineering");

        alice = new Employee();
        alice.setId(10L);
        alice.setName("Alice");
        alice.setDepartment(engineering);
    }

    // --- create ---

    @Test
    void create_whenDepartmentExists_setsAndSavesEmployee() {
        Employee newEmp = new Employee();
        newEmp.setName("Bob");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(engineering));
        when(employeeRepository.save(newEmp)).thenReturn(newEmp);

        Employee result = employeeService.create(1L, newEmp);

        assertThat(result.getDepartment()).isEqualTo(engineering);
        verify(employeeRepository).save(newEmp);
    }

    @Test
    void create_whenDepartmentDoesNotExist_throwsDepartmentNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.create(99L, new Employee()))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("99");

        verify(employeeRepository, never()).save(any());
    }

    // --- getAll ---

    @Test
    void getAll_returnsAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(alice));

        List<Employee> result = employeeService.getAll();

        assertThat(result).containsExactly(alice);
    }

    @Test
    void getAll_whenNoEmployees_returnsEmptyList() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        assertThat(employeeService.getAll()).isEmpty();
    }

    // --- getByDepartment ---

    @Test
    void getByDepartment_whenDepartmentExists_returnsEmployees() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.findByDepartmentId(1L)).thenReturn(List.of(alice));

        List<Employee> result = employeeService.getByDepartment(1L);

        assertThat(result).containsExactly(alice);
    }

    @Test
    void getByDepartment_whenDepartmentDoesNotExist_throwsDepartmentNotFoundException() {
        when(departmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> employeeService.getByDepartment(99L))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getByDepartment_whenDepartmentHasNoEmployees_returnsEmptyList() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.findByDepartmentId(1L)).thenReturn(List.of());

        assertThat(employeeService.getByDepartment(1L)).isEmpty();
    }

    // --- getByIdAndDepartment ---

    @Test
    void getByIdAndDepartment_whenEmployeeBelongsToDepartment_returnsEmployee() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(alice));

        Employee result = employeeService.getByIdAndDepartment(10L, 1L);

        assertThat(result).isEqualTo(alice);
    }

    @Test
    void getByIdAndDepartment_whenEmployeeNotFound_throwsEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getByIdAndDepartment(99L, 1L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getByIdAndDepartment_whenEmployeeBelongsToDifferentDepartment_throwsMismatchException() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(alice));
        // alice.getDepartment().getId() == 1, but we ask for deptId == 2

        assertThatThrownBy(() -> employeeService.getByIdAndDepartment(10L, 2L))
                .isInstanceOf(EmployeeDepartmentMismatchException.class)
                .hasMessageContaining("10")
                .hasMessageContaining("2");
    }

    // --- delete ---

    @Test
    void delete_whenEmployeeBelongsToDepartment_deletesEmployee() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(alice));

        employeeService.delete(1L, 10L);

        verify(employeeRepository).delete(alice);
    }

    @Test
    void delete_whenEmployeeNotFound_throwsEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.delete(1L, 99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");

        verify(employeeRepository, never()).delete(any());
    }

    @Test
    void delete_whenEmployeeBelongsToDifferentDepartment_throwsMismatchException() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> employeeService.delete(2L, 10L))
                .isInstanceOf(EmployeeDepartmentMismatchException.class)
                .hasMessageContaining("10")
                .hasMessageContaining("2");

        verify(employeeRepository, never()).delete(any());
    }
}