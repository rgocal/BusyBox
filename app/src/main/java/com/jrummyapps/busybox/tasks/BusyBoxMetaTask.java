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

package com.jrummyapps.busybox.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.jrummyapps.android.app.App;
import com.jrummyapps.android.fileproperties.models.FileMeta;
import com.jrummyapps.android.files.FilePermission;
import com.jrummyapps.android.roottools.box.BusyBox;
import com.jrummyapps.android.util.DateFormats;
import com.jrummyapps.busybox.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.greenrobot.eventbus.EventBus;

public class BusyBoxMetaTask extends AsyncTask<BusyBox, Void, ArrayList<FileMeta>> {

  @Override protected ArrayList<FileMeta> doInBackground(BusyBox... params) {
    BusyBox file = params[0];
    if (file == null) return null;
    if (!file.exists() && file.path.equals("/sbin/busybox")) file.getEntry();
    if (!file.exists()) return null;
    ArrayList<FileMeta> properties = new ArrayList<>();
    properties.add(new FileMeta("path", R.string.path, file.path));
    String version = file.getVersion();
    if (!TextUtils.isEmpty(version)) {
      properties.add(new FileMeta("version", R.string.version, version));
    }
    FilePermission permissions = file.getFilePermission();
    if (permissions != null) {
      String value = permissions.mode + " (" + permissions.permissions + ")";
      properties.add(new FileMeta("permissions", R.string.permissions, value));
    }
    properties.add(new FileMeta("size", R.string.size, Formatter.formatFileSize(App.getContext(), file.length())));
    SimpleDateFormat sdf = DateFormats.getDateTimeFormatter();
    properties.add(new FileMeta("last_modified", R.string.last_modified, sdf.format(file.lastModified())));
    return properties;
  }

  @Override protected void onPostExecute(ArrayList<FileMeta> properties) {
    EventBus.getDefault().post(new BusyBoxPropertiesEvent(properties));
  }

  public static final class BusyBoxPropertiesEvent {

    public final ArrayList<FileMeta> properties;

    BusyBoxPropertiesEvent(ArrayList<FileMeta> properties) {
      this.properties = properties;
    }
  }

}
