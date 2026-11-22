package com.scvmds.dao;

import com.scvmds.model.Medicine;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeParseException;

public class MedicineDao {

    public boolean addMedicine(String name, String description, String dosageForm, int stockQuantity, String expirationDate) {
        String sql = "INSERT INTO medicines(name, description, dosage_form, stock_quantity, expiration_date, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, dosageForm);
            pstmt.setInt(4, stockQuantity);
            pstmt.setString(5, expirationDate);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to add medicine: " + e.getMessage());
            return false;
        }
    }

    public List<Medicine> getAllMedicines(boolean activeOnly) {
        List<Medicine> list = new ArrayList<>();
        String sql = activeOnly
                ? "SELECT * FROM medicines WHERE status = 'ACTIVE' ORDER BY name ASC"
                : "SELECT * FROM medicines ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Medicine(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("dosage_form"),
                        rs.getInt("stock_quantity"),
                        rs.getString("expiration_date"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch medicines: " + e.getMessage());
        }
        return list;
    }

    public Medicine getMedicineById(int id) {
        String sql = "SELECT * FROM medicines WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Medicine(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("dosage_form"),
                            rs.getInt("stock_quantity"),
                            rs.getString("expiration_date"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch medicine: " + e.getMessage());
        }
        return null;
    }

    public boolean updateMedicineStockAndDetails(int id, String name, String dosageForm, int newStock, String expDate) {
        String sql = "UPDATE medicines SET name = ?, dosage_form = ?, stock_quantity = ?, expiration_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dosageForm);
            pstmt.setInt(3, newStock);
            pstmt.setString(4, expDate);
            pstmt.setInt(5, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to update medicine: " + e.getMessage());
            return false;
        }
    }

    public boolean setMedicineStatus(int id, String status) {
        String sql = "UPDATE medicines SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.toUpperCase());
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to update medicine status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMedicine(int id) {
        String sql = "DELETE FROM medicines WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] Cannot delete medicine (may be referenced in visit logs): " + e.getMessage());
            return false;
        }
    }

    public int deactivateExpiredMedicines() {
        int deactivatedCount = 0;
        LocalDate today = LocalDate.now();
        DateTimeFormatter flexibleDateFormatter = new DateTimeFormatterBuilder()
                .appendPattern("uuuu-M-d")
                .toFormatter()
                .withResolverStyle(ResolverStyle.SMART);

        for (Medicine med : getAllMedicines(true)) {
            if ("INACTIVE".equalsIgnoreCase(med.status())) {
                continue;
            }
            String expStr = med.expirationDate();
            if (expStr == null || expStr.isBlank()) {
                continue;
            }

            LocalDate expiryDate = parseExpirationDate(expStr.trim(), med.id(), flexibleDateFormatter);
            if (expiryDate == null) {
                continue;
            }


            if (!expiryDate.isAfter(today)) {
                if (setMedicineStatus(med.id(), "INACTIVE")) {
                    deactivatedCount++;
                }
            }
        }
        return deactivatedCount;
    }

    private LocalDate parseExpirationDate(String rawDate, int medId, DateTimeFormatter formatter) {
        try {
            return LocalDate.parse(rawDate, formatter);
        } catch (DateTimeParseException ex) {
            String[] parts = rawDate.split("-");
            if (parts.length == 3 && ("02".equals(parts[1]) || "2".equals(parts[1]))) {
                try {
                    int year = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[2]);
                    if (day >= 29) {
                        LocalDate firstOfFeb = LocalDate.of(year, 2, 1);
                        LocalDate clamped = firstOfFeb.withDayOfMonth(firstOfFeb.lengthOfMonth());
                        System.err.println("[WARN] Adjusted non-leap Feb date for Medicine ID " + medId
                                + " from " + rawDate + " to " + clamped);
                        return clamped;
                    }
                } catch (NumberFormatException ignored) {
                    // Fall through to invalid format warning
                }
            }
            System.err.println("[WARN] Invalid expiration date format for Medicine ID " + medId + ": " + rawDate);
            return null;
        }
    }
}