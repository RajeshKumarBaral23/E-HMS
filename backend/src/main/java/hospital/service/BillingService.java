package hospital.service;

import hospital.entity.Billing;

public interface BillingService {
    Billing generateBill(Long appointmentId);
    Billing generateAggregatedBill(Long patientId, Long admissionId);
    Billing markAsPaid(Long billId);
    java.util.List<Billing> listBillsForCurrentUser();
    java.util.List<Billing> getBillsByPatient(Long patientId);
}
