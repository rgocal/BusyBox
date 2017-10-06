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

package com.jrummyapps.busybox.activities;

import android.content.ActivityNotFoundException;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.view.View;

import com.jaredrummler.android.widget.AnimatedSvgView;
import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.animations.Rebound;
import com.jrummyapps.android.radiant.Radiant;
import com.jrummyapps.android.radiant.activity.RadiantActivity;
import com.jrummyapps.android.transitions.FabDialogMorphSetup;
import com.jrummyapps.android.transitions.TransitionUtils;
import com.jrummyapps.android.util.Intents;
import com.jrummyapps.android.util.OrientationUtils;
import com.jrummyapps.android.util.ResUtils;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.design.SvgIcons;

public class DeveloperProfileActivity extends RadiantActivity {

   AnimatedSvgView twitterView;
   AnimatedSvgView googlePlusView;
   AnimatedSvgView githubView;
   AnimatedSvgView linkedinView;

  private final Rebound.SpringyTouchListener touchListener = new Rebound.SpringyTouchListener() {

    @Override public void onClick(View v) {
      if (v == twitterView) {
        try {
          startActivity(Intents.newTwitterIntent("https://twitter.com/jrummy16"));
          Analytics.newEvent("clicked_developer_twitter").log();
        } catch (ActivityNotFoundException ignored) {
        }
      } else if (v == googlePlusView) {
        try {
          startActivity(Intents.newGooglePlusIntent("https://plus.google.com/+JaredRummler"));
          Analytics.newEvent("clicked_developer_google_plus").log();
        } catch (ActivityNotFoundException ignored) {
        }
      } else if (v == githubView) {
        try {
          startActivity(Intents.newOpenWebBrowserIntent("https://github.com/jaredrummler"));
          Analytics.newEvent("clicked_developer_github").log();
        } catch (ActivityNotFoundException ignored) {
        }
      } else if (v == linkedinView) {
        try {
          startActivity(Intents.newOpenWebBrowserIntent("https://www.linkedin.com/in/jaredrummler"));
          Analytics.newEvent("clicked_developer_linkedin").log();
        } catch (ActivityNotFoundException ignored) {
        }
      }
    }

  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    OrientationUtils.lockOrientation(this);

    super.onCreate(savedInstanceState);

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    setContentView(R.layout.activity_developer_profile);

    twitterView = getViewById(R.id.twitter);
    googlePlusView = getViewById(R.id.google_plus);
    githubView = getViewById(R.id.github);
    linkedinView = getViewById(R.id.linkedin);

    findViewById(R.id.bottom_container).setBackgroundColor(getRadiant().accentColor());

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      FabDialogMorphSetup.setupSharedEelementTransitions(this, findViewById(R.id.container), ResUtils.dpToPx(2));
    }

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && getWindow().getSharedElementEnterTransition() != null) {
      getWindow().getSharedElementEnterTransition().addListener(new TransitionUtils.TransitionListenerAdapter() {

        @Override public void onTransitionEnd(Transition transition) {
          loadView();
        }
      });
    } else {
      loadView();
    }
  }

  @Override protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    if (getRadiant().isThemeChanged()) {
      getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
  }

  void loadView() {
    Analytics.newEvent("clicked_developer_profile").log();

    SvgIcons.TWITTER.into(twitterView).start();
    SvgIcons.GOOGLE_PLUS.into(googlePlusView).start();
    SvgIcons.GITHUB.into(githubView).start();
    SvgIcons.LINKEDIN.into(linkedinView).start();

    touchListener.setEndValue(-0.3);
    twitterView.setOnTouchListener(touchListener);
    googlePlusView.setOnTouchListener(touchListener);
    githubView.setOnTouchListener(touchListener);
    linkedinView.setOnTouchListener(touchListener);
  }

  @Override public int getThemeResId() {
    if (getRadiant().getBaseTheme() == Radiant.BaseTheme.DARK) {
      return R.style.Radiant_Dark_NoActionBar_MaterialDialog;
    }
    return R.style.Radiant_Light_NoActionBar_MaterialDialog;
  }

  @Override public void onBackPressed() {
    dismiss(null);
  }

  public void dismiss(View view) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      finishAfterTransition();
    } else {
      finish();
    }
  }

}
