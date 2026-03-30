package ie.tus.jeff.assignment2.exception;

public class DepartmentAlreadyExistsException extends ApplicationException {
    public DepartmentAlreadyExistsException(String name) {
        super("Department with name '" + name + "' already exists");
    }
}