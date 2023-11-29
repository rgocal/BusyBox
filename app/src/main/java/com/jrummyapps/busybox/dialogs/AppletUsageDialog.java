/*
 * Copyright (C) 2016 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jrummyapps.busybox.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.LruCache;
import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.radiant.Radiant;
import com.jrummyapps.android.roottools.box.BusyBox;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.utils.Utils;
import java.lang.ref.WeakReference;
import org.json.JSONException;
import org.json.JSONObject;

public class AppletUsageDialog extends DialogFragment {

  static final LruCache<String, String> APPLET_USAGE_CACHE = new LruCache<String, String>(15) {

    private String json;

    @Override protected String create(String key) {
      if (BusyBox.getInstance().exists()) {
        String usage = BusyBox.getInstance().getUsage(key);
        if (!TextUtils.isEmpty(usage)) {
          return usage;
        }
      }
      if (json == null) {
        json = Utils.readRaw(R.raw.busybox_applets);
      }
      try {
        return new JSONObject(json).optString(key, null);
      } catch (JSONException e) {
        return null;
      }
    }
  };

  public static void show(Activity activity, String applet) {
    new AppletHelpTask(activity, applet).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  public static void show(Activity activity, String applet, String help) {
    AppletUsageDialog dialog = new AppletUsageDialog();
    Bundle args = new Bundle();
    args.putString("applet_name", applet);
    args.putString("applet_help", help);
    dialog.setArguments(args);
    dialog.show(activity.getFragmentManager(), "AppletUsageDialog");
    Analytics.newEvent("dialog_applet_usage").put("applet", applet).log();
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialog.Builder(getActivity())
        .setTitle(getArguments().getString("applet_name"))
        .setMessage(getArguments().getString("applet_help"))
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .create();
  }

  @Override public void onStart() {
    super.onStart();
    ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE)
        .setTextColor(Radiant.getInstance(getActivity()).accentColor());
  }

  static final class AppletHelpTask extends AsyncTask<Void, Void, String> {

    private final WeakReference<Activity> activityRef;
    private final String applet;

    AppletHelpTask(Activity activity, String name) {
      activityRef = new WeakReference<>(activity);
      applet = name;
    }

    @Override protected String doInBackground(Void... params) {
      return APPLET_USAGE_CACHE.get(applet);
    }

    @Override protected void onPostExecute(String help) {
      Activity activity = activityRef.get();
      if (activity == null || activity.isFinishing()) {
        return;
      }
      AppletUsageDialog.show(activity, applet, help);
    }

  }

}
