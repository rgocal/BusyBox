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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import com.jrummyapps.android.files.FileOperation;
import com.jrummyapps.android.files.LocalFile;
import com.jrummyapps.android.roottools.RootTools;
import com.jrummyapps.busybox.R;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class Uninstaller implements Runnable {

  public static void showConfirmationDialog(Activity activity, LocalFile file) {
    ConfirmUninstallDialog dialog = new ConfirmUninstallDialog();
    Bundle args = new Bundle();
    args.putParcelable("file", file);
    dialog.setArguments(args);
    dialog.show(activity.getFragmentManager(), "ConfirmUninstallDialog");
  }

  public static boolean uninstall(LocalFile binary) {
    return FileOperation.delete(binary) && uninstallSymlinks(binary);
  }

  public static boolean uninstallSymlinks(LocalFile binary) {
    List<LocalFile> symlinks = getSymlinks(binary);
    if (symlinks.isEmpty()) {
      return true;
    }

    String rm = RootTools.getTool("rm");
    if (rm != null) {
      StringBuilder command = new StringBuilder(rm);
      for (LocalFile symlink : symlinks) {
        command.append(" \"").append(symlink.path).append("\"");
      }
      if (RootTools.remountThenRun(binary, command.toString()).isSuccessful()) {
        return true;
      }
    }

    for (LocalFile symlink : symlinks) {
      if (!FileOperation.delete(symlink)) {
        return false;
      }
    }

    return true;
  }

  private static List<LocalFile> getSymlinks(LocalFile binary) {
    List<LocalFile> symlinks = new ArrayList<>();
    LocalFile parent = binary.getParentFile();
    if (parent != null) {
      LocalFile[] files = parent.listFiles();
      if (files != null) {
        for (LocalFile file : files) {
          if (file.isSymlink() && file.getCanonicalFile().equals(binary)) {
            symlinks.add(file);
          }
        }
      }
    }
    return symlinks;
  }

  private final LocalFile file;

  public Uninstaller(LocalFile file) {
    this.file = file;
  }

  @Override public void run() {
    EventBus.getDefault().post(new StartEvent(file));
    EventBus.getDefault().post(new FinishedEvent(file, uninstall(file)));
  }

  public static final class StartEvent {

    public final LocalFile file;

    public StartEvent(LocalFile file) {
      this.file = file;
    }

  }

  public static final class FinishedEvent {

    public final LocalFile file;
    public final boolean success;

    public FinishedEvent(LocalFile file, boolean success) {
      this.file = file;
      this.success = success;
    }

  }

  public static class ConfirmUninstallDialog extends DialogFragment {

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
      final LocalFile file = getArguments().getParcelable("file");
      return new AlertDialog.Builder(getActivity())
          .setTitle(R.string.confirm_before_deleting)
          .setMessage(getString(R.string.are_you_sure_you_want_to_uninstall_s, file.name))
          .setNegativeButton(android.R.string.cancel, null)
          .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override public void onClick(DialogInterface dialog, int which) {
              new Thread(new Uninstaller(file)).start();
              dialog.dismiss();
            }
          })
          .create();
    }

  }

}
