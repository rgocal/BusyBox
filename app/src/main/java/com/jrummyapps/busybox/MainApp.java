/*
 * Copyright (C) 2017 JRummy Apps Inc.
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

package com.jrummyapps.busybox;

import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jrummyapps.android.BaseApp;
import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.analytics.AnswersLogger;
import com.jrummyapps.android.util.Jot;

import io.fabric.sdk.android.Fabric;

public class MainApp extends BaseApp {

  @Override public void onCreate() {
    super.onCreate();
    MultiDex.install(this);

    // Logging
    if (BuildConfig.DEBUG) {
      Jot.add(new Jot.DebugLogger());
    } else {
      Jot.add(new CrashlyticsLogger());
    }

    // Fabric
    Fabric.with(this, new Crashlytics(), new Answers());
    Analytics.add(AnswersLogger.getInstance());
    // Crashlytics
    Crashlytics.setString("GIT_SHA", BuildConfig.GIT_SHA);
    Crashlytics.setString("BUILD_TIME", BuildConfig.BUILD_TIME);

    FirebaseMessaging.getInstance().subscribeToTopic("main-" + BuildConfig.FLAVOR);
  }

  static final class CrashlyticsLogger extends Jot.Logger {

    private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
    private static final String CRASHLYTICS_KEY_TAG = "tag";
    private static final String CRASHLYTICS_KEY_MESSAGE = "message";

    @Override protected void log(int priority, String tag, String message, Throwable t) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
        return;
      }

      Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
      Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
      Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

      if (t == null) {
        Crashlytics.logException(new Exception(message));
      } else {
        Crashlytics.logException(t);
      }
    }
  }

}
