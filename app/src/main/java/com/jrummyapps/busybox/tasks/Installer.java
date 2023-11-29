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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import com.crashlytics.android.Crashlytics;
import com.jrummyapps.android.app.App;
import com.jrummyapps.android.files.FileOperation;
import com.jrummyapps.android.files.LocalFile;
import com.jrummyapps.android.prefs.Prefs;
import com.jrummyapps.android.roottools.RootTools;
import com.jrummyapps.android.roottools.box.BusyBox;
import com.jrummyapps.android.roottools.commands.LsCommand;
import com.jrummyapps.android.roottools.commands.RebootCommand;
import com.jrummyapps.android.roottools.commands.ShellCommand;
import com.jrummyapps.android.shell.Shell;
import com.jrummyapps.android.storage.MountPoint;
import com.jrummyapps.android.storage.Storage;
import com.jrummyapps.android.util.Assets;
import com.jrummyapps.android.util.FileUtils;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.utils.BusyBoxZipHelper;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.greenrobot.eventbus.EventBus;
import static com.jrummyapps.android.files.FilePermission.S_IRGRP;
import static com.jrummyapps.android.files.FilePermission.S_IROTH;
import static com.jrummyapps.android.files.FilePermission.S_IRUSR;
import static com.jrummyapps.android.files.FilePermission.S_IWUSR;
import static com.jrummyapps.android.files.FilePermission.S_IXGRP;
import static com.jrummyapps.android.files.FilePermission.S_IXOTH;
import static com.jrummyapps.android.files.FilePermission.S_IXUSR;

public class Installer implements Runnable {

  private static final int MODE_EXECUTABLE = S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;

  public static final String ERROR_NOT_ROOTED = "Root is required to install busybox";

  public static Builder newBusyboxInstaller() {
    return new Builder();
  }

  public final LocalFile binary;
  public final String asset;
  public final String path;
  public final String filename;
  public final boolean symlink;
  public final boolean overwrite;
  public final boolean recovery;

  private Installer(Builder builder) {
    binary = builder.binary;
    asset = builder.asset;
    path = builder.path;
    filename = builder.filename;
    symlink = builder.symlink;
    overwrite = builder.overwrite;
    recovery = builder.recovery;
  }

  @Override public void run() {
    EventBus.getDefault().post(new StartEvent(this));

    if (!RootTools.isAccessGiven()) {
      EventBus.getDefault().post(new ErrorEvent(this, ERROR_NOT_ROOTED));
      return;
    }

    LocalFile dtFile = new LocalFile(path, filename);
    LocalFile parent = dtFile.getParentFile();
    LocalFile srFile;

    if (asset != null) {
      Assets.transferAsset(asset, filename, MODE_EXECUTABLE);
      srFile = new LocalFile(App.getContext().getFilesDir(), filename);
    } else {
      srFile = binary;
    }

    if (recovery) {
      File updateZip = new File(App.getContext().getFilesDir(), "update.zip");
      BusyBox busybox = BusyBox.newInstance(srFile.path);
      try {
        BusyBoxZipHelper.createBusyboxRecoveryZip(busybox, dtFile.getAbsolutePath(), updateZip);

        File commandTemp = new File(App.getContext().getFilesDir(), "command");
        FileUtils.write(commandTemp, "--update_package=CACHE:busybox.zip'");
        ShellCommand.cp(commandTemp, new File("/cache/recovery/command"));
        ShellCommand.chmod("755", new File("/cache/recovery/command"));
        ShellCommand.cp(updateZip, new File("/cache/busybox.zip"));
        ShellCommand.chmod("755", new File("/cache/busybox.zip"));

        // TWRP reportedly does not find the update package
        FileUtils.write(commandTemp, "install /cache/busybox.zip");
        ShellCommand.cp(commandTemp, new File("/cache/recovery/openrecoveryscript"));
        ShellCommand.chmod("755", new File("/cache/recovery/openrecoveryscript"));

        commandTemp.delete();
        updateZip.delete();

        RebootCommand.REBOOT_RECOVERY.execute();

        EventBus.getDefault().post(new FinishedEvent(this));
      } catch (IOException e) {
        EventBus.getDefault().post(new ErrorEvent(this, "Error creating installable zip"));
        Crashlytics.logException(e);
      }
    } else {
      MountPoint mountPoint;
      try {
        mountPoint = MountPoint.findMountPoint(path);
      } catch (MountPoint.MountPointNotFoundException e) {
        EventBus.getDefault().post(new ErrorEvent(this, "Error getting mount point for " + path));
        return;
      }

      boolean mountedReadWrite = mountPoint.isReadWrite();

      if (!mountPoint.remount("rw") && !mountedReadWrite) {
        EventBus.getDefault().post(new ErrorEvent(this, "Error mounting " + mountPoint.getMountPoint() + " read/write"));
        return;
      }

      if (parent != null && !parent.isDirectory()) {
        if (Storage.isOnRemovableStorage(parent)) {
          FileOperation.mkdir(parent);
        } else if (Storage.isOnPrimaryStorage(parent)) {
          //noinspection ResultOfMethodCallIgnored
          parent.mkdirs();
        } else {
          ShellCommand.mkdir(parent);
          ShellCommand.chmod("0755", parent);
          ShellCommand.chown("root", "shell", parent);
        }
      }

      if (dtFile.path.equals("/sbin/busybox")) {
        dtFile.setEntry(LsCommand.getEntry(dtFile.path));
      }

      if (dtFile.exists()) {
        Uninstaller.uninstall(dtFile);
      }

      if (!ShellCommand.cp(srFile, dtFile) && !dtFile.exists()) {
        EventBus.getDefault().post(new ErrorEvent(this, "Failed copying " + srFile + " to " + dtFile));
        return;
      }

      ShellCommand.chmod("0755", dtFile);
      ShellCommand.chown("root", "root", dtFile);

      BusyBox busyBox = BusyBox.newInstance(dtFile.getAbsolutePath());

      if (overwrite && symlink && Storage.isSystemFile(dtFile)) {
        mountPoint.remount("rw");
        Set<String> applets = busyBox.getApplets();
        for (String applet : applets) {
          File file = new File(path, applet);
          if (file.exists()) {
            ShellCommand.rm(file);
          }
        }
      }

      if (symlink && Storage.isSystemFile(dtFile)) {
        mountPoint.remount("rw");
        if (!Shell.SU.run("\"" + busyBox.path + "\" --install -s \"" + path + "\"").isSuccessful()) {
          // "--install" is not a command, symlink applets one by one.
          Set<String> applets = busyBox.getApplets();
          for (String applet : applets) {
            LocalFile file = new LocalFile(path, applet);
            ShellCommand.ln(busyBox, file);
          }
        }
      }

      if (!mountedReadWrite) {
        mountPoint.remount("ro");
      }

      EventBus.getDefault().post(new FinishedEvent(this));
    }
  }

  public static final class StartEvent {

    public final Installer installer;

    public StartEvent(Installer installer) {
      this.installer = installer;
    }

  }

  public static final class FinishedEvent {

    public final Installer installer;

    public FinishedEvent(Installer installer) {
      this.installer = installer;
    }

  }

  public static final class ErrorEvent {

    public final Installer installer;
    public final String error;

    public ErrorEvent(Installer installer, String error) {
      this.installer = installer;
      this.error = error;
    }

  }

  public static final class Builder implements Parcelable {

    private LocalFile binary;
    private String asset;
    private String path;
    private String filename;
    private boolean symlink;
    private boolean overwrite;
    private boolean recovery;

    private Builder() {
      symlink = Prefs.getInstance().get("symlink_busybox_applets", true);
      overwrite = Prefs.getInstance().get("replace_with_busybox_applets", false);
      recovery = Prefs.getInstance().get("install_busybox_in_recovery", false);
    }

    public Installer create() {
      return new Installer(this);
    }

    public void confirm(Activity activity) {
      ConfirmInstallDialog dialog = new ConfirmInstallDialog();
      Bundle args = new Bundle();
      args.putParcelable("builder", this);
      dialog.setArguments(args);
      // Fix “Error Can not perform this action after onSaveInstanceState”
      // http://stackoverflow.com/a/12464899/1048340
      FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
      transaction.add(dialog, "ConfirmInstallDialog").commitAllowingStateLoss();
      //dialog.show(activity.getFragmentManager(), "ConfirmInstallDialog");
    }

    public Builder setBinary(LocalFile binary) {
      this.binary = binary;
      return this;
    }

    public Builder setAsset(String asset) {
      this.asset = asset;
      return this;
    }

    public Builder setPath(String path) {
      this.path = path;
      return this;
    }

    public Builder setFilename(String filename) {
      this.filename = filename;
      return this;
    }

    public Builder setSymlink(boolean symlink) {
      this.symlink = symlink;
      return this;
    }

    public Builder setOverwrite(boolean overwrite) {
      this.overwrite = overwrite;
      return this;
    }

    public Builder setRecovery(boolean recovery) {
      this.recovery = recovery;
      return this;
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeParcelable(this.binary, 0);
      dest.writeString(this.asset);
      dest.writeString(this.path);
      dest.writeString(this.filename);
      dest.writeByte(symlink ? (byte) 1 : (byte) 0);
      dest.writeByte(overwrite ? (byte) 1 : (byte) 0);
    }

    protected Builder(Parcel in) {
      this.binary = in.readParcelable(LocalFile.class.getClassLoader());
      this.asset = in.readString();
      this.path = in.readString();
      this.filename = in.readString();
      this.symlink = in.readByte() != 0;
      this.overwrite = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Builder> CREATOR = new Parcelable.Creator<Builder>() {

      public Builder createFromParcel(Parcel source) {
        return new Builder(source);
      }

      public Builder[] newArray(int size) {
        return new Builder[size];
      }
    };

  }

  public static class ConfirmInstallDialog extends DialogFragment {

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Builder builder = getArguments().getParcelable("builder");
      return new AlertDialog.Builder(getActivity())
          .setTitle(R.string.confirm_before_install)
          .setMessage(getString(R.string.are_you_sure_you_want_to_install_s_to_s, builder.filename, builder.path))
          .setNegativeButton(android.R.string.cancel, null)
          .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override public void onClick(DialogInterface dialog, int which) {
              new Thread(new Installer(builder)).start();
              dialog.dismiss();
            }
          })
          .create();
    }

  }

}
