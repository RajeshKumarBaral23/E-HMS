package hospital.service;

import hospital.dto.DepartmentRequest;
import hospital.entity.Department;
import hospital.entity.Doctor;
import hospital.repository.DepartmentRepository;
import hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department not found"));
    }

    @Override
    public Department createDepartment(DepartmentRequest request) {
        Department d = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .phone(request.getPhone())
                .build();
        return departmentRepository.save(d);
    }

    @Override
    public Department updateDepartment(Long id, DepartmentRequest request) {
        Department d = departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department not found"));
        if (request.getName() != null) d.setName(request.getName());
        if (request.getDescription() != null) d.setDescription(request.getDescription());
        if (request.getPhone() != null) d.setPhone(request.getPhone());
        return departmentRepository.save(d);
    }

    @Override
    public boolean deleteDepartment(Long id) {
        // Check if any doctors reference this department
        boolean inUse = doctorRepository.findAll().stream()
                .map(Doctor::getDepartment)
                .anyMatch(dep -> dep != null && id.equals(dep.getId()));
        if (inUse) return false;
        Department d = departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department not found"));
        departmentRepository.delete(d);
        return true;
    }
}
