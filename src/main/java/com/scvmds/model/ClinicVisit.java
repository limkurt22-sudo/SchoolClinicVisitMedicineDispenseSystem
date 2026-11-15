package com.scvmds.model;

public record ClinicVisit(
        int id,
        int studentUserId,
        String studentName,
        int nurseUserId,
        String complaint,
        String treatmentGiven,
        int medicineId,
        String medicineName,
        int quantityDispensed,
        String visitDate,
        String status
) {
    @Override
    public String toString(){
        return  String.format("(%d) [%d] %s (%s) COMPLAINT: %s %s (%d) %s [%d] Visit Date -> %s STATUS: %s", id, studentUserId, studentName, nurseUserId, complaint,
        treatmentGiven, medicineId, medicineName, quantityDispensed, visitDate, status);
    }
}