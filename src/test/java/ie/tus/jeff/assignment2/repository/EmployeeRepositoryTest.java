package ie.tus.jeff.assignment2.repository;

import ie.tus.jeff.assignment2.model.Department;
import ie.tus.jeff.assignment2.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department engineering;

    @BeforeEach
    void setUp() {
        Department d = new Department();
        d.setName("Engineering");
        engineering = em.persistAndFlush(d);
    }

    private Employee persistEmployee(String name, Department dept) {
        Employee e = new Employee();
        e.setName(name);
        e.setDepartment(dept);
        return em.persistAndFlush(e);
    }

    @Test
    void existsByDepartmentId_whenEmployeesExist_returnsTrue() {
        persistEmployee("Alice", engineering);

        assertThat(employeeRepository.existsByDepartmentId(engineering.getId())).isTrue();
    }

    @Test
    void existsByDepartmentId_whenNoEmployees_returnsFalse() {
        assertThat(employeeRepository.existsByDepartmentId(engineering.getId())).isFalse();
    }

    @Test
    void existsByDepartmentId_forNonExistentDepartment_returnsFalse() {
        assertThat(employeeRepository.existsByDepartmentId(9999L)).isFalse();
    }

    @Test
    void findByDepartmentId_returnsOnlyEmployeesInThatDepartment() {
        Department hr = new Department();
        hr.setName("HR");
        hr = em.persistAndFlush(hr);

        persistEmployee("Alice", engineering);
        persistEmployee("Bob", engineering);
        persistEmployee("Carol", hr);

        List<Employee> result = employeeRepository.findByDepartmentId(engineering.getId());

        assertThat(result).hasSize(2)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    void findByDepartmentId_whenNoEmployeesInDepartment_returnsEmptyList() {
        assertThat(employeeRepository.findByDepartmentId(engineering.getId())).isEmpty();
    }
}