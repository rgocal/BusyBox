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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import com.jrummyapps.android.preferences.activities.MainPreferenceActivity;
import com.jrummyapps.android.preferences.fragments.AboutPreferenceFragment;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.fragments.AboutFragment;
import com.jrummyapps.busybox.fragments.SettingsFragment;
import com.jrummyapps.sweetsweetdesserts.DessertCase;

public class SettingsActivity extends MainPreferenceActivity implements AboutPreferenceFragment.EasterEggCallback {

  @Override protected Fragment getFragment(int position) {
    int stringId = getStringId(position);
    if (stringId == R.string.settings) {
      return new SettingsFragment();
    } else if (stringId == R.string.about) {
      return new AboutFragment();
    }
    return super.getFragment(position);
  }

  @Override public void onRequestEgg(Activity activity, int count) {
    startActivity(new Intent(activity, DessertCase.class));
  }

}
