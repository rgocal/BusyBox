package com.jrummyapps.busybox.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;

import com.jrummyapps.android.app.ActivityMonitor;
import com.jrummyapps.android.prefs.Prefs;

/**
 * Created by yaroslavsudnik on 27.11.2017.
 */

public class App extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    private static Application app;

    /**
     * Check if {@link #init(Application)} has been called.
     *
     * <p>Note: The static application instance will not be set until all
     * {@link android.content.ContentProvider providers} have been initialized.</p>
     *
     * <p>See:</p>
     * <ul>
     * <li>https://code.google.com/p/android/issues/detail?id=8727</li>
     * <li>http://stackoverflow.com/questions/9873669</li>
     * </ul>
     *
     * @return {@code true} if the static application instance is not null.
     */
    public static boolean initialized() {
        return App.app != null;
    }

    /**
     * A convenience method to get the default shared preferences.
     *
     * @return The {@link Prefs#getInstance()}
     */
    public static Prefs prefs() {
        return Prefs.getInstance();
    }

    /**
     * Sets the application instance and starts the {@link ActivityMonitor}. This should only be called once.
     *
     * @param app
     *     The application instance
     */
    public static void init(@NonNull Application app) {
        if (App.app == null) {
            App.app = app;
            ActivityMonitor.register(app);
        }
    }

    /**
     * Set the current application. This is already done if your application inherits from or is {@link com.jrummyapps.android.app.App}.
     *
     * @param app
     *     The application instance
     */
    public static void setApp(@NonNull Application app) {
        App.app = app;
    }

    /**
     * Get the current application.
     *
     * @param <T>
     *     The Application subclass implemented for the application
     * @return The application instance which is instantiated before any of the application's components.
     */
    public static <T extends Application> T getApp() {
        return (T) app;
    }

    /**
     * Get the application context.
     *
     * @return The application context.
     */
    public static Application getContext() {
        return Holders.ApplicationHolder.APPLICATION;
    }

    /**
     * Get the UI thread handler.
     *
     * @return a {@link Handler} to post messages to the main thread.
     */
    public static Handler getHandler() {
        return Holders.HandlerHolder.HANDLER;
    }

    /**
     * Check if the application is debuggable.
     *
     * @return {@code true} if the application is debuggable
     */
    public static boolean isDebuggable() {
        return Holders.DebuggableHolder.DEBUGGABLE;
    }

    /**
     * Retrieve overall information about this application.
     *
     * @return the overall information about this application package.
     */
    @NonNull public static PackageInfo getPackageInfo() {
        return Holders.PackageInfoHolder.PACKAGE_INFO;
    }

    /**
     * Get the current {@link Activity}.
     *
     * @return the current foreground Activity if available, may be null. Caller is expected to check lifecycle state
     * before using, see Activity.isFinishing().
     */
    public static Activity getActivity() {
        return ActivityMonitor.getInstance().getCurrentActivity();
    }

    /**
     * Retrieve the current textual label associated with this app.
     *
     * @return a string containing the application's label.
     */
    @NonNull public static String getAppLabel() {
        return Holders.AppLabelHolder.APP_LABEL;
    }

    @Override public void onCreate() {
        super.onCreate();
        init(this);
    }



}
