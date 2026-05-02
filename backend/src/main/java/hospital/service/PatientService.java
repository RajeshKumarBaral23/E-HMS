package hospital.service;

import hospital.dto.PatientRequest;
import hospital.entity.Patient;

import java.util.List;

public interface PatientService {
    Patient createPatient(PatientRequest request);
    List<Patient> getAllPatients();
    Patient getPatientById(Long id);
    Patient getCurrentPatient();
    Patient updatePatient(Long id, PatientRequest request);
    Patient updateCurrentPatient(PatientRequest request);
    void deletePatient(Long id);
    java.util.Map<String, Object> getPatientHistory(Long id);
}
