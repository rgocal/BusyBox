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
import android.support.annotation.Nullable;
import com.jrummyapps.android.roottools.box.BusyBox;
import com.jrummyapps.android.roottools.commands.LsCommand;
import com.jrummyapps.android.roottools.commands.LsEntry;
import com.jrummyapps.android.storage.Storage;
import java.io.File;
import org.greenrobot.eventbus.EventBus;

public class BusyBoxFinder extends AsyncTask<Void, Void, BusyBox> {

  @Override protected BusyBox doInBackground(Void... params) {
    for (String path : Storage.PATH) {
      BusyBox busybox = BusyBox.newInstance(new File(path, "busybox").getAbsolutePath());
      if (path.equals("/sbin")) {
        // /sbin is not readable. Get file info with root
        LsEntry entry = LsCommand.getEntry("/sbin/busybox");
        if (entry != null) {
          busybox.setEntry(entry);
          if (!entry.isSymlink) {
            return busybox;
          }
        }
        continue;
      }
      if (busybox.exists() && !busybox.isSymlink()) {
        return busybox;
      }
    }
    return null;
  }

  @Override protected void onPostExecute(BusyBox busybox) {
    EventBus.getDefault().post(new BusyboxFoundEvent(busybox));
  }

  public static final class BusyboxFoundEvent {

    @Nullable public final BusyBox busybox;

    BusyboxFoundEvent(BusyBox busybox) {
      this.busybox = busybox;
    }

  }

}
