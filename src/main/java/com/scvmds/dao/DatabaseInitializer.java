package com.scvmds.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.scvmds.dao.DatabaseConnection.getConnection;

public class DatabaseInitializer {
    public static void initialize() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING'
            );
        """;

        String createMedicinesTable = """
            CREATE TABLE IF NOT EXISTS medicines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                description TEXT,
                dosage_form TEXT NOT NULL,
                stock_quantity INTEGER NOT NULL DEFAULT 0,
                expiration_date TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'ACTIVE'
            );
        """;

        String createClinicVisitsTable = """
            CREATE TABLE IF NOT EXISTS clinic_visits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_user_id INTEGER NOT NULL,
                nurse_user_id INTEGER NOT NULL,
                complaint TEXT NOT NULL,
                treatment_given TEXT NOT NULL,
                medicine_id INTEGER,
                quantity_dispensed INTEGER DEFAULT 0,
                visit_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                status TEXT NOT NULL DEFAULT 'VALID',
                FOREIGN KEY (student_user_id) REFERENCES users(id),
                FOREIGN KEY (nurse_user_id) REFERENCES users(id),
                FOREIGN KEY (medicine_id) REFERENCES medicines(id)
            );
        """;

        String createParentStudentLinksTable = """
            CREATE TABLE IF NOT EXISTS parent_student_links (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                parent_user_id INTEGER NOT NULL,
                student_user_id INTEGER NOT NULL,
                UNIQUE(parent_user_id, student_user_id),
                FOREIGN KEY (parent_user_id) REFERENCES users(id),
                FOREIGN KEY (student_user_id) REFERENCES users(id)
            );
        """;

        String createActivityLogsTable = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                username TEXT NOT NULL,
                action TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createMedicinesTable);
            stmt.execute(createClinicVisitsTable);
            stmt.execute(createParentStudentLinksTable);
            stmt.execute(createActivityLogsTable);
        } catch (SQLException e) {
            System.err.println("[ERROR] Database initialization failed: " + e.getMessage());
        }

        new UserDao();
        System.out.println(" -> Database tables verified.");
    }
}