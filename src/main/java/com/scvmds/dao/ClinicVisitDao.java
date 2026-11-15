package com.scvmds.dao;

import com.scvmds.model.ClinicVisit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClinicVisitDao {

    public boolean recordVisitAndDispense(int studentUserId, int nurseUserId, String complaint,
                                          String treatmentGiven, Integer medicineId, int quantityDispensed) {
        String insertVisitSql = """
            INSERT INTO clinic_visits (student_user_id, nurse_user_id, complaint, treatment_given, medicine_id, quantity_dispensed, status)
            VALUES (?, ?, ?, ?, ?, ?, 'VALID')
        """;
        String updateStockSql = "UPDATE medicines SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (medicineId != null && quantityDispensed > 0) {
                    try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSql)) {
                        stockStmt.setInt(1, quantityDispensed);
                        stockStmt.setInt(2, medicineId);
                        stockStmt.setInt(3, quantityDispensed);
                        int updated = stockStmt.executeUpdate();
                        if (updated == 0) {
                            conn.rollback();
                            System.out.println("-> [ERROR] Insufficient medicine stock.");
                            return false;
                        }
                    }
                }

                try (PreparedStatement visitStmt = conn.prepareStatement(insertVisitSql)) {
                    visitStmt.setInt(1, studentUserId);
                    visitStmt.setInt(2, nurseUserId);
                    visitStmt.setString(3, complaint);
                    visitStmt.setString(4, treatmentGiven);
                    if (medicineId != null) {
                        visitStmt.setInt(5, medicineId);
                    } else {
                        visitStmt.setNull(5, Types.INTEGER);
                    }
                    visitStmt.setInt(6, quantityDispensed);
                    visitStmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to record clinic visit: " + e.getMessage());
            return false;
        }
    }

    public List<ClinicVisit> getAllVisits(boolean includeVoided) {
        List<ClinicVisit> visits = new ArrayList<>();
        String filter = includeVoided ? "" : "WHERE cv.status = 'VALID'";
        String sql = """
            SELECT cv.id, cv.student_user_id, u.username AS student_name, cv.nurse_user_id,
                   cv.complaint, cv.treatment_given, cv.medicine_id,
                   COALESCE(m.name, 'None') AS medicine_name, cv.quantity_dispensed,
                   cv.visit_date, cv.status
            FROM clinic_visits cv
            JOIN users u ON cv.student_user_id = u.id
            LEFT JOIN medicines m ON cv.medicine_id = m.id
            """ + filter + " ORDER BY cv.visit_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                visits.add(mapRowToVisit(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch clinic visits: " + e.getMessage());
        }
        return visits;
    }

    public List<ClinicVisit> getVisitsByStudentId(int studentUserId) {
        List<ClinicVisit> visits = new ArrayList<>();
        String sql = """
            SELECT cv.id, cv.student_user_id, u.username AS student_name, cv.nurse_user_id,
                   cv.complaint, cv.treatment_given, cv.medicine_id,
                   COALESCE(m.name, 'None') AS medicine_name, cv.quantity_dispensed,
                   cv.visit_date, cv.status
            FROM clinic_visits cv
            JOIN users u ON cv.student_user_id = u.id
            LEFT JOIN medicines m ON cv.medicine_id = m.id
            WHERE cv.student_user_id = ? AND cv.status = 'VALID'
            ORDER BY cv.visit_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    visits.add(mapRowToVisit(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch student visits: " + e.getMessage());
        }
        return visits;
    }

    public boolean updateVisitRecord(int visitId, String complaint, String treatmentGiven) {
        String sql = "UPDATE clinic_visits SET complaint = ?, treatment_given = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, complaint);
            pstmt.setString(2, treatmentGiven);
            pstmt.setInt(3, visitId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to update visit record: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteVisitRecord(int visitId) {
        String sql = "DELETE FROM clinic_visits WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to delete visit record: " + e.getMessage());
            return false;
        }
    }

    public boolean linkParentToStudent(int parentUserId, int studentUserId) {
        String sql = "INSERT OR IGNORE INTO parent_student_links(parent_user_id, student_user_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentUserId);
            pstmt.setInt(2, studentUserId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to link parent to student: " + e.getMessage());
            return false;
        }
    }

    public List<Integer> getLinkedStudentIds(int parentUserId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT student_user_id FROM parent_student_links WHERE parent_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("student_user_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to get linked students: " + e.getMessage());
        }
        return ids;
    }

    public void printMonthlySummaryReport(String yearMonth) {
        String sql = """
            SELECT COUNT(*) AS total_visits,
                   COUNT(DISTINCT student_user_id) AS unique_students,
                   COALESCE(SUM(quantity_dispensed), 0) AS total_meds_dispensed
            FROM clinic_visits
            WHERE strftime('%Y-%m', visit_date) = ? AND status = 'VALID'
        """;

        String topMedsSql = """
            SELECT m.name, SUM(cv.quantity_dispensed) AS total_qty
            FROM clinic_visits cv
            JOIN medicines m ON cv.medicine_id = m.id
            WHERE strftime('%Y-%m', cv.visit_date) = ? AND cv.status = 'VALID'
            GROUP BY m.id, m.name
            ORDER BY total_qty DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("\n=======================================================");
            System.out.println("   CLINIC VISIT & DISPENSING REPORT (" + yearMonth + ")");
            System.out.println("=======================================================");

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, yearMonth);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Total Clinic Visits      : " + rs.getInt("total_visits"));
                        System.out.println("Unique Students Treated  : " + rs.getInt("unique_students"));
                        System.out.println("Total Medicines Dispensed: " + rs.getInt("total_meds_dispensed"));
                    }
                }
            }

            System.out.println("\n--- Medicine Dispensing Breakdown ---");
            try (PreparedStatement medStmt = conn.prepareStatement(topMedsSql)) {
                medStmt.setString(1, yearMonth);
                try (ResultSet rs = medStmt.executeQuery()) {
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        System.out.printf("  * %-25s : %d units\n", rs.getString("name"), rs.getInt("total_qty"));
                    }
                    if (!hasData) {
                        System.out.println("  -> No medicines dispensed during this period.");
                    }
                }
            }
            System.out.println("=======================================================\n");
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to generate report: " + e.getMessage());
        }
    }

    private ClinicVisit mapRowToVisit(ResultSet rs) throws SQLException {
        return new ClinicVisit(
                rs.getInt("id"),
                rs.getInt("student_user_id"),
                rs.getString("student_name"),
                rs.getInt("nurse_user_id"),
                rs.getString("complaint"),
                rs.getString("treatment_given"),
                rs.getInt("medicine_id"),
                rs.getString("medicine_name"),
                rs.getInt("quantity_dispensed"),
                rs.getString("visit_date"),
                rs.getString("status")
        );
    }
}