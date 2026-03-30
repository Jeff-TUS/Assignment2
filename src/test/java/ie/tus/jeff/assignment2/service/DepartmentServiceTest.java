package ie.tus.jeff.assignment2.service;

import ie.tus.jeff.assignment2.exception.DepartmentAlreadyExistsException;
import ie.tus.jeff.assignment2.exception.DepartmentDeletionException;
import ie.tus.jeff.assignment2.exception.DepartmentNotFoundException;
import ie.tus.jeff.assignment2.model.Department;
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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department engineering;

    @BeforeEach
    void setUp() {
        engineering = new Department();
        engineering.setId(1L);
        engineering.setName("Engineering");
    }

    // --- create ---

    @Test
    void create_whenNameIsUnique_savesDepartmentAndReturnsIt() {
        when(departmentRepository.existsByName("Engineering")).thenReturn(false);
        when(departmentRepository.save(engineering)).thenReturn(engineering);

        Department result = departmentService.create(engineering);

        assertThat(result).isEqualTo(engineering);
        verify(departmentRepository).save(engineering);
    }

    @Test
    void create_whenNameAlreadyExists_throwsDepartmentAlreadyExistsException() {
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(engineering))
                .isInstanceOf(DepartmentAlreadyExistsException.class)
                .hasMessageContaining("Engineering");

        verify(departmentRepository, never()).save(any());
    }

    // --- getAll ---

    @Test
    void getAll_returnsAllDepartments() {
        Department hr = new Department();
        hr.setId(2L);
        hr.setName("HR");

        when(departmentRepository.findAll()).thenReturn(List.of(engineering, hr));

        List<Department> result = departmentService.getAll();

        assertThat(result).hasSize(2).contains(engineering, hr);
    }

    @Test
    void getAll_whenNoDepartments_returnsEmptyList() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        assertThat(departmentService.getAll()).isEmpty();
    }

    // --- getById ---

    @Test
    void getById_whenDepartmentExists_returnsDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(engineering));

        Department result = departmentService.getById(1L);

        assertThat(result).isEqualTo(engineering);
    }

    @Test
    void getById_whenDepartmentDoesNotExist_throwsDepartmentNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(99L))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    void delete_whenDepartmentExistsAndHasNoEmployees_deletesDepartment() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.existsByDepartmentId(1L)).thenReturn(false);

        departmentService.delete(1L);

        verify(departmentRepository).deleteById(1L);
    }

    @Test
    void delete_whenDepartmentDoesNotExist_throwsDepartmentNotFoundException() {
        when(departmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> departmentService.delete(99L))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("99");

        verify(departmentRepository, never()).deleteById(any());
    }

    @Test
    void delete_whenDepartmentHasEmployees_throwsDepartmentDeletionException() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.existsByDepartmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(DepartmentDeletionException.class)
                .hasMessageContaining("1");

        verify(departmentRepository, never()).deleteById(any());
    }
}