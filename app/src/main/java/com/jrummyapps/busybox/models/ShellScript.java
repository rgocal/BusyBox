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

package com.jrummyapps.busybox.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.jrummyapps.android.database.TableRow;

public class ShellScript implements Parcelable, TableRow {

  public String name;
  public String path;
  public String info;
  public long lastRunTime;
  public boolean runAtBoot;
  public boolean runOnNetworkChange;

  public ShellScript(String name, String path) {
    this.name = name;
    this.path = path;
  }

  public ShellScript setName(String name) {
    this.name = name;
    return this;
  }

  public ShellScript setPath(String path) {
    this.path = path;
    return this;
  }

  public ShellScript setInfo(String info) {
    this.info = info;
    return this;
  }

  public ShellScript setLastRunTime(long lastRunTime) {
    this.lastRunTime = lastRunTime;
    return this;
  }

  public ShellScript setRunAtBoot(boolean runAtBoot) {
    this.runAtBoot = runAtBoot;
    return this;
  }

  public ShellScript setRunOnNetworkChange(boolean runOnNetworkChange) {
    this.runOnNetworkChange = runOnNetworkChange;
    return this;
  }

  @Override public String toString() {
    return name;
  }

  @Override public long getId() {
    return 0;
  }

  @SuppressWarnings("unchecked")
  @Override public ShellScript setId(long id) {
    return this;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(path);
    dest.writeString(info);
    dest.writeLong(lastRunTime);
    dest.writeByte(runAtBoot ? (byte) 1 : (byte) 0);
    dest.writeByte(runOnNetworkChange ? (byte) 1 : (byte) 0);
  }

  protected ShellScript(Parcel in) {
    name = in.readString();
    path = in.readString();
    info = in.readString();
    lastRunTime = in.readLong();
    runAtBoot = in.readByte() != 0;
    runOnNetworkChange = in.readByte() != 0;
  }

  public static final Parcelable.Creator<ShellScript> CREATOR = new Parcelable.Creator<ShellScript>() {

    @Override public ShellScript createFromParcel(Parcel source) {
      return new ShellScript(source);
    }

    @Override public ShellScript[] newArray(int size) {
      return new ShellScript[size];
    }

  };

}
