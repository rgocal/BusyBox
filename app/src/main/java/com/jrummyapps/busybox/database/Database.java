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

package com.jrummyapps.busybox.database;

import com.jrummyapps.android.app.App;
import com.jrummyapps.android.database.BaseDatabase;
import com.jrummyapps.android.database.DatabaseHelper;

public class Database extends BaseDatabase {

  private static volatile Database instance;

  public static Database getInstance() {
    if (instance == null) {
      synchronized (Database.class) {
        if (instance == null) {
          instance = new Database();
        }
      }
    }
    return instance;
  }

  private Database() {

  }

  @Override public String getName() {
    return "busybox.db";
  }

  @Override public int getVersion() {
    return 1;
  }

  @Override protected DatabaseHelper createHelper() {
    addTable(new ShellScriptTable());
    return new DatabaseHelper(App.getContext(), this);
  }

}
