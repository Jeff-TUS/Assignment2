package ie.tus.jeff.assignment2.exception;

public class DepartmentDeletionException extends ApplicationException {
    public DepartmentDeletionException(Long id) {
        super("Cannot delete department with id " + id + " because it has employees");
    }
}