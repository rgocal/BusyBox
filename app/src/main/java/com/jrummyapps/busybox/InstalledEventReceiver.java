package com.jrummyapps.busybox;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.jrummyapps.android.analytics.Analytics;

import java.util.List;

public class InstalledEventReceiver extends BroadcastReceiver {

    private static final String EXTRA_APP_NAME     = "extraAppName";
    private static final String EXTRA_PACKAGE_NAME = "extraPackageName";

    public static final long VERIFY_INSTALL_APP_DELAY_IN_MILLIS = 10 * 60 * 1000;

    public static PendingIntent pendingIntent(Context context, String appName, String packageName) {
        Intent intent = new Intent(context, InstalledEventReceiver.class)
            .putExtra(EXTRA_APP_NAME, appName)
            .putExtra(EXTRA_PACKAGE_NAME, packageName);

        return PendingIntent.getBroadcast(context, packageName.hashCode(), intent, 0);
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            String appName = intent.getStringExtra(EXTRA_APP_NAME);
            String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);

            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

            for (PackageInfo packageInfo : packages) {
                if (packageInfo.packageName.equals(packageName)) {
                    Analytics.newEvent("installations_from_apps_tab")
                        .put("installed", appName + " (" + packageName + ")").log();
                    return;
                }
            }
        }
    }

}