package com.scvmds.dao;

import com.scvmds.model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}