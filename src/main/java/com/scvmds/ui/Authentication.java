package com.scvmds.ui;

import com.scvmds.dao.ActivityLogDao;
import com.scvmds.dao.ClinicVisitDao;
import com.scvmds.dao.MedicineDao;
import com.scvmds.dao.UserDao;
import com.scvmds.model.User;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final MedicineDao medicineDao;
    private final ClinicVisitDao visitDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, MedicineDao medicineDao, ActivityLogDao logDao, ClinicVisitDao visitDao, Scanner scanner) {
        this.userDao = userDao;
        this.medicineDao = medicineDao;
        this.visitDao = visitDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("===================================================");
            System.out.println(" SCHOOL CLINIC VISIT AND MEDICINE DISPENSING SYSTEM");
            System.out.println("===================================================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Select option: ");
            int select = scanner.nextInt();
            scanner.nextLine();
            System.out.println("===================================================");

            switch (select) {
                case 1:
                    System.out.print("Enter new Username: ");
                    String newUser = scanner.nextLine().trim();
                    System.out.print("Enter Password: ");
                    String newPass = scanner.nextLine();

                    System.out.print("Select Role: [1] Admin [2] School Nurse [3] Student [4] Parent: ");
                    String roleChoice = scanner.nextLine().trim();
                    String role;

                    switch (roleChoice) {
                        case "1" -> role = "ADMIN";
                        case "2" -> role = "SCHOOL NURSE";
                        case "3" -> role = "STUDENT";
                        case "4" -> role = "PARENT";
                        default -> role = null;
                    }
                    if (role != null) {
                        userDao.registerUser(newUser, newPass, role);
                    } else {
                        System.out.println("-> [ERROR] Invalid Role Selection!");
                    }
                    break;
                case 2:
                    handleLogin();
                    break;
                case 3:
                    System.out.println("THANK YOU FOR VISITING THE SYSTEM. GOOD BYE!");
                    running = false;
                    break;
                default:
                    System.out.println("-> [ERROR] Invalid option. Please select 1, 2, or 3.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user != null) {
            logDao.logActivity(user.id(), user.username(), "USER_LOGIN");
            System.out.println(" > Welcome " + user.username() + " | ROLE: " + user.roles());

            switch (user.roles().toUpperCase()) {
                case "ADMIN" -> new AdminMenu(userDao, medicineDao, visitDao, logDao, scanner).display(user);
                case "SCHOOL NURSE" -> new SchoolNurseMenu(userDao, medicineDao, visitDao, logDao, scanner).display(user);
                case "STUDENT" -> new ParentandStudentMenu(visitDao, logDao, scanner).displayStudent(user);
                case "PARENT" -> new ParentandStudentMenu(visitDao, logDao, scanner).displayParent(user);
                default -> System.out.println("-> [ERROR] Account roles not found or unrecognized!");
            }
        }
    }
}