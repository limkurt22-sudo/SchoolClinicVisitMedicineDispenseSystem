package com.scvmds;

import com.scvmds.dao.DatabaseInitializer;
import com.scvmds.dao.UserDao;
import com.scvmds.ui.Authentication;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();

        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        Authentication auth = new Authentication(userDao, scanner);
        auth.start();
        scanner.close();
    }
}