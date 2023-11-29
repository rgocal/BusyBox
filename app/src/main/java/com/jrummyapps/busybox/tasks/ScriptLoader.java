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
import com.crashlytics.android.Crashlytics;
import com.jrummyapps.android.app.App;
import com.jrummyapps.android.files.FilePermission;
import com.jrummyapps.android.prefs.Prefs;
import com.jrummyapps.android.util.Assets;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.database.Database;
import com.jrummyapps.busybox.database.ShellScriptTable;
import com.jrummyapps.busybox.models.ShellScript;
import com.jrummyapps.busybox.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScriptLoader extends AsyncTask<Void, Void, ArrayList<ShellScript>> {

  private static final String LOAD_SCRIPTS_FROM_ASSETS = "load_scripts_from_assets";

  @Override protected ArrayList<ShellScript> doInBackground(Void... params) {
    ShellScriptTable table = Database.getInstance().getTable(ShellScriptTable.NAME);
    ArrayList<ShellScript> scripts = new ArrayList<>();

    if (Prefs.getInstance().get(LOAD_SCRIPTS_FROM_ASSETS, true)) {
      String json = Utils.readRaw(R.raw.scripts);
      try {
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          String name = jsonObject.getString("name");
          String filename = jsonObject.getString("filename");
          String info = jsonObject.getString("info");
          String asset = "scripts/" + filename;
          //noinspection OctalInteger
          Assets.transferAsset(asset, FilePermission.RWXR_XR_X);
          File file = new File(App.getContext().getFilesDir(), asset);
          ShellScript script = new ShellScript(name, file.getAbsolutePath()).setInfo(info);
          table.insert(script);
        }
        Prefs.getInstance().save(LOAD_SCRIPTS_FROM_ASSETS, false);
      } catch (JSONException e) {
        Crashlytics.logException(e);
      }
    }

    scripts.addAll(table.select());

    return scripts;
  }

  @Override protected void onPostExecute(ArrayList<ShellScript> scripts) {
    EventBus.getDefault().post(new ScriptsLoadedEvent(scripts));
  }

  public static final class ScriptsLoadedEvent {

    public final ArrayList<ShellScript> scripts;

    ScriptsLoadedEvent(ArrayList<ShellScript> scripts) {
      this.scripts = scripts;
    }

  }

}
