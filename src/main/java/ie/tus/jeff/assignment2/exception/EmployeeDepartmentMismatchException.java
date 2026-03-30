package ie.tus.jeff.assignment2.exception;

public class EmployeeDepartmentMismatchException extends ApplicationException {
    public EmployeeDepartmentMismatchException(Long empId, Long deptId) {
        super("Employee with id " + empId + " does not belong to department " + deptId);
    }
}