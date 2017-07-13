package com.jrummy.busybox.installer.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.util.Intents;
import com.jrummyapps.busybox.R;

public class RootCheckDialog extends DialogFragment {

    public static final String TAG = "RootCheckDialog";

    private static final String EXTRA_DEVICE_NAME = "extraDeviceName";

    private static final String ROOT_CHECK_URL = "https://play.google.com/store/" +
        "apps/details?id=com.jrummyapps.rootchecker";

    public static void show(Activity activity, String deviceName) {
        RootCheckDialog dialog = new RootCheckDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_DEVICE_NAME, deviceName);
        dialog.setArguments(args);
        dialog.show(activity.getFragmentManager(), TAG);
    }

    private String deviceName;

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments().containsKey(EXTRA_DEVICE_NAME)) {
            deviceName = getArguments().getString(EXTRA_DEVICE_NAME);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.dialog_warning_text)
            .setPositiveButton(R.string.continue_text, null);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_root_check, null);

        TextView uiDeviceNameTv = (TextView) view.findViewById(R.id.device_name);
        uiDeviceNameTv.setText(Html.fromHtml(getString(R.string.this_device_is_not,
            deviceName)));

        TextView uiInstallTv = (TextView) view.findViewById(R.id.install_button);
        uiInstallTv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Analytics.newEvent("Root Check Install").log();
                startActivity(Intents.newOpenWebBrowserIntent(ROOT_CHECK_URL));
            }
        });
        builder.setView(view);

        return builder.create();
    }

}