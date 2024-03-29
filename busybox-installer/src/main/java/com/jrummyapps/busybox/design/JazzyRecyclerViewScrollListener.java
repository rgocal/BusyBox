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

package com.jrummyapps.busybox.design;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

import com.jrummyapps.android.widget.jazzylistview.JazzyEffect;
import com.jrummyapps.android.widget.jazzylistview.JazzyHelper;

public class JazzyRecyclerViewScrollListener extends OnScrollListener {

  private final JazzyHelper jazzyHelper;
  private OnScrollListener onScrollListener;

  public JazzyRecyclerViewScrollListener() {
    jazzyHelper = new JazzyHelper();
  }

  @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    int firstVisibleItem = recyclerView.getChildPosition(recyclerView.getChildAt(0));
    int visibleItemCount = recyclerView.getChildCount();
    int totalItemCount = recyclerView.getAdapter().getItemCount();

    jazzyHelper.onScrolled(recyclerView, firstVisibleItem, visibleItemCount, totalItemCount);

    notifyAdditionalOnScrolledListener(recyclerView, dx, dy);
  }

  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    switch (newState) {
      case RecyclerView.SCROLL_STATE_SETTLING: // fall through
      case RecyclerView.SCROLL_STATE_DRAGGING:
        jazzyHelper.setScrolling(true);
        break;
      case RecyclerView.SCROLL_STATE_IDLE:
        jazzyHelper.setScrolling(false);
        break;
      default:
        break;
    }
    notifyAdditionalOnScrollStateChangedListener(recyclerView, newState);
  }

  /**
   * Sets the desired transition effect.
   *
   * @param transitionEffect
   *     Numeric constant representing a bundled transition effect.
   */
  public void setTransitionEffect(int transitionEffect) {
    jazzyHelper.setTransitionEffect(transitionEffect);
  }

  /**
   * Sets the desired transition effect.
   *
   * @param transitionEffect
   *     The non-bundled transition provided by the client.
   */
  public void setTransitionEffect(JazzyEffect transitionEffect) {
    jazzyHelper.setTransitionEffect(transitionEffect);
  }

  /**
   * Sets whether new items or all items should be animated when they become visible.
   *
   * @param onlyAnimateNew
   *     True if only new items should be animated; false otherwise.
   */
  public void setShouldOnlyAnimateNewItems(boolean onlyAnimateNew) {
    jazzyHelper.setShouldOnlyAnimateNewItems(onlyAnimateNew);
  }

  /**
   * Stop animations after the list has reached a certain velocity. When the list slows down
   * it will start animating again. This gives a performance boost as well as preventing
   * the list from animating under the users finger if they suddenly stop it.
   *
   * @param itemsPerSecond,
   *     set to JazzyHelper.MAX_VELOCITY_OFF to turn off max velocity.
   *     While 13 is a good default, it is dependent on the size of your items.
   */
  public void setMaxAnimationVelocity(int itemsPerSecond) {
    jazzyHelper.setMaxAnimationVelocity(itemsPerSecond);
  }

  public void setOnScrollListener(OnScrollListener l) {
    // hijack the scroll listener setter and have this list also notify the additional listener
    onScrollListener = l;
  }

  /**
   * Notifies the OnScrollListener of an onScroll event, since JazzyListView is the primary
   * listener
   * for onScroll events.
   */
  private void notifyAdditionalOnScrolledListener(RecyclerView recyclerView, int dx, int dy) {
    if (onScrollListener != null) {
      onScrollListener.onScrolled(recyclerView, dx, dy);
    }
  }

  /**
   * Notifies the OnScrollListener of an onScrollStateChanged event, since JazzyListView is the
   * primary listener for onScrollStateChanged events.
   */
  private void notifyAdditionalOnScrollStateChangedListener(RecyclerView recyclerView, int newState) {
    if (onScrollListener != null) {
      onScrollListener.onScrollStateChanged(recyclerView, newState);
    }
  }

}