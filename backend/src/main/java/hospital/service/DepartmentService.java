package hospital.service;

import hospital.dto.DepartmentRequest;
import hospital.entity.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Department getDepartmentById(Long id);
    Department createDepartment(DepartmentRequest request);
    Department updateDepartment(Long id, DepartmentRequest request);
    /**
     * Delete department by id.
     * @return true if deleted, false if department has linked doctors and cannot be deleted.
     */
    boolean deleteDepartment(Long id);
}
