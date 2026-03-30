package ie.tus.jeff.assignment2.dto;

public class EmployeeDTO {
    private Long id;
    private String name;
    private Long departmentId;

    public EmployeeDTO() {}

    public EmployeeDTO(Long id, String name, Long departmentId) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}