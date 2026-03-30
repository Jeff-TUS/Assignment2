package ie.tus.jeff.assignment2.exception;

public class DepartmentNotFoundException extends ApplicationException {
    public DepartmentNotFoundException(Long id) {
        super("Department not found with id: " + id);
    }
}
