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

package com.jrummyapps.busybox.utils;

import android.support.v4.view.ViewPager;
import java.util.Locale;

/**
 * Contains utility methods for getting a Fragment in a {@link ViewPager}.
 */
public class FragmentUtils {

  private static final String FRAGMENT_ADAPTER_ID = "android:switcher:%d:%d";

  /**
   * Get the current fragment in a {@link ViewPager}s adapter.
   *
   * @param manager
   *     the support fragment manager.
   *     {@link android.support.v4.app.FragmentActivity#getSupportFragmentManager()}
   * @param pager
   *     the {@link ViewPager} holding the {@link android.support.v4.app.Fragment}
   * @return the fragment at this position in the {@link ViewPager}'s adapter
   */
  public static <fragment extends android.support.v4.app.Fragment> fragment getCurrentFragment(
      android.support.v4.app.FragmentManager manager, ViewPager pager) {
    return findFragmentByPosition(manager, pager, pager.getCurrentItem());
  }

  /**
   * Get a fragment in a {@link ViewPager}s adapter.
   *
   * @param manager
   *     the support fragment manager.
   *     {@link android.support.v4.app.FragmentActivity#getSupportFragmentManager()}
   * @param pager
   *     the {@link ViewPager} holding the {@link android.support.v4.app.Fragment}
   * @param position
   *     the position in the {@link ViewPager} e.g. {@link ViewPager#getCurrentItem()}
   * @param <fragment>
   *     Destination cast class type.
   * @return the fragment at this position in the {@link ViewPager}'s adapter
   */
  @SuppressWarnings("unchecked")
  public static <fragment extends android.support.v4.app.Fragment> fragment
  findFragmentByPosition(android.support.v4.app.FragmentManager manager, ViewPager pager,
                         int position) {
    return (fragment) manager.findFragmentByTag(String.format(Locale.US, FRAGMENT_ADAPTER_ID, pager.getId(), position));
  }

  /**
   * Get the current fragment in a {@link ViewPager}s adapter.
   *
   * @param manager
   *     the support fragment manager {@link android.app.Activity#getFragmentManager()}
   * @param pager
   *     the {@link ViewPager} holding the {@link android.app.Fragment}
   * @param position
   *     the position in the {@link ViewPager} e.g. {@link ViewPager#getCurrentItem()}
   * @param <fragment>
   *     Destination cast class type.
   * @return the fragment at this position in the {@link ViewPager}'s adapter
   */
  public static <fragment extends android.app.Fragment> fragment getCurrentFragment(
      android.app.FragmentManager manager, ViewPager pager, int position) {
    return findFragmentByPosition(manager, pager, pager.getCurrentItem());
  }

  /**
   * Get a fragment in a {@link ViewPager}s adapter.
   *
   * @param manager
   *     the support fragment manager {@link android.app.Activity#getFragmentManager()}
   * @param pager
   *     the {@link ViewPager} holding the {@link android.app.Fragment}
   * @param position
   *     the position in the {@link ViewPager} e.g. {@link ViewPager#getCurrentItem()}
   * @param <fragment>
   *     Destination cast class type.
   * @return the fragment at this position in the {@link ViewPager}'s adapter
   */
  @SuppressWarnings("unchecked")
  public static <fragment extends android.app.Fragment> fragment findFragmentByPosition(
      android.app.FragmentManager manager, ViewPager pager, int position) {
    return (fragment) manager.findFragmentByTag(String.format(Locale.US, FRAGMENT_ADAPTER_ID, pager.getId(), position));
  }

}
