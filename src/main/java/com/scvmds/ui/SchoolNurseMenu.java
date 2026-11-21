package com.scvmds.ui;

import com.scvmds.dao.ActivityLogDao;
import com.scvmds.dao.ClinicVisitDao;
import com.scvmds.dao.MedicineDao;
import com.scvmds.dao.UserDao;
import com.scvmds.model.ClinicVisit;
import com.scvmds.model.Medicine;
import com.scvmds.model.User;

import java.util.List;
import java.util.Scanner;

public class SchoolNurseMenu {

    private final UserDao userDao;
    private final MedicineDao medicineDao;
    private final ClinicVisitDao visitDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public SchoolNurseMenu(UserDao userDao, MedicineDao medicineDao, ClinicVisitDao visitDao,
                           ActivityLogDao logDao, Scanner scanner) {
        this.userDao = userDao;
        this.medicineDao = medicineDao;
        this.visitDao = visitDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void display(User nurse) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n======================================");
            System.out.println(" ->> SCHOOL NURSE / CLINIC STAFF <<- ");
            System.out.println("======================================");
            System.out.println("1. Record Clinic Visit & Dispense Medicine"); //Main Function
            System.out.println("2. Search Student Clinic History");
            System.out.println("3. View All Clinic Visits");
            System.out.println("4. View Available Medicine Inventory");
            System.out.println("5. Logout");
            System.out.print("Choose an Option (1 - 5): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> recordClinicVisit(nurse);
                case "2" -> searchStudentHistory();
                case "3" -> displayVisitsTable(visitDao.getAllVisits(false), "ALL CLINIC VISITS");
                case "4" -> displayMedicineTable(medicineDao.getAllMedicines(true));
                case "5" -> {
                    logDao.logActivity(nurse.id(), nurse.username(), "NURSE_LOGOUT");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void recordClinicVisit(User nurse) {
        List<User> students = userDao.getAllUsers().stream()
                .filter(u -> "STUDENT".equalsIgnoreCase(u.roles()) && "APPROVED".equalsIgnoreCase(u.status()))
                .toList();

        if (students.isEmpty()) {
            System.out.println("-> [INFO] No approved student accounts found.");
            return;
        }

        System.out.println("\n--- APPROVED STUDENTS ---");
        students.forEach(s -> System.out.printf("ID: %-4d | Username: %s\n", s.id(), s.username()));

        int studentId = readInt("Enter Student ID (or 0 to cancel): ");
        if (studentId == 0) return;

        boolean validStudent = students.stream().anyMatch(s -> s.id() == studentId);
        if (!validStudent) {
            System.out.println("-> [ERROR] Invalid Student ID.");
            return;
        }

        System.out.print("Enter Complaint / Symptoms: ");
        String complaint = scanner.nextLine().trim();
        if (complaint.isEmpty()) {
            System.out.println("-> [ERROR] Complaint cannot be empty.");
            return;
        }

        System.out.print("Enter Treatment / First Aid Given: ");
        String treatment = scanner.nextLine().trim();
        if (treatment.isEmpty()) {
            System.out.println("-> [ERROR] Treatment cannot be empty.");
            return;
        }

        Integer medicineId = null;
        int quantity = 0;

        System.out.print("Dispense medicine? (Y/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            List<Medicine> meds = medicineDao.getAllMedicines(true);
            displayMedicineTable(meds);
            if (!meds.isEmpty()) {
                int selectedMedId = readInt("Enter Medicine ID (or 0 for none): ");
                if (selectedMedId > 0) {
                    Medicine selected = medicineDao.getMedicineById(selectedMedId);
                    if (selected == null || !"ACTIVE".equalsIgnoreCase(selected.status())) {
                        System.out.println("-> [ERROR] Invalid or inactive medicine ID.");
                        return;
                    }
                    quantity = readInt("Enter Quantity to Dispense (Available: " + selected.stockQuantity() + "): ");
                    if (quantity <= 0 || quantity > selected.stockQuantity()) {
                        System.out.println("-> [ERROR] Invalid dispense quantity.");
                        return;
                    }
                    medicineId = selectedMedId;
                }
            }
        }

        boolean success = visitDao.recordVisitAndDispense(studentId, nurse.id(), complaint, treatment, medicineId, quantity);
        if (success) {
            logDao.logActivity(nurse.id(), nurse.username(),
                    "Recorded visit for Student ID " + studentId + (medicineId != null ? " (Dispensed Med ID " + medicineId + " x" + quantity + ")" : ""));
            System.out.println("-> [SUCCESS] Clinic visit recorded successfully.");
        }
    }

    private void searchStudentHistory() {
        int studentId = readInt("Enter Student User ID to search: ");
        List<ClinicVisit> visits = visitDao.getVisitsByStudentId(studentId);
        displayVisitsTable(visits, "CLINIC HISTORY FOR STUDENT ID: " + studentId);
    }

    private void displayMedicineTable(List<Medicine> meds) {
        System.out.println("\n+-----+----------------------+---------------+-------+------------+------------+");
        System.out.printf("| %-3s | %-20s | %-13s | %-5s | %-10s | %-10s |\n",
                "ID", "Medicine Name", "Form", "Stock", "Expiry", "Status");
        System.out.println("+-----+----------------------+---------------+-------+------------+------------+");
        if (meds.isEmpty()) {
            System.out.println("| No medicines available.                                                      |");
        } else {
            for (Medicine m : meds) {
                System.out.printf("| %-3d | %-20s | %-13s | %-5d | %-10s | %-10s |\n",
                        m.id(), m.name(), m.dosageForm(), m.stockQuantity(), m.expirationDate(), m.status());
            }
        }
        System.out.println("+-----+----------------------+---------------+-------+------------+------------+");
    }

    private void displayVisitsTable(List<ClinicVisit> visits, String title) {
        System.out.println("\n=== " + title + " ===");
        if (visits.isEmpty()) {
            System.out.println("-> No clinic visit records found.");
            return;
        }
        System.out.println("+-----+---------------+--------------------+--------------------+--------------------+-----+---------------------+");
        System.out.printf("| %-3s | %-13s | %-18s | %-18s | %-18s | %-3s | %-19s |\n",
                "ID", "Student", "Complaint", "Treatment", "Medicine", "Qty", "Date");
        System.out.println("+-----+---------------+--------------------+--------------------+--------------------+-----+---------------------+");
        for (ClinicVisit v : visits) {
            System.out.printf("| %-3d | %-13s | %-18s | %-18s | %-18s | %-3d | %-19s |\n",
                    v.id(), v.studentName(), truncate(v.complaint(), 18), truncate(v.treatmentGiven(), 18),
                    truncate(v.medicineName(), 18), v.quantityDispensed(), v.visitDate());
        }
        System.out.println("+-----+---------------+--------------------+--------------------+--------------------+-----+---------------------+");
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
            }
        }
    }
}