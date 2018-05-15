package org.coreocto.dev.apkexporter.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by John on 11/13/2016.
 */

public class AndroidUtil {
    public static boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public static String getAppName(PackageManager packageManager, ApplicationInfo applicationInfo) {
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    public static String getAppVer(PackageManager packageManager, String packageName) throws PackageManager.NameNotFoundException {
        return packageManager.getPackageInfo(packageName, 0).versionName;
    }
}
