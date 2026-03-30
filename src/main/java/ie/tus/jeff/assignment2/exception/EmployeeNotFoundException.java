package ie.tus.jeff.assignment2.exception;

public class EmployeeNotFoundException extends ApplicationException {
    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }
}