package com.scvmds;

import com.scvmds.dao.ActivityLogDao;
import com.scvmds.dao.ClinicVisitDao;
import com.scvmds.dao.MedicineDao;
import com.scvmds.dao.DatabaseInitializer;
import com.scvmds.dao.UserDao;
import com.scvmds.ui.Authentication;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();

        Scanner scanner = new Scanner(System.in);
        MedicineDao medicineDao = new MedicineDao();
        ClinicVisitDao visitDao = new ClinicVisitDao();
        ActivityLogDao logDao = new ActivityLogDao();
        UserDao userDao = new UserDao();

        Authentication auth = new Authentication(userDao, medicineDao, logDao, visitDao, scanner);
        auth.start();
        scanner.close();
    }
}