package hospital.service;

import hospital.dto.AdmissionDTO;
import hospital.entity.Admission;
import hospital.entity.Bed;
import hospital.entity.Patient;
import hospital.repository.AdmissionRepository;
import hospital.repository.BedRepository;
import hospital.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdmissionServiceImpl implements AdmissionService {
    @Autowired private AdmissionRepository admissionRepo;
    @Autowired private BedRepository bedRepo;
    @Autowired private PatientRepository patientRepo;

    @Override
    public AdmissionDTO admitPatient(AdmissionDTO dto) {
        Patient patient = patientRepo.findById(dto.getPatientId()).orElseThrow();
        Bed bed = bedRepo.findById(dto.getBedId()).orElseThrow();
        if (bed.isOccupied()) throw new RuntimeException("Bed already occupied");
        bed.setOccupied(true);
        bedRepo.save(bed);

        Admission admission = new Admission();
        admission.setPatient(patient);
        admission.setBed(bed);
        admission.setAdmissionDate(dto.getAdmissionDate());
        admission.setRoomChargePerDay(dto.getRoomChargePerDay());
        admissionRepo.save(admission);

        dto.setId(admission.getId());
        return dto;
    }

    @Override
    public AdmissionDTO dischargePatient(Long admissionId, LocalDate dischargeDate) {
        Admission admission = admissionRepo.findById(admissionId).orElseThrow();
        admission.setDischargeDate(dischargeDate);
        admission.getBed().setOccupied(false);
        bedRepo.save(admission.getBed());
        admissionRepo.save(admission);
        return toDTO(admission);
    }

    @Override
    public List<AdmissionDTO> getAdmissionsByPatient(Long patientId) {
        return admissionRepo.findAll().stream()
            .filter(a -> a.getPatient().getId().equals(patientId))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    private AdmissionDTO toDTO(Admission a) {
        AdmissionDTO dto = new AdmissionDTO();
        dto.setId(a.getId());
        dto.setPatientId(a.getPatient().getId());
        dto.setBedId(a.getBed().getId());
        dto.setAdmissionDate(a.getAdmissionDate());
        dto.setDischargeDate(a.getDischargeDate());
        dto.setRoomChargePerDay(a.getRoomChargePerDay());
        return dto;
    }
}

