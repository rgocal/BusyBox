package com.jrummyapps.busybox.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.jrummyapps.android.util.Reflection;

import static com.jrummyapps.android.app.App.getContext;
import static com.jrummyapps.android.app.App.getPackageInfo;

/**
 * Created by yaroslavsudnik on 27.11.2017.
 */

final class Holders {

    static final class ApplicationHolder {
        @SuppressLint("StaticFieldLeak")
        static final Application APPLICATION;

        static {
            Application application = com.jrummyapps.android.app.App.getApp();
            if (application == null) {
                application = Reflection.invoke("android.app.ActivityThread", "currentApplication");
                if (application != null) {
                    com.jrummyapps.android.app.App.init(application);
                }
            }
            APPLICATION = application;
        }
    }

    static final class HandlerHolder {
        static final Handler HANDLER = new Handler(Looper.getMainLooper());
    }

    static final class DebuggableHolder {
        static final boolean DEBUGGABLE = (getPackageInfo().applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    static final class PackageInfoHolder {
        static final PackageInfo PACKAGE_INFO;

        static {
            try {
                PACKAGE_INFO = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                //noinspection UnnecessaryLocalVariable
                RuntimeException up = new RuntimeException();
                throw up;
            }
        }
    }

    static final class AppLabelHolder {
        static final String APP_LABEL =
                getPackageInfo().applicationInfo.loadLabel(getContext().getPackageManager()).toString();
    }

}