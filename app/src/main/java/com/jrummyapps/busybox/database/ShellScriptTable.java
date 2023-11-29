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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jrummyapps.android.database.Table;
import com.jrummyapps.android.database.WhereStatement;
import com.jrummyapps.busybox.models.ShellScript;

public class ShellScriptTable extends Table<ShellScript> {

  public static final String NAME = "shell_scripts";

  @Override public String getName() {
    return NAME;
  }

  @Override public SQLiteDatabase getReadableDatabase() {
    return Database.getInstance().getHelper().getReadableDatabase();
  }

  @Override public SQLiteDatabase getWritableDatabase() {
    return Database.getInstance().getHelper().getWritableDatabase();
  }

  @Override public ContentValues getValues(ShellScript script) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(Columns.NAME, script.name);
    contentValues.put(Columns.PATH, script.path);
    contentValues.put(Columns.INFO, script.info);
    contentValues.put(Columns.LAST_RUN_TIME, script.lastRunTime);
    contentValues.put(Columns.RUN_AT_BOOT, script.runAtBoot);
    contentValues.put(Columns.RUN_ON_NETWORK_CHANGE, script.runOnNetworkChange);
    return contentValues;
  }

  @Override protected void onCreate(SQLiteDatabase db) {
    db.execSQL(String.format("CREATE TABLE %s (%s, %s, %s, %s, %s, %s)", getName(),
        Columns.NAME,
        Columns.PATH,
        Columns.INFO,
        Columns.LAST_RUN_TIME,
        Columns.RUN_AT_BOOT,
        Columns.RUN_ON_NETWORK_CHANGE
    ));
  }

  @Override public ShellScript onCreateObject(Cursor cursor) {
    String name = cursor.getString(cursor.getColumnIndex(Columns.NAME));
    String path = cursor.getString(cursor.getColumnIndex(Columns.PATH));
    String info = cursor.getString(cursor.getColumnIndex(Columns.INFO));
    long lastRunTime = cursor.getLong(cursor.getColumnIndex(Columns.LAST_RUN_TIME));
    boolean runAtBoot = cursor.getInt(cursor.getColumnIndex(Columns.RUN_AT_BOOT)) == 1;
    boolean runOnNetworkChange = cursor.getInt(cursor.getColumnIndex(Columns.RUN_ON_NETWORK_CHANGE)) == 1;

    ShellScript script = new ShellScript(name, path);
    script.setInfo(info);
    script.setLastRunTime(lastRunTime);
    script.setRunAtBoot(runAtBoot);
    script.setRunOnNetworkChange(runOnNetworkChange);

    return script;
  }

  @Override protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

  @Override protected WhereStatement buildWhereStatement(ShellScript script) {
    WhereStatement statement = new WhereStatement();
    statement.whereClause = Columns.PATH + "=?";
    statement.whereArgs = new String[]{
        script.path
    };
    return statement;
  }

  public static final class Columns {

    public static final String NAME = "name";
    public static final String PATH = "path";
    public static final String INFO = "info";
    public static final String LAST_RUN_TIME = "last_run_time";
    public static final String RUN_AT_BOOT = "run_at_boot";
    public static final String RUN_ON_NETWORK_CHANGE = "run_on_network_change";

  }

}
