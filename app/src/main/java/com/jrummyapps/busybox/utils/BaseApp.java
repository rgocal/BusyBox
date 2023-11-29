package com.jrummyapps.busybox.utils;

import android.content.res.Resources;

import com.jrummyapps.android.radiant.Radiant;
import com.jrummyapps.android.radiant.RadiantResources;

/**
 * Created by yaroslavsudnik on 27.11.2017.
 */

public class BaseApp extends App {

    private RadiantResources resources;
    private boolean initialized;

    @Override public void onCreate() {
        super.onCreate();
        Radiant.with(this, super.getResources());
        initialized = true;
    }

    @Override public Resources getResources() {
        if (!initialized) {
            // Don't give ContentProviders the RadiantResources
            return super.getResources();
        }
        if (resources == null) {
            resources = new RadiantResources(Radiant.getInstance(), super.getResources());
        }
        return resources;
    }

}
