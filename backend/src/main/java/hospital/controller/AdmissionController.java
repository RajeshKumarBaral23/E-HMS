package hospital.controller;

import hospital.dto.AdmissionDTO;
import hospital.service.AdmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {
    @Autowired
    private AdmissionService service;

    @PostMapping
    public AdmissionDTO admit(@RequestBody AdmissionDTO dto) {
        return service.admitPatient(dto);
    }

    @PutMapping("/{id}/discharge")
    public AdmissionDTO discharge(@PathVariable Long id, @RequestParam String dischargeDate) {
        return service.dischargePatient(id, LocalDate.parse(dischargeDate));
    }

    @GetMapping("/patient/{patientId}")
    public List<AdmissionDTO> getByPatient(@PathVariable Long patientId) {
        return service.getAdmissionsByPatient(patientId);
    }
}

