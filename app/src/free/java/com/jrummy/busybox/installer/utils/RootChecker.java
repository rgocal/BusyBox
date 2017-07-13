package com.jrummy.busybox.installer.utils;

import android.os.AsyncTask;

import com.jrummyapps.android.roottools.checks.RootCheck;

import org.greenrobot.eventbus.EventBus;

public class RootChecker extends AsyncTask<Void, Void, RootCheck> {

    public static void execute() {
        new RootChecker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override protected RootCheck doInBackground(Void... params) {
        return RootCheck.getInstance();
    }

    @Override protected void onPostExecute(RootCheck result) {
        EventBus.getDefault().post(result);
    }

}