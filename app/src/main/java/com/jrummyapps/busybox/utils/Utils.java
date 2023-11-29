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

package com.jrummyapps.busybox.utils;

import android.os.Build;
import android.text.TextUtils;
import com.crashlytics.android.Crashlytics;
import com.jrummyapps.android.os.ABI;
import com.jrummyapps.android.roottools.box.BusyBox;
import com.jrummyapps.android.util.IoUtils;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.models.BinaryInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.compress.utils.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import static com.jrummyapps.android.app.App.getContext;

public class Utils {

  public static List<String> getBusyBoxApplets() {
    BusyBox busyBox = BusyBox.getInstance();
    Set<String> applets = busyBox.getApplets();
    if (applets.isEmpty()) {
      String json = readRaw(R.raw.busybox_applets);
      try {
        JSONObject jsonObject = new JSONObject(json);
        for (Iterator<String> iterator = jsonObject.keys(); iterator.hasNext(); ) {
          applets.add(iterator.next());
        }
      } catch (Exception ignored) {
      }
    }
    return new ArrayList<>(applets);
  }

  /**
   * Read a file from /res/raw
   *
   * @param id
   *     The id from R.raw
   * @return The contents of the file or {@code null} if reading failed.
   */
  public static String readRaw(int id) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    InputStream inputStream = getContext().getResources().openRawResource(id);
    try {
      IOUtils.copy(inputStream, outputStream);
    } catch (IOException e) {
      return null;
    } finally {
      IoUtils.closeQuietly(outputStream);
      IoUtils.closeQuietly(inputStream);
    }
    return outputStream.toString();
  }

  /**
   * Get a list of binaries in the assets directory
   *
   * @return a list of binaries from the assets in this APK file.
   */
  public static ArrayList<BinaryInfo> getBinariesFromAssets() {
    ArrayList<BinaryInfo> binaries = new ArrayList<>();
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      InputStream input = getContext().getResources().openRawResource(R.raw.binaries);
      byte[] buffer = new byte[4096];
      int n;
      while ((n = input.read(buffer)) != -1) {
        output.write(buffer, 0, n);
      }
      input.close();
      JSONArray jsonArray = new JSONArray(output.toString());
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        String name = jsonObject.getString("name");
        String filename = jsonObject.getString("filename");
        String abi = jsonObject.getString("abi");
        String flavor = jsonObject.getString("flavor");
        String url = jsonObject.getString("url");
        String md5sum = jsonObject.getString("md5sum");
        long size = jsonObject.getLong("size");
        binaries.add(new BinaryInfo(name, filename, abi, flavor, url, md5sum, size));
      }
    } catch (Exception e) {
      Crashlytics.logException(e);
    }
    return binaries;
  }

  /**
   * Get a list of supported binaries for the given ABI.
   *
   * @param abi
   *     the {@link ABI} to filter
   * @return a list of binaries from the assets in this APK file.
   */
  public static ArrayList<BinaryInfo> getBinariesFromAssets(ABI abi) {
    ArrayList<BinaryInfo> binaries = getBinariesFromAssets();
    String flavor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH ? "pie" : "nopie";
    for (Iterator<BinaryInfo> iterator = binaries.iterator(); iterator.hasNext(); ) {
      BinaryInfo binaryInfo = iterator.next();
      if (!TextUtils.equals(binaryInfo.abi, abi.base) || !TextUtils.equals(binaryInfo.flavor, flavor)) {
        iterator.remove();
      }
    }
    return binaries;
  }

}
