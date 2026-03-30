package ie.tus.jeff.assignment2.repository;

import ie.tus.jeff.assignment2.model.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DepartmentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department persistDepartment(String name) {
        Department d = new Department();
        d.setName(name);
        return em.persistAndFlush(d);
    }

    @Test
    void existsByName_whenNameExists_returnsTrue() {
        persistDepartment("Engineering");

        assertThat(departmentRepository.existsByName("Engineering")).isTrue();
    }

    @Test
    void existsByName_whenNameDoesNotExist_returnsFalse() {
        assertThat(departmentRepository.existsByName("Nonexistent")).isFalse();
    }

    @Test
    void existsByName_isCaseSensitive() {
        persistDepartment("Engineering");
        // H2 default collation is case-sensitive; verify this behaviour is consistent
        assertThat(departmentRepository.existsByName("engineering")).isFalse();
    }

    @Test
    void save_withUniqueName_persistsDepartment() {
        Department d = new Department();
        d.setName("Finance");
        Department saved = departmentRepository.save(d);

        assertThat(saved.getId()).isNotNull();
        assertThat(em.find(Department.class, saved.getId()).getName()).isEqualTo("Finance");
    }

    @Test
    void findAll_returnsAllPersistedDepartments() {
        persistDepartment("A");
        persistDepartment("B");

        assertThat(departmentRepository.findAll()).hasSize(2);
    }

    @Test
    void deleteById_removesDepartment() {
        Department d = persistDepartment("Temp");
        departmentRepository.deleteById(d.getId());

        assertThat(departmentRepository.findById(d.getId())).isEmpty();
    }
}