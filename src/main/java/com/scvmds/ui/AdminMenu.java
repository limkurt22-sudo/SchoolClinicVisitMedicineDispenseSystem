package com.scvmds.ui;

import com.scvmds.dao.ActivityLogDao;
import com.scvmds.dao.ClinicVisitDao;
import com.scvmds.dao.MedicineDao;
import com.scvmds.dao.UserDao;
import com.scvmds.model.ActivityLog;
import com.scvmds.model.ClinicVisit;
import com.scvmds.model.Medicine;
import com.scvmds.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final MedicineDao medicineDao;
    private final ClinicVisitDao visitDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, MedicineDao medicineDao, ClinicVisitDao visitDao,
                     ActivityLogDao logDao, Scanner scanner) {
        this.userDao = userDao;
        this.medicineDao = medicineDao;
        this.visitDao = visitDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=========================");
            System.out.println(" ->> ADMIN DASHBOARD <<- ");
            System.out.println("=========================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User");
            System.out.println("7. Manage Medicines");
            System.out.println("8. Manage Visit Records");
            System.out.println("9. Delete Erroneous Entries");
            System.out.println("10. Generate Reports");
            System.out.println("11. View Activity Logs");
            System.out.println("12. Link Parent to Student");
            System.out.println("13. Logout");
            System.out.print("Choose an Option (1 - 13): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUser();
                case "3" -> approveUser(admin);
                case "4" -> addUser(admin);
                case "5" -> updateUser(admin);
                case "6" -> deleteUser(admin);
                case "7" -> manageMedicines(admin);
                case "8" -> manageVisitRecords(admin);
                case "9" -> deleteErroneousEntries(admin);
                case "10" -> generateReports(admin);
                case "11" -> viewAuditLogsMenu();
                case "12" -> linkParentToStudent(admin);
                case "13" -> {
                    logDao.logActivity(admin.id(), admin.username(), "USER_LOGOUT");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void viewAuditLogsMenu() {
        boolean viewing = true;
        while (viewing) {
            System.out.println("\n========================================");
            System.out.println("========= SYSTEM AUDIT LOGS ============");
            System.out.println("========================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Dashboard");
            System.out.print("Select Option: ");

            int choice = readIntInput("");
            switch (choice) {
                case 1 -> displayLogTable(logDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
                case 2 -> {
                    int targetUserId = readIntInput("Enter User ID to filter: ");
                    displayLogTable(logDao.getLogsByUserId(targetUserId), "LOGS FOR USER ID: " + targetUserId);
                }
                case 3 -> viewing = false;
                default -> System.out.println("[ERROR] Invalid option.");
            }
        }
    }

    private void displayLogTable(List<ActivityLog> logs, String title) {
        System.out.println("\n==================================================================================================");
        System.out.println("                                   " + title);
        System.out.println("==================================================================================================");
        if (logs.isEmpty()) {
            System.out.println("-> No Audit Logs Found.");
            System.out.println("==================================================================================================");
            return;
        }
        String border = "+-------+----------+--------------------+---------------------------------------------------------------------+----------------------+";
        System.out.println(border);
        System.out.printf("| %-5s | %-8s | %-18s | %-69s | %-20s |\n", "ID", "User ID", "Username", "Action", "Timestamp");
        System.out.println(border);

        for (ActivityLog log : logs) {
            System.out.printf("| %-5s | %-8s | %-18s | %-69s | %-20s |\n",
                    log.id(), log.user_id(), log.username(), log.action(), log.timestamp());
        }
        System.out.println(border);
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

    private void displayUserTable(List<User> users, String title) {
        System.out.println("\n================== " + title + " ==================");
        if (users.isEmpty()) {
            System.out.println("-> No Records Found");
            return;
        }
        String border = "+-----+-----------------+---------------------------+------------+";
        System.out.println(border);
        System.out.printf("| %-3s | %-15s | %-25s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User u : users) {
            System.out.printf("| %-3s | %-15s | %-25s | %-10s |\n",
                    u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println(border);
    }

    private void viewAllUser() {
        displayUserTable(userDao.getAllUsers(), "ALL REGISTERED USERS");
    }

    private void viewPendingUser() {
        displayUserTable(userDao.getPendingUser(), "PENDING APPROVALS");
    }

    private void approveUser(User currentAdmin) {
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()) return;

        int userID = readIntInput("Enter User ID to Approve (or 0 to cancel): ");
        if (userID == 0) {
            System.out.println("-> Operation Cancelled");
            return;
        }
        boolean exists = pendingUsers.stream().anyMatch(u -> u.id() == userID);
        if (!exists) {
            System.out.println("[ERROR] User ID not found.");
            return;
        }
        userDao.approveUser(userID);
        logDao.logActivity(currentAdmin.id(), currentAdmin.username(), "Admin approved account for User ID " + userID);
        System.out.println("-> [SUCCESS] User ID " + userID + " approved successfully.");
    }

    private void updateUser(User currentAdmin) {
        List<User> users = userDao.getAllUsers();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) return;

        int userId = readIntInput("Enter Selected ID to Update (or 0 to Cancel): ");
        if (userId == 0) return;

        if (userId == currentAdmin.id()) {
            System.out.println("-> [WARNING] Modifying your own active admin account is restricted.");
        }

        String newRole = readValidRole();
        if (newRole == null) return;

        String newStatus = readValidStatus();
        if (newStatus == null) return;

        userDao.updateUserAndStatus(userId, newRole, newStatus);
        logDao.logActivity(currentAdmin.id(), currentAdmin.username(), "Admin changed role/status of User " + userId);
        System.out.println("-> [SUCCESS] User ID " + userId + " updated successfully.");
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter Role (ADMIN / SCHOOL NURSE / STUDENT /PARENT): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0") || role.equals("CANCEL")) return null;
            if (role.equals("ADMIN") || role.equals("SCHOOL NURSE") || role.equals("STUDENT") || role.equals("PARENT")) {
                return role;
            }
            System.out.println("[ERROR] Invalid Role.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter Status (APPROVED / PENDING): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0") || status.equals("CANCEL")) return null;
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("[ERROR] Invalid Status.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid numeric ID.");
            }
        }
    }

    private void addUser(User currentAdmin) {
        System.out.println("========== ADD NEW USER ===========");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) return;

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        if (password.isEmpty()) return;

        String role = readValidRole();
        if (role == null) return;

        userDao.registerUser(username, password, role);
        userDao.approveUser(userDao.getAllUsers().stream().filter(u -> u.username().equals(username)).findFirst().get().id());
        logDao.logActivity(currentAdmin.id(), currentAdmin.username(), "Admin created user: " + username);
        System.out.println("-> [SUCCESS] User '" + username + "' added successfully.");
    }

    private void deleteUser(User currentAdmin) {
        List<User> users = userDao.getAllUsers();
        displayUserTable(users, "SELECT USER TO DELETE");

        if (users.isEmpty()) return;

        int userId = readIntInput("Enter User ID to Delete (or 0 to Cancel): ");
        if (userId == 0) return;

        if (userId == currentAdmin.id()) {
            System.out.println("-> [WARNING] You cannot delete your own active Admin account!");
            return;
        }




        userDao.deleteUser(userId);
        logDao.logActivity(currentAdmin.id(), currentAdmin.username(), "Admin deleted User ID " + userId);
        System.out.println("-> [SUCCESS] User ID " + userId + " deleted successfully.");
    }

    private void displayVisitsTable(List<ClinicVisit> visits, String title) {
        System.out.println("\n=========================================================================================================================");
        System.out.println("                                            " + title);
        System.out.println("=========================================================================================================================");
        if (visits == null || visits.isEmpty()) {
            System.out.println("-> No clinic visit records found.");
            return;
        }

        String border = "+-----+---------------+--------------------+--------------------+--------------------+-----+---------------------+----------+";
        System.out.println(border);
        System.out.printf("| %-3s | %-13s | %-18s | %-18s | %-18s | %-3s | %-19s | %-8s |\n",
                "ID", "Student", "Complaint", "Treatment", "Medicine", "Qty", "Date", "Status");
        System.out.println(border);

        for (ClinicVisit v : visits) {
            System.out.printf("| %-3d | %-13s | %-18s | %-18s | %-18s | %-3d | %-19s | %-8s |\n",
                    v.id(),
                    truncate(v.studentName(), 13),
                    truncate(v.complaint(), 18),
                    truncate(v.treatmentGiven(), 18),
                    truncate(v.medicineName(), 18),
                    v.quantityDispensed(),
                    truncate(v.visitDate(), 19),
                    truncate(v.status(), 8));
        }
        System.out.println(border);
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private void manageMedicines(User admin) {

        int expiredCount = medicineDao.deactivateExpiredMedicines();
        if (expiredCount > 0) {
            logDao.logActivity(admin.id(), admin.username(), "Auto-deactivated " + expiredCount + " expired medicine(s)");
            System.out.println("-> [INFO] " + expiredCount + " expired medicine(s) automatically set to INACTIVE.");
        }
        boolean managing = true;
        while (managing) {
            System.out.println("\n=== MANAGE MEDICINES ===");
            System.out.println("1. View All Medicines");
            System.out.println("2. Add New Medicine");
            System.out.println("3. Update Medicine Details / Stock");
            System.out.println("4. Deactivate / Activate Medicine");
            System.out.println("5. Back");
            int opt = readIntInput("Select Option: ");

            switch (opt) {
                case 1 -> medicineDao.getAllMedicines(false).forEach(m ->
                        System.out.printf("ID: %-3d | %-18s | %-12s | Stock: %-4d | Exp: %-10s | %s\n",
                                m.id(), m.name(), m.dosageForm(), m.stockQuantity(), m.expirationDate(), m.status()));

                case 2 -> {
                    System.out.print("Medicine Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Description: ");
                    String desc = scanner.nextLine().trim();
                    System.out.print("Dosage Form (Tablet/Syrup/etc.): ");
                    String form = scanner.nextLine().trim();
                    int stock = readIntInput("Initial Stock: ");
                    System.out.print("Expiration Date (YYYY-MM-DD): ");
                    String exp = scanner.nextLine().trim();

                    if (medicineDao.addMedicine(name, desc, form, stock, exp)) {
                        logDao.logActivity(admin.id(), admin.username(), "Added medicine: " + name);
                        System.out.println("-> [SUCCESS] Medicine added.");
                    }
                }
                case 3 -> {
                    medicineDao.getAllMedicines(false).forEach(m ->
                            System.out.printf("ID: %-3d | %-18s | %-12s | Stock: %-4d | Exp: %-10s | %s\n",
                                    m.id(), m.name(), m.dosageForm(), m.stockQuantity(), m.expirationDate(), m.status()));

                    int id = readIntInput("Enter Medicine ID to update: ");
                    Medicine ex = medicineDao.getMedicineById(id);
                    if (ex == null) {
                        System.out.println("-> [ERROR] Medicine not found.");
                        break;
                    }
                    System.out.print("New Name (" + ex.name() + "): ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) name = ex.name();

                    System.out.print("New Dosage Form (" + ex.dosageForm() + "): ");
                    String form = scanner.nextLine().trim();
                    if (form.isEmpty()) form = ex.dosageForm();

                    int stock = readIntInput("New Stock (Current: " + ex.stockQuantity() + "): ");
                    System.out.print("New Expiry (" + ex.expirationDate() + "): ");
                    String exp = scanner.nextLine().trim();
                    if (exp.isEmpty()) exp = ex.expirationDate();

                    if (medicineDao.updateMedicineStockAndDetails(id, name, form, stock, exp)) {
                        logDao.logActivity(admin.id(), admin.username(), "Updated medicine ID " + id);
                        System.out.println("-> [SUCCESS] Medicine updated.");
                    }
                }
                case 4 -> {
                    int id = readIntInput("Enter Medicine ID: ");
                    System.out.print("Status (ACTIVE / INACTIVE): ");
                    String st = scanner.nextLine().trim().toUpperCase();
                    if (medicineDao.setMedicineStatus(id, st)) {
                        logDao.logActivity(admin.id(), admin.username(), "Set medicine ID " + id + " to " + st);
                        System.out.println("-> [SUCCESS] Status updated.");
                    }
                }
                case 5 -> managing = false;
            }
        }
    }

    private void manageVisitRecords(User admin) {
        List<ClinicVisit> visits = visitDao.getAllVisits(true);
        if (visits.isEmpty()) {
            System.out.println("-> No visits found.");
            return;
        }
        visits.forEach(v -> System.out.printf("ID: %-3d | Student: %-12s | Complaint: %-15s | Treatment: %-15s | Date: %s\n",
                v.id(), v.studentName(), v.complaint(), v.treatmentGiven(), v.visitDate()));

        int visitId = readIntInput("Enter Visit ID to Edit (or 0 to go back): ");
        if (visitId == 0) return;

        System.out.print("Updated Complaint: ");
        String complaint = scanner.nextLine().trim();
        System.out.print("Updated Treatment: ");
        String treatment = scanner.nextLine().trim();

        if (visitDao.updateVisitRecord(visitId, complaint, treatment)) {
            logDao.logActivity(admin.id(), admin.username(), "Edited clinic visit ID " + visitId);
            System.out.println("-> [SUCCESS] Visit updated.");
        }
    }

    private void deleteErroneousEntries(User admin) {
        System.out.println("\n=== DELETE ERRONEOUS ENTRIES ===");
        System.out.println("1. Delete Visit Record");
        System.out.println("2. Delete Medicine");
        int opt = readIntInput("Select: ");

        if (opt == 1) {
            List<ClinicVisit> viewAllClinicVisit = visitDao.getAllVisits(true);
            displayVisitsTable(viewAllClinicVisit, "ALL CLINIC VISITS");

            if (viewAllClinicVisit.isEmpty()) {
                System.out.println("-> No visit records available to delete.");
                return;
            }

            int id = readIntInput("Enter Visit ID to delete (or 0 to cancel): ");
            if (id == 0) {
                System.out.println("-> Operation cancelled.");
                return;
            }

            boolean exists = viewAllClinicVisit.stream().anyMatch(v -> v.id() == id);
            if (!exists) {
                System.out.println("-> [ERROR] Visit ID " + id + " not found.");
                return;
            }

            if (visitDao.deleteVisitRecord(id)) {
                logDao.logActivity(admin.id(), admin.username(), "Deleted visit ID " + id);
                System.out.println("-> [SUCCESS] Visit deleted.");
            } else {
                System.out.println("-> [ERROR] Failed to delete visit ID " + id + ".");
            }
        } else if (opt == 2) {
            List<Medicine> viewAllMedicine = medicineDao.getAllMedicines(true);
            displayMedicineTable(viewAllMedicine);
            int id = readIntInput("Enter Medicine ID to delete: ");
            if (medicineDao.deleteMedicine(id)) {
                logDao.logActivity(admin.id(), admin.username(), "Deleted medicine ID " + id);
                System.out.println("-> [SUCCESS] Medicine deleted.");
            }
        }
    }

    private void generateReports(User admin) {
        System.out.print("Enter Month & Year (YYYY-MM, e.g., 2026-10): ");
        String ym = scanner.nextLine().trim();
        visitDao.printMonthlySummaryReport(ym);
        logDao.logActivity(admin.id(), admin.username(), "Generated clinic report for " + ym);
    }

    private void linkParentToStudent(User admin) {
        List<User> allUsers = userDao.getAllUsers();

        java.util.Set<Integer> linkedParentIds = new java.util.HashSet<>();
        java.util.Set<Integer> linkedStudentIds = new java.util.HashSet<>();

        String checkSql = "SELECT parent_user_id, student_user_id FROM parent_student_links";
        try (java.sql.Connection conn = com.scvmds.dao.DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(checkSql)) {
            while (rs.next()) {
                linkedParentIds.add(rs.getInt("parent_user_id"));
                linkedStudentIds.add(rs.getInt("student_user_id"));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[ERROR] Failed to load existing links: " + e.getMessage());
            return;
        }

        List<User> unlinkedParents = allUsers.stream()
                .filter(u -> "PARENT".equalsIgnoreCase(u.roles()) && !linkedParentIds.contains(u.id()))
                .toList();

        List<User> unlinkedStudents = allUsers.stream()
                .filter(u -> "STUDENT".equalsIgnoreCase(u.roles()) && !linkedStudentIds.contains(u.id()))
                .toList();

        displayUserTable(unlinkedParents, "UNLINKED PARENTS");
        displayUserTable(unlinkedStudents, "UNLINKED STUDENTS");

        if (unlinkedParents.isEmpty() || unlinkedStudents.isEmpty()) {
            System.out.println("-> [INFO] Need at least one unlinked Parent and one unlinked Student to create a link.");
            return;
        }

        int parentId = readIntInput("Enter Parent User ID (or 0 to cancel): ");
        if (parentId == 0) return;

        int studentId = readIntInput("Enter Student User ID (or 0 to cancel): ");
        if (studentId == 0) return;

        boolean validParent = unlinkedParents.stream().anyMatch(u -> u.id() == parentId);
        boolean validStudent = unlinkedStudents.stream().anyMatch(u -> u.id() == studentId);

        if (!validParent || !validStudent) {
            System.out.println("-> [ERROR] Please select valid IDs from the Unlinked Parents and Unlinked Students tables.");
            return;
        }

        if (visitDao.linkParentToStudent(parentId, studentId, "LINKED")) {
            logDao.logActivity(admin.id(), admin.username(), "Linked Parent ID " + parentId + " to Student ID " + studentId);
            System.out.println("-> [SUCCESS] Parent linked to Student.");
        } else {
            System.out.println("-> [ERROR] Failed to link accounts (link may already exist).");
        }
    }
}