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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.TextView;

import com.commit451.elasticdragdismisslayout.ElasticDragDismissFrameLayout;
import com.commit451.elasticdragdismisslayout.ElasticDragDismissListener;
import com.jaredrummler.android.widget.AnimatedSvgView;
import com.jrummyapps.android.animations.Technique;
import com.jrummyapps.android.radiant.activity.RadiantAppCompatActivity;
import com.jrummyapps.android.radiant.tinting.SystemBarTint;
import com.jrummyapps.android.util.HtmlBuilder;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.design.SvgIcons;

public class AboutActivity extends RadiantAppCompatActivity implements ElasticDragDismissListener {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_busybox_about);

    final ElasticDragDismissFrameLayout draggableLayout = getViewById(R.id.draggable_frame);
    final View backgroundView = getViewById(R.id.about_background);
    final AnimatedSvgView svgView = getViewById(R.id.svg);
    final TextView aboutText = getViewById(R.id.about_text);
    final TextView creditsText = getViewById(R.id.credits_text);

    backgroundView.setBackgroundColor(getRadiant().backgroundColorLight());
    SvgIcons.LOGO.into(svgView).start();

    svgView.setOnLongClickListener(new View.OnLongClickListener() {

      @Override public boolean onLongClick(View v) {
        Technique.ROTATE.playOn(v);
        return true;
      }
    });

    aboutText.setText(new HtmlBuilder().h3("BusyBox: The Swiss Army Knife of Embedded Linux")
        .p("BusyBox combines tiny versions of many common UNIX utilities into a single small executable. It provides replacements for most of the utilities you usually find in GNU fileutils, shellutils, etc. The utilities in BusyBox generally have fewer options than their full-featured GNU cousins; however, the options that are included provide the expected functionality and behave very much like their GNU counterparts. BusyBox provides a fairly complete environment for any small or embedded system.")
        .p("BusyBox has been written with size-optimization and limited resources in mind. It is also extremely modular so you can easily include or exclude commands (or features) at compile time. This makes it easy to customize your embedded systems. To create a working system, just add some device nodes in /dev, a few configuration files in /etc, and a Linux kernel.")
        .p()
        .append("BusyBox is maintained by Denys Vlasenko, and licensed under the ")
        .a("https://busybox.net/license.html", "GNU GENERAL PUBLIC LICENSE")
        .append(" version 2.")
        .close()
        .h3()
        .append("Learn more at ")
        .a("https://busybox.net", "busybox.net")
        .append(" or ")
        .a("http://busybox.jrummyapps.com", "busybox.jrummyapps.com")
        .append(".")
        .close()
        .build());
    aboutText.setLinkTextColor(getRadiant().primaryColor());
    aboutText.setMovementMethod(LinkMovementMethod.getInstance());

    creditsText.setText(new HtmlBuilder()
        .p("Developed by Jared Rummler")
        .a("https://twitter.com/jrummy16", "Twitter")
        .append(" | ")
        .a("https://plus.google.com/+JaredRummler", "Google+")
        .build());
    creditsText.setLinkTextColor(getRadiant().primaryColor());
    creditsText.setMovementMethod(LinkMovementMethod.getInstance());

    draggableLayout.addListener(this);

    SystemBarTint systemBarTint = new SystemBarTint(this);
    systemBarTint.setStatusBarColor(Color.TRANSPARENT);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
  }

  @Override public int getThemeResId() {
    return getRadiant().getNoActionBarTranslucentTheme();
  }

  @Override public void onDrag(float elasticOffset, float elasticOffsetPixels, float rawOffset, float rawOffsetPixels) {

  }

  @Override public void onDragDismissed() {
    final ElasticDragDismissFrameLayout draggableLayout = getViewById(R.id.draggable_frame);
    // if we drag dismiss downward then the default reversal of the enter
    // transition would slide content upward which looks weird. So reverse it.
    if (draggableLayout.getTranslationY() > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.return_downward));
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      finishAfterTransition();
    } else {
      finish();
    }
  }

}
