package hospital.service;

import hospital.dto.DoctorRequest;
import hospital.entity.Doctor;

import java.util.List;

public interface DoctorService {
    Doctor createDoctor(DoctorRequest request);
    List<Doctor> getAllDoctors();
    Doctor getDoctorById(Long id);
    Doctor updateDoctor(Long id, DoctorRequest request);
    void deleteDoctor(Long id);
}
