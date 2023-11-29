package com.jrummyapps.busybox.activities;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.radiant.activity.RadiantAppCompatActivity;
import com.jrummyapps.android.util.Intents;
import com.jrummyapps.android.widget.observablescrollview.ObservableGridView;
import com.jrummyapps.busybox.BuildConfig;
import com.jrummyapps.busybox.InstalledEventReceiver;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.adapters.AppListAdapter;
import com.jrummyapps.busybox.models.RootAppInfo;

import java.util.ArrayList;
import java.util.List;

import static com.jrummyapps.busybox.InstalledEventReceiver.VERIFY_INSTALL_APP_DELAY_IN_MILLIS;

public class CrossPromoActivity extends RadiantAppCompatActivity
        implements AppListAdapter.OnClickListener {

    private static final String REMOTE_CONFIG_KEY = "rootapps";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cross_promo);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(R.string.app_name);
        }

        initRemoteConfig();
        fetch();
    }

    @Override public int getThemeResId() {
        return getRadiant().getActionBarTheme();
    }

    private void initRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.defaults);
    }

    private void fetch() {
        long cacheExpiration = 3600; // 1 hour in seconds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        }
                        showAppList();
                    }
                });
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAppList() {
        AppListAdapter adapter = new AppListAdapter(this);

        List<RootAppInfo> crossPromoAppInfos = null;
        try {
            crossPromoAppInfos = getFeaturedRootApps();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        if (crossPromoAppInfos == null) {
            crossPromoAppInfos = new ArrayList<>();
        }
        adapter.setAppInfos(crossPromoAppInfos);

        ObservableGridView gridView = (ObservableGridView) getViewById(R.id.list);
        gridView.setAdapter(adapter);
    }

    /**
     * @return featured root apps
     */
    @WorkerThread
    private ArrayList<RootAppInfo> getFeaturedRootApps() {
        TypeToken typeToken = new TypeToken<ArrayList<RootAppInfo>>() {
        };
        return new Gson().fromJson(mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY
    ), typeToken.getType());
    }

    @Override public void onClick(RootAppInfo rootAppInfo) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
                VERIFY_INSTALL_APP_DELAY_IN_MILLIS, InstalledEventReceiver.pendingIntent(this,
                rootAppInfo.getAppName(), rootAppInfo.getPackageName()));

        if (rootAppInfo.getUrl() != null) {
            Analytics.newEvent("clicks_on_cross_promo").put("clicks",
                    rootAppInfo.getAppName() + " (" + rootAppInfo.getPackageName() + ")").log();
            startActivity(Intents.newOpenWebBrowserIntent(rootAppInfo.getUrl()));
        }
    }

}
