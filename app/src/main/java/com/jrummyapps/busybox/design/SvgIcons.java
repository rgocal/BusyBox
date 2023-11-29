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

import android.graphics.Color;

import com.jaredrummler.android.widget.AnimatedSvgView;

public enum SvgIcons {
  LOGO(
      new String[]{
          "M481,452.9c0,15.5-12.6,28.1-28.1,28.1H59.1C43.6,481,31,468.4,31,452.9V59.1C31,43.6,43.6,31,59.1,31h393.8c15.5,0,28.1,12.6,28.1,28.1V452.9z",
          "M256.4,397.8l-0.1-0.1l-119.9-71.4v-115l0,0l120.4,64.2l0,0l0.1,0l0,0V398l0,0l-0.1-0.1L256.4,397.8z",
          "M375.7,211.3l-118.8,64.3V398l118.8-71.9L375.7,211.3L375.7,211.3z",
          "M375.7,211.3v84.2L312.3,328l-55.5-52.4v0L375.7,211.3L375.7,211.3z",
          "M433.3,244.4L375.7,274l-64,32.9l-54.8-31.3v0l118.8-64.3l0,0L433.3,244.4z",
          "M255.2,147l120.5,64.3l0,0l54.5-31.3l-54.5-30.7l-63-35.4L255.2,147L255.2,147L255.2,147L255.2,147z",
          "M78.6,178.3l57.6-29.6l64-32.9l54.8,31.3v0l-118.8,64.3l0,0L78.6,178.3z",
          "M255.1,147l1.8,128.7l-0.1,0l-120.5-64.3L255.1,147z",
          "M256.9,275.7L256.9,275.7L255.1,147l0,0l0.1,0l120.5,64.3L256.9,275.7z",
          "M256.9,275.7V350l-91-48.6l9.6-6.1l23.8,13.4L256.9,275.7L256.9,275.7z",
          "M256.8,275.6L256.8,275.6l-0.1,122.3l0.2,0.1L256.8,275.6L256.8,275.6z",
          "M256.8,275.6l-120.5-64.3l0,0l-54.5,31.3l54.5,30.7l63,35.4L256.8,275.6L256.8,275.6L256.8,275.6L256.8,275.6z"
      },
      new int[]{
          0xFF41A4C4,
          0xFFD95545,
          0xFFC54C3F,
          0xFFAA4438,
          0xFFF4F3EE,
          0xFFF4F3EE,
          0xFFF4F3EE,
          0xFFD2D1CC,
          0xFFDCDAD6,
          0xFFC54C3F,
          0xFFD95545,
          0xFFF4F3EE
      },
      512, 512
  ),
  TWITTER(
      new String[]{
          "M22.46,6C21.69,6.35 20.86,6.58 20,6.69C20.88,6.16 21.56,5.32 21.88,4.31C21.05,4.81 20.13,5.16 19.16,5.36C18.37,4.5 17.26,4 16,4C13.65,4 11.73,5.92 11.73,8.29C11.73,8.63 11.77,8.96 11.84,9.27C8.28,9.09 5.11,7.38 3,4.79C2.63,5.42 2.42,6.16 2.42,6.94C2.42,8.43 3.17,9.75 4.33,10.5C3.62,10.5 2.96,10.3 2.38,10C2.38,10 2.38,10 2.38,10.03C2.38,12.11 3.86,13.85 5.82,14.24C5.46,14.34 5.08,14.39 4.69,14.39C4.42,14.39 4.15,14.36 3.89,14.31C4.43,16 6,17.26 7.89,17.29C6.43,18.45 4.58,19.13 2.56,19.13C2.22,19.13 1.88,19.11 1.54,19.07C3.44,20.29 5.7,21 8.12,21C16,21 20.33,14.46 20.33,8.79C20.33,8.6 20.33,8.42 20.32,8.23C21.16,7.63 21.88,6.87 22.46,6Z"
      },
      new int[]{
          Color.WHITE
      },
      24, 24
  ),
  GOOGLE_PLUS(
      new String[]{
          "M21,11.2h-1.6V9.5h-1.6v1.6h-1.6v1.6h1.6v1.6h1.6v-1.6H21 M8.7,11.2v2H12c-0.2,0.8-1,2.5-3.3,2.5c-2,0-3.5-1.6-3.5-3.6s1.6-3.6,3.5-3.6c1.1,0,1.9,0.5,2.3,0.9l1.6-1.5c-1-1-2.3-1.6-3.8-1.6C5.5,6.3,3,8.8,3,12s2.5,5.7,5.7,5.7c3.3,0,5.5-2.3,5.5-5.6c0-0.4,0-0.7-0.1-1H8.7z",
      },
      new int[]{
          Color.WHITE
      },
      24, 24
  ),
  LINKEDIN(
      new String[]{
          "M21,21h-4v-6.8c0-1.1-1.2-1.9-2.3-1.9S13,13.2,13,14.3V21H9V9h4v2c0.7-1.1,2.4-1.8,3.5-1.8c2.5,0,4.5,2,4.5,4.5V21 M7,21H3V9h4V21 M5,3c1.1,0,2,0.9,2,2S6.1,7,5,7S3,6.1,3,5S3.9,3,5,3z",
      },
      new int[]{
          Color.WHITE
      },
      24, 24
  ),
  GITHUB( // https://github.com/logos
      new String[]{
          "M21,12.2c0,4-2.6,7.3-6.1,8.5c-0.5,0.1-0.6-0.2-0.6-0.4c0-0.3,0-1.3,0-2.5c0-0.8-0.3-1.4-0.6-1.7c2-0.2,4.1-1,4.1-4.4c0-1-0.3-1.8-0.9-2.4c0.1-0.2,0.4-1.1-0.1-2.4c0,0-0.8-0.2-2.5,0.9c-0.7-0.2-1.5-0.3-2.3-0.3c-0.8,0-1.5,0.1-2.3,0.3C8,6.7,7.3,7,7.3,7C6.8,8.2,7.1,9.1,7.2,9.3c-0.6,0.6-0.9,1.4-0.9,2.4c0,3.4,2.1,4.2,4.1,4.4c-0.3,0.2-0.5,0.6-0.6,1.2C9.3,17.6,8,18,7.2,16.7c0,0-0.5-0.9-1.4-0.9c0,0-0.9,0-0.1,0.5c0,0,0.6,0.3,1,1.3c0,0,0.5,1.8,3,1.2c0,0.8,0,1.3,0,1.5c0,0.2-0.2,0.5-0.6,0.4C5.6,19.6,3,16.2,3,12.2c0-5,4-9,9-9C17,3.2,21,7.3,21,12.2z"
      },
      new int[]{
          Color.WHITE
      },
      24, 24
  );

  public final String[] glyphs;
  public final int[] colors;
  public final int width;
  public final int height;

  SvgIcons(String[] glyphs, int[] colors, int width, int height) {
    this.glyphs = glyphs;
    this.colors = colors;
    this.width = width;
    this.height = height;
  }

  public AnimatedSvgView into(AnimatedSvgView view) {
    view.setGlyphStrings(glyphs);
    view.setFillColors(colors);
    view.setTraceColors(colors);
    view.setTraceResidueColor(0x32000000);
    view.setViewportSize(width, height);
    view.rebuildGlyphData();
    return view;
  }

}
