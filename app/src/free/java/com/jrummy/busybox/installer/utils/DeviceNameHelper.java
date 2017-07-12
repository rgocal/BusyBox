package com.jrummy.busybox.installer.utils;

import android.os.Build;

import com.jaredrummler.android.device.DeviceName;
import com.jrummyapps.android.app.App;

public class DeviceNameHelper {

    private static volatile DeviceNameHelper singleton;

    public static DeviceNameHelper getSingleton() {
        if (singleton == null) {
            synchronized (DeviceNameHelper.class) {
                if (singleton == null) {
                    singleton = new DeviceNameHelper();
                }
            }
        }
        return singleton;
    }

    private String name;

    /**
     * Get the current device name. If the device is unknown then a request is made to retrieve the
     * device information.
     *
     * @return The name of the current device.
     */
    public String getName() {
        if (name == null) {
            name = DeviceName.getDeviceName(Build.DEVICE, Build.MODEL, null);
            if (name == null) {
                name = DeviceName.getDeviceName();
                // This is an unknown/unpopular device.
                // Attempt to get the name from Google's maintained device list.
                DeviceName.with(App.getContext()).request(new DeviceName.Callback() {

                    @Override public void onFinished(DeviceName.DeviceInfo info, Exception error) {
                        if (error != null || info == null) {
                            return;
                        }
                        name = info.getName();
                    }
                });
            }
        }
        return name;
    }

}