package com.scvmds.ui;

import com.scvmds.dao.UserDao;
import com.scvmds.model.User;

import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner){
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("=========================");
            System.out.println(" ->> ADMIN DASHBOARD <<- ");
            System.out.println("=========================");
            System.out.println("1. View All User");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User role / Status");
            System.out.println("6. Delete User");
            System.out.println("7. Manage Medicines");
            System.out.println("8. Manage Visit Records");
            System.out.println("9. Delete Erroneous Entries");
            System.out.println("10. Generate Reports");
            System.out.print("Choose an Option ( 1 - 10): ");
            String choice = scanner.nextLine().trim();

            switch (choice){

            }

        }
    }
}