package com.example.appsentinel.scanner;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class AppScanner {

    public static List<PackageInfo> getUserInstalledApps(Context context){

        PackageManager pm = context.getPackageManager();

        List<PackageInfo> packages =
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        List<PackageInfo> userApps = new ArrayList<>();

        for(PackageInfo pkg : packages){

            if((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                userApps.add(pkg);
            }

        }

        return userApps;
    }

}