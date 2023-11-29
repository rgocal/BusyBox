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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jrummyapps.android.directorypicker.DirectoryPickerDialog;
import com.jrummyapps.android.exceptions.NotImplementedException;
import com.jrummyapps.android.files.LocalFile;
import com.jrummyapps.android.files.external.ExternalStorageHelper;
import com.jrummyapps.android.permiso.Permiso;
import com.jrummyapps.android.radiant.activity.RadiantAppCompatActivity;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.fragments.AppletsFragment;
import com.jrummyapps.busybox.fragments.InstallerFragment;
import com.jrummyapps.busybox.fragments.ScriptsFragment;

import static com.jrummyapps.android.app.App.getContext;
import static com.jrummyapps.busybox.utils.FragmentUtils.getCurrentFragment;

public class MainActivity extends RadiantAppCompatActivity implements
    DirectoryPickerDialog.OnDirectorySelectedListener,
    DirectoryPickerDialog.OnDirectoryPickerCancelledListener {

  protected static final String EXTRA_URI_KEY = "extra_web_link";

  public ViewPager viewPager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_busybox_main);
    TabLayout tabLayout = getViewById(R.id.tabs);
    viewPager = getViewById(R.id.container);
    Toolbar toolbar = getViewById(R.id.toolbar);
    String[] titles = {getString(R.string.applets), getString(R.string.installer), getString(R.string.scripts)};
    SectionsAdapter pagerAdapter = new SectionsAdapter(getSupportFragmentManager(), titles);
    setSupportActionBar(toolbar);
    viewPager.setOffscreenPageLimit(2);
    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);
    viewPager.setCurrentItem(1);

    if (getIntent() != null) {
      openLink(getIntent());
    }
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    if (intent != null) {
      openLink(intent);
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.main_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.action_promo).setIcon(R.drawable.ic_shop_two_white_24dp);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    switch (itemId) {
      case R.id.action_settings: {
        startActivity(new Intent(this, SettingsActivity.class));
        break;
      }
      case R.id.action_promo: {
        startActivity(new Intent(this, CrossPromoActivity.class));
        break;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (ExternalStorageHelper.getInstance().onActivityResult(requestCode, resultCode, data)) {
      return;
    }
    if (requestCode == ScriptsFragment.REQUEST_CREATE_SCRIPT) {
      Fragment fragment = getCurrentFragment(getSupportFragmentManager(), viewPager);
      if (fragment instanceof ScriptsFragment) {
        // android.app.support.v4.Fragment doesn't have
        // startActivityForResult(Intent intent, int requestCode, Bundle options)
        // so wee need to pass the result on
        fragment.onActivityResult(requestCode, resultCode, data);
        return;
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (Permiso.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults)) {
      return;
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override public void onDirectorySelected(LocalFile directory) {
    Fragment fragment = getCurrentFragment(getSupportFragmentManager(), viewPager);
    if (fragment instanceof DirectoryPickerDialog.OnDirectorySelectedListener) {
      ((DirectoryPickerDialog.OnDirectorySelectedListener) fragment).onDirectorySelected(directory);
    }
  }

  @Override public void onDirectoryPickerCancelledListener() {
    Fragment fragment = getCurrentFragment(getSupportFragmentManager(), viewPager);
    if (fragment instanceof DirectoryPickerDialog.OnDirectoryPickerCancelledListener) {
      ((DirectoryPickerDialog.OnDirectoryPickerCancelledListener) fragment).onDirectoryPickerCancelledListener();
    }
  }

  @Override public int getThemeResId() {
    return getRadiant().getNoActionBarTheme();
  }

  public static class SectionsAdapter extends FragmentPagerAdapter {

    private final String[] titles;

    public SectionsAdapter(FragmentManager fm, String[] titles) {
      super(fm);
      this.titles = titles;
    }

    @Override public Fragment getItem(int position) {
      final String title = getPageTitle(position).toString();
      if (title.equals(getContext().getString(R.string.installer))) {
        return new InstallerFragment();
      } else if (title.equals(getContext().getString(R.string.applets))) {
        return new AppletsFragment();
      } else if (title.equals(getContext().getString(R.string.scripts))) {
        return new ScriptsFragment();
      }
      throw new NotImplementedException();
    }

    @Override public int getCount() {
      return titles.length;
    }

    @Override public CharSequence getPageTitle(int position) {
      return titles[position];
    }

  }

  private void openLink(Intent intent) {
    String link = intent.getStringExtra(EXTRA_URI_KEY);
    if (link != null) {
      intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(link));
      if (intent.resolveActivity(getPackageManager()) != null) {
        startActivity(intent);
      }
    }
  }

}
