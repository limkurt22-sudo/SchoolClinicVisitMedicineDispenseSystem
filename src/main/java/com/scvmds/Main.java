package com.scvmds;

import com.scvmds.dao.UserDao;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static  void main(String[] args){
        System.out.println("===================================================");
        System.out.println(" SCHOOL CLINIC VISIT AND MEDICINE DESPENSING SYSTEM");
        System.out.println("===================================================");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Select option: ");
        int select = sc.nextInt();
        System.out.println("===================================================");

        switch (select){
            case 1:
                break;
            case 2:
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
            case 3:
                System.out.println("THANK YOU FOR VISITING THE SYSTEM. GOOD BYE!");
                break;
        }
    }
}