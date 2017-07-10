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

package com.jrummy.busybox.installer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
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
import com.jrummyapps.busybox.utils.Monetize;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends com.jrummyapps.busybox.activities.MainActivity
    implements BillingProcessor.IBillingHandler {

  InterstitialAd interstitialAd;
  BillingProcessor bp;
  AdView adView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EventBus.getDefault().register(this);

    MobileAds.initialize(getApplicationContext(), "ca-app-pub-1915343032510958/9622649453");

    adView = (AdView) findViewById(R.id.ad_view);
    bp = new BillingProcessor(this, Monetize.decrypt(Monetize.ENCRYPTED_LICENSE_KEY), this);

    if (Prefs.getInstance().get("loaded_purchases_from_google", true)) {
      Prefs.getInstance().save("loaded_purchases_from_google", false);
      bp.loadOwnedPurchasesFromGoogle();
    }

    if (!Monetize.isAdsRemoved()) {
      AdRequest adRequest;
      if (App.isDebuggable()) {
        adRequest = new AdRequest.Builder().addTestDevice(DeviceUtils.getDeviceId()).build();
      } else {
        adRequest = new AdRequest.Builder().build();
      }
      adView.setAdListener(new AdListener() {

        @Override public void onAdFailedToLoad(int errorCode) {
          if (adView.getVisibility() == View.VISIBLE) {
            Technique.SLIDE_OUT_DOWN.getComposer().hideOnFinished().playOn(adView);
          }
        }

        @Override public void onAdLoaded() {
          adView.setVisibility(View.VISIBLE);
          Analytics.newEvent("on_ad_loaded").put("id", adView.getAdUnitId()).log();
        }
      });
      adView.loadAd(adRequest);
      loadInterstitialAd();
    }
  }

  @Override protected void onPause() {
    if (adView != null) {
      adView.pause();
    }
    super.onPause();
  }

  @Override protected void onResume() {
    super.onResume();
    if (adView != null) {
      adView.resume();
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
    if (bp != null) {
      bp.release();
    }
    if (adView != null) {
      adView.destroy();
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (bp.handleActivityResult(requestCode, resultCode, data)) {
      return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.action_remove_ads).setVisible(!Monetize.isAdsRemoved());
    menu.findItem(R.id.action_unlock_premium).setVisible(!Monetize.isProVersion());
    return super.onPrepareOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.action_remove_ads) {
      Analytics.newEvent("remove ads menu item").log();
      onEventMainThread(new Monetize.Event.RequestRemoveAds());
      return true;
    } else if (itemId == R.id.action_unlock_premium) {
      Analytics.newEvent("pre version menu item").log();
      onEventMainThread(new Monetize.Event.RequestPremiumEvent());
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onProductPurchased(String productId, TransactionDetails details) {
    // Called when requested PRODUCT ID was successfully purchased
    Analytics.newEvent("in-app purchase").put("product_id", productId).log();
    if (productId.equals(Monetize.decrypt(Monetize.ENCRYPTED_PRO_VERSION_PRODUCT_ID))) {
      Monetize.removeAds();
      Monetize.unlockProVersion();
      EventBus.getDefault().post(new Monetize.Event.OnAdsRemovedEvent());
      EventBus.getDefault().post(new Monetize.Event.OnPurchasedPremiumEvent());
    } else if (productId.equals(Monetize.decrypt(Monetize.ENCRYPTED_REMOVE_ADS_PRODUCT_ID))) {
      Monetize.removeAds();
      EventBus.getDefault().post(new Monetize.Event.OnAdsRemovedEvent());
    }
  }

  @Override public void onPurchaseHistoryRestored() {
    // Called when requested PRODUCT ID was successfully purchased
    Jot.d("Restored purchases");
  }

  @Override public void onBillingError(int errorCode, Throwable error) {
    // Called when some error occurred. See Constants class for more details
    Analytics.newEvent("billing error").put("error_code", errorCode).log();
    Crashlytics.logException(error);
  }

  @Override public void onBillingInitialized() {
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
  public void onEventMainThread(Monetize.Event.OnPurchasedPremiumEvent event) {

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventMainThread(Monetize.Event.RequestPremiumEvent event) {
    bp.purchase(this, Monetize.decrypt(Monetize.ENCRYPTED_PRO_VERSION_PRODUCT_ID));
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

      @Override public void onAdClosed() {
        if (App.isDebuggable()) {
          interstitialAd.loadAd(new AdRequest.Builder().addTestDevice(DeviceUtils.getDeviceId()).build());
        } else {
          interstitialAd.loadAd(new AdRequest.Builder().build());
        }
      }
    });
  }

}
