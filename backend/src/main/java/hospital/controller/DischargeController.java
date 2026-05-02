package hospital.controller;

import hospital.dto.DischargeSummaryDTO;
import hospital.service.DischargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discharge")
public class DischargeController {
    @Autowired
    private DischargeService service;

    @GetMapping("/summary")
    public DischargeSummaryDTO getSummary(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return service.generateSummary(patientId, admissionId);
    }
}

