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

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.eventbus.Events;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.monetize.RequestPremiumEvent;
import com.jrummyapps.busybox.monetize.RequestRemoveAds;
import com.jrummyapps.busybox.monetize.ShowInterstitalAdEvent;

public class BusyboxSuccessDialog extends DialogFragment {

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    setCancelable(false);
    return new AlertDialog.Builder(getActivity())
        .setCancelable(false)
        .setTitle(R.string.success)
        .setMessage(R.string.install_success_message)
        .setNegativeButton(R.string.pro_version, new DialogInterface.OnClickListener() {

          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Events.post(new RequestPremiumEvent());
            Analytics.newEvent("request in-app purchase").put("prodcut", "pro_version").log();
          }
        })
        .setNeutralButton(R.string.remove_ads, new DialogInterface.OnClickListener() {

          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Events.post(new RequestRemoveAds());
            Analytics.newEvent("request in-app purchase").put("prodcut", "remove_ads").log();
          }
        })
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Events.post(new ShowInterstitalAdEvent());
            Analytics.newEvent("show interstitial ad").log();
          }
        })
        .create();
  }
}
