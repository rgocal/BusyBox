/*
 * Copyright 2015 Google Inc.
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

package com.jrummyapps.busybox.transitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.jrummyapps.android.theme.ColorScheme;
import com.jrummyapps.android.util.ResUtils;
import com.jrummyapps.busybox.drawable.MorphDrawable;

import static com.jrummyapps.busybox.utils.AnimUtils.getFastOutLinearInInterpolator;
import static com.jrummyapps.busybox.utils.AnimUtils.getFastOutSlowInInterpolator;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MorphDialogToFab extends ChangeBounds {

  // taken from https://github.com/nickbutcher/plaid

  private static final String PROPERTY_COLOR = "com.jrummyapps:rectMorph:color";
  private static final String PROPERTY_CORNER_RADIUS = "com.jrummyapps:rectMorph:cornerRadius";
  private static final String[] TRANSITION_PROPERTIES = {PROPERTY_COLOR, PROPERTY_CORNER_RADIUS};

  @ColorInt private int endColor = Color.TRANSPARENT;
  private int endCornerRadius = -1;

  public MorphDialogToFab(@ColorInt int endColor) {
    super();
    setEndColor(endColor);
  }

  public MorphDialogToFab(@ColorInt int endColor, int endCornerRadius) {
    super();
    setEndColor(endColor);
    setEndCornerRadius(endCornerRadius);
  }

  public MorphDialogToFab(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setEndColor(@ColorInt int endColor) {
    this.endColor = endColor;
  }

  public void setEndCornerRadius(int endCornerRadius) {
    this.endCornerRadius = endCornerRadius;
  }

  @Override public String[] getTransitionProperties() {
    return TRANSITION_PROPERTIES;
  }

  @Override public void captureStartValues(TransitionValues transitionValues) {
    super.captureStartValues(transitionValues);
    final View view = transitionValues.view;
    if (view.getWidth() <= 0 || view.getHeight() <= 0) {
      return;
    }
    transitionValues.values.put(PROPERTY_COLOR, ColorScheme.getBackground(view.getContext()));
    transitionValues.values.put(PROPERTY_CORNER_RADIUS, ResUtils.dpToPx(2));
  }

  @Override public void captureEndValues(TransitionValues transitionValues) {
    super.captureEndValues(transitionValues);
    final View view = transitionValues.view;
    if (view.getWidth() <= 0 || view.getHeight() <= 0) {
      return;
    }
    transitionValues.values.put(PROPERTY_COLOR, endColor);
    transitionValues.values.put(PROPERTY_CORNER_RADIUS,
        endCornerRadius >= 0 ? endCornerRadius : view.getHeight() / 2);
  }

  @Override
  public Animator createAnimator(final ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
    Animator changeBounds = super.createAnimator(sceneRoot, startValues, endValues);
    if (startValues == null || endValues == null || changeBounds == null) {
      return null;
    }

    Integer startColor = (Integer) startValues.values.get(PROPERTY_COLOR);
    Integer startCornerRadius = (Integer) startValues.values.get(PROPERTY_CORNER_RADIUS);
    Integer endColor = (Integer) endValues.values.get(PROPERTY_COLOR);
    Integer endCornerRadius = (Integer) endValues.values.get(PROPERTY_CORNER_RADIUS);

    if (startColor == null || startCornerRadius == null || endColor == null ||
        endCornerRadius == null) {
      return null;
    }

    MorphDrawable background = new MorphDrawable(startColor, startCornerRadius);
    endValues.view.setBackground(background);

    Animator color = ObjectAnimator.ofArgb(background, MorphDrawable.COLOR, endColor);
    Animator corners = ObjectAnimator.ofFloat(background, MorphDrawable.CORNER_RADIUS, endCornerRadius);

    // hide child views (offset down & fade out)
    if (endValues.view instanceof ViewGroup) {
      ViewGroup vg = (ViewGroup) endValues.view;
      for (int i = 0; i < vg.getChildCount(); i++) {
        View v = vg.getChildAt(i);
        v.animate()
            .alpha(0f)
            .translationY(v.getHeight() / 3)
            .setStartDelay(0L)
            .setDuration(50L)
            .setInterpolator(getFastOutLinearInInterpolator(vg.getContext()))
            .start();
      }
    }

    AnimatorSet transition = new AnimatorSet();
    transition.playTogether(changeBounds, corners, color);
    transition.setDuration(300);
    transition.setInterpolator(getFastOutSlowInInterpolator(sceneRoot.getContext()));
    return transition;
  }

}