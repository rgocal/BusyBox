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
 */

package com.jrummy.busybox.installer.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.animations.Technique;
import com.jrummyapps.android.app.App;
import com.jrummyapps.android.prefs.Prefs;
import com.jrummyapps.android.util.DeviceUtils;
import com.jrummyapps.android.util.Jot;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.activities.SettingsActivity;
import com.jrummyapps.busybox.utils.Monetize;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends com.jrummyapps.busybox.activities.MainActivity
    implements BillingProcessor.IBillingHandler {

    private static final String EXTRA_URI_KEY = "extra_web_link";

    public static Intent linkIntent(Context context, String link) {
        return new Intent(context, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_URI_KEY, link);
    }

    InterstitialAd   interstitialAd;
    BillingProcessor bp;

    private AdView[] adViewTiers;

    private int currentAdViewIndex;

    private RelativeLayout adContainer;

    private InterstitialAd[] interstitialsTabAd;
    private InterstitialAd[] interstitialsSettingsAd;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1915343032510958/9622649453");

        adContainer = (RelativeLayout) findViewById(R.id.ad_view);
        bp = new BillingProcessor(this, Monetize.decrypt(Monetize.ENCRYPTED_LICENSE_KEY), this);

        if (Prefs.getInstance().get("loaded_purchases_from_google", true)) {
            Prefs.getInstance().save("loaded_purchases_from_google", false);
            bp.loadOwnedPurchasesFromGoogle();
        }

        if (!Monetize.isAdsRemoved()) {
            currentAdViewIndex = 0;
            adViewTiers = new AdView[getResources().getStringArray(R.array.banners_id).length];
            setupBanners();
            loadInterstitialAd();

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override public void onPageSelected(int position) {
                    showTabInterstitials();
                }

                @Override public void onPageScrollStateChanged(int state) {
                }
            });

            interstitialsTabAd = new InterstitialAd[getResources()
                .getStringArray(R.array.tabs_interstitials_id).length];
            interstitialsSettingsAd = new InterstitialAd[getResources()
                .getStringArray(R.array.settings_interstitials_id).length];

            setupTabInterstitialsAd();
            setupSettingsInterstitialsAd();
        } else {
            adContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        for (AdView adView : adViewTiers) {
            if (adView != null) {
                adView.pause();
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (AdView adView : adViewTiers) {
            if (adView != null) {
                adView.resume();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (bp != null) {
            bp.release();
        }
        for (AdView adView : adViewTiers) {
            if (adView != null) {
                adView.destroy();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bp.handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_remove_ads).setVisible(!Monetize.isAdsRemoved());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            showSettingsInterstitials();
            return true;
        } else if (itemId == R.id.action_remove_ads) {
            Analytics.newEvent("remove ads menu item").log();
            onEventMainThread(new Monetize.Event.RequestRemoveAds());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        // Called when requested PRODUCT ID was successfully purchased
        Analytics.newEvent("in-app purchase").put("product_id", productId).log();
        if (productId.equals(Monetize.decrypt(Monetize.ENCRYPTED_REMOVE_ADS_PRODUCT_ID))) {
            Monetize.removeAds();
            EventBus.getDefault().post(new Monetize.Event.OnAdsRemovedEvent());
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        // Called when requested PRODUCT ID was successfully purchased
        Jot.d("Restored purchases");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        // Called when some error occurred. See Constants class for more details
        Analytics.newEvent("billing error").put("error_code", errorCode).log();
        Crashlytics.logException(error);
    }

    @Override
    public void onBillingInitialized() {
        // Called when BillingProcessor was initialized and it's ready to purchase
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Monetize.Event.RequestInterstitialAd event) {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            Analytics.newEvent("interstitial_ad").put("id", interstitialAd.getAdUnitId()).log();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Monetize.Event.OnAdsRemovedEvent event) {
        Technique.SLIDE_OUT_DOWN.getComposer().hideOnFinished().playOn(findViewById(R.id.ad_view));
        interstitialAd = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Monetize.Event.RequestRemoveAds event) {
        bp.purchase(this, Monetize.decrypt(Monetize.ENCRYPTED_REMOVE_ADS_PRODUCT_ID));
    }

    private void loadInterstitialAd() {
        if (Monetize.isAdsRemoved()) return;
        if (interstitialAd == null) {
            interstitialAd = new InterstitialAd(this);
        }
        interstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id));
        if (App.isDebuggable()) {
            interstitialAd.loadAd(new AdRequest.Builder().addTestDevice(DeviceUtils.getDeviceId()).build());
        } else {
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }
        interstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                if (App.isDebuggable()) {
                    interstitialAd.loadAd(new AdRequest.Builder().addTestDevice(DeviceUtils.getDeviceId()).build());
                } else {
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            }
        });
    }

    private void setupBanners() {
        AdRequest.Builder builder = new AdRequest.Builder();
        if (App.isDebuggable()) {
            builder.addTestDevice(DeviceUtils.getDeviceId());
        }

        adViewTiers[currentAdViewIndex] = new AdView(this);
        adViewTiers[currentAdViewIndex].setAdSize(AdSize.SMART_BANNER);
        adViewTiers[currentAdViewIndex]
            .setAdUnitId(getResources().getStringArray(R.array.banners_id)[currentAdViewIndex]);
        adViewTiers[currentAdViewIndex].setAdListener(new AdListener() {
            @Override public void onAdFailedToLoad(int errorCode) {
                if (currentAdViewIndex != (adViewTiers.length - 1)) {
                    currentAdViewIndex++;
                    setupBanners();
                } else if (adContainer.getVisibility() == View.VISIBLE) {
                    Technique.SLIDE_OUT_DOWN.getComposer().hideOnFinished().playOn(adContainer);
                }
            }

            @Override public void onAdLoaded() {
                adContainer.setVisibility(View.VISIBLE);
                if (adContainer.getChildCount() != 0) {
                    adContainer.removeAllViews();
                }
                adContainer.addView(adViewTiers[currentAdViewIndex]);
                Analytics.newEvent("on_ad_loaded")
                    .put("id", adViewTiers[currentAdViewIndex].getAdUnitId()).log();
            }
        });

        adViewTiers[currentAdViewIndex].loadAd(builder.build());
    }

    private void showTabInterstitials() {
        for (InterstitialAd interstitialAd : interstitialsTabAd) {
            if (interstitialIsReady(interstitialAd)) {
                interstitialAd.show();
                return;
            }
        }
    }

    private void showSettingsInterstitials() {
        for (InterstitialAd interstitialAd : interstitialsSettingsAd) {
            if (interstitialIsReady(interstitialAd)) {
                interstitialAd.show();
                return;
            }
        }
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void setupTabInterstitialsAd() {
        for (int i = 0; i < interstitialsTabAd.length; i++) {
            if (!interstitialIsReady(interstitialsTabAd[i])) {
                newTabInterstitialAd(i, getResources().getStringArray(R.array.tabs_interstitials_id)[i]);
            }
        }
    }

    private void setupSettingsInterstitialsAd() {
        for (int i = 0; i < interstitialsSettingsAd.length; i++) {
            if (!interstitialIsReady(interstitialsSettingsAd[i])) {
                newSettingsInterstitialAd(i, getResources()
                    .getStringArray(R.array.settings_interstitials_id)[i]);
            }
        }
    }

    private void newTabInterstitialAd(final int position, String placementId) {
        interstitialsTabAd[position] = new InterstitialAd(this);
        interstitialsTabAd[position].setAdListener(new AdListener() {
            @Override public void onAdClosed() {
                super.onAdClosed();
                interstitialsTabAd[position] = null;
                setupTabInterstitialsAd();
            }
        });
        interstitialsTabAd[position].setAdUnitId(placementId);
        interstitialsTabAd[position].loadAd(getAdRequest());
    }

    private void newSettingsInterstitialAd(final int position, String placementId) {
        interstitialsSettingsAd[position] = new InterstitialAd(this);
        interstitialsSettingsAd[position].setAdListener(new AdListener() {
            @Override public void onAdClosed() {
                super.onAdClosed();
                interstitialsSettingsAd[position] = null;
                setupSettingsInterstitialsAd();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        interstitialsSettingsAd[position].setAdUnitId(placementId);
        interstitialsSettingsAd[position].loadAd(getAdRequest());
    }

    private boolean interstitialIsReady(InterstitialAd interstitialAd) {
        return interstitialAd != null && interstitialAd.isLoaded();
    }

    private AdRequest getAdRequest() {
        AdRequest adRequest;
        if (App.isDebuggable()) {
            adRequest = new AdRequest.Builder().addTestDevice(DeviceUtils.getDeviceId()).build();
        } else {
            adRequest = new AdRequest.Builder().build();
        }
        return adRequest;
    }

}
