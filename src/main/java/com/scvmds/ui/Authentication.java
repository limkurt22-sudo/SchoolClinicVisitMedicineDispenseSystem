package com.scvmds.ui;

import com.scvmds.dao.UserDao;
import com.scvmds.model.User;

import java.util.Scanner;

public class Authentication {

        private final UserDao userDao;
        private final Scanner sc;

        public Authentication(UserDao userDao, Scanner scanner){
            this.userDao = userDao;
            this.sc = scanner;
        }

        public void start(){
            System.out.println("===================================================");
            System.out.println(" SCHOOL CLINIC VISIT AND MEDICINE DESPENSING SYSTEM");
            System.out.println("===================================================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Select option: ");
            int select = sc.nextInt();
            sc.nextLine();
            System.out.println("===================================================");

            switch (select){
                case 1:
                    System.out.print("Enter new Username: ");
                    String newUser = sc.nextLine().trim();
                    System.out.print("Enter Password: ");
                    String newPass = sc.nextLine();

                    System.out.print("Select Role: [1] Admin [2] School Nurse [3] Student [4] Parent: ");
                    String roleChoice = sc.nextLine().trim();
                    String role;

                    switch (roleChoice) {
                        case "1":
                            role = "ADMIN";
                            break;
                        case "2":
                            role = "SCHOOL NURSE";
                            break;
                        case "3":
                            role = "STUDENT";
                            break;
                        case "4":
                            role = "PARENT";
                            break;
                        default:
                            role = null;
                            break;
                    }
                    if (role != null) {
                        userDao.registerUser(newUser, newPass, role);
                    } else {
                        System.out.println("Invalid Role Selection!");
                    }
                    break;
                case 2:
                    handleLogin();
                    break;
                case 3:
                    System.out.println("THANK YOU FOR VISITING THE SYSTEM. GOOD BYE!");
                    break;
            }
        }
    private void handleLogin(){
        System.out.print("Enter Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        UserDao userDao = new UserDao();

        User user = userDao.login(username, password);

        if (user != null){
            System.out.println(" > Welcome " + user.username() + " | ROLE: " + user.roles());

            switch (user.roles()){
                case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
                default -> System.out.println("Account roles not found!");
            }
        }
    }
}

