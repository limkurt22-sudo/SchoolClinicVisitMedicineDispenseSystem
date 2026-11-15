package com.scvmds.ui;

import com.scvmds.dao.ActivityLogDao;
import com.scvmds.dao.ClinicVisitDao;
import com.scvmds.model.ClinicVisit;
import com.scvmds.model.User;

import java.util.List;
import java.util.Scanner;

public class ParentandStudentMenu {
    private final ClinicVisitDao visitDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public ParentandStudentMenu(ClinicVisitDao visitDao, ActivityLogDao logDao, Scanner scanner) {
        this.visitDao = visitDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void displayStudent(User student) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n======================================");
            System.out.println(" ->> STUDENT HEALTH PORTAL <<- ");
            System.out.println("======================================");
            System.out.println("1. View My Clinic Visit & Medicine History");
            System.out.println("2. Logout");
            System.out.print("Choose an Option (1 - 2): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> printVisitSummary(visitDao.getVisitsByStudentId(student.id()), "MY CLINIC VISIT HISTORY");
                case "2" -> {
                    logDao.logActivity(student.id(), student.username(), "STUDENT_LOGOUT");
                    System.out.println("-> Logging out. Goodbye!");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    public void displayParent(User parent) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n======================================");
            System.out.println(" ->> PARENT / GUARDIAN PORTAL <<- ");
            System.out.println("======================================");
            System.out.println("1. View Child's Clinic Visit Summary");
            System.out.println("2. Logout");
            System.out.print("Choose an Option (1 - 2): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    List<Integer> childIds = visitDao.getLinkedStudentIds(parent.id());
                    if (childIds.isEmpty()) {
                        System.out.println("-> [INFO] No student account is currently linked to your Parent account. Contact the Clinic Admin.");
                    } else {
                        for (int childId : childIds) {
                            printVisitSummary(visitDao.getVisitsByStudentId(childId), "CHILD CLINIC HISTORY (Student ID: " + childId + ")");
                        }
                    }
                }
                case "2" -> {
                    logDao.logActivity(parent.id(), parent.username(), "PARENT_LOGOUT");
                    System.out.println("-> Logging out. Goodbye!");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option.");
            }
        }
    }

    private void printVisitSummary(List<ClinicVisit> visits, String title) {
        System.out.println("\n=== " + title + " ===");
        if (visits.isEmpty()) {
            System.out.println("-> No clinic visit records found.");
            return;
        }
        for (ClinicVisit v : visits) {
            System.out.println("--------------------------------------------------");
            System.out.println("Visit ID          : " + v.id());
            System.out.println("Date              : " + v.visitDate());
            System.out.println("Complaint         : " + v.complaint());
            System.out.println("Treatment Given   : " + v.treatmentGiven());
            System.out.println("Medicine Received : " + v.medicineName() + " (Qty: " + v.quantityDispensed() + ")");
        }
        System.out.println("--------------------------------------------------");
    }
}