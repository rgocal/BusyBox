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

package com.jrummyapps.busybox.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.radiant.Radiant;
import com.jrummyapps.android.util.KeyboardUtils;
import com.jrummyapps.busybox.R;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;

public class CreateScriptDialog extends DialogFragment {

  Button positiveButton;
  EditText editScriptName;
  EditText editFileName;

   boolean setFileNameText = true;
   boolean fromUser = false;

  private final TextWatcher fileNameTextWatcher = new TextWatcher() {

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override public void afterTextChanged(Editable s) {
      positiveButton.setEnabled(editFileName.getText().toString().trim().length() > 0
          && editScriptName.getText().toString().trim().length() > 0
          && !editFileName.getText().toString().equals(".sh"));
      setFileNameText = !fromUser; // only modify the file name if the user has never changed the text
    }

  };

  private final TextWatcher scriptNameTextWatcher = new TextWatcher() {

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override public void afterTextChanged(Editable s) {
      positiveButton.setEnabled(editFileName.getText().toString().trim().length() > 0
          && editScriptName.getText().toString().trim().length() > 0
          && !editFileName.getText().toString().equals(".sh"));
      if (setFileNameText) {
        String filename = s.toString().toLowerCase(Locale.ENGLISH).replaceAll(" ", "-") + ".sh";
        editFileName.setText(filename);
      }
    }
  };

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

    LayoutInflater inflater = getActivity().getLayoutInflater();
    @SuppressLint("InflateParams")
    View view = inflater.inflate(R.layout.dialog_create_script, null);

    editScriptName = (EditText) view.findViewById(R.id.script_name);
    editFileName = (EditText) view.findViewById(R.id.file_name);

    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.create_script)
        .setView(view)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {

          @Override public void onClick(DialogInterface dialog, int which) {
            Analytics.newEvent("created script").log();
            String name = editScriptName.getText().toString();
            String filename = editFileName.getText().toString();
            EventBus.getDefault().post(new CreateScriptEvent(name, filename));
          }
        })
        .create();
  }

  @Override public void onStart() {
    super.onStart();

    AlertDialog dialog = (AlertDialog) getDialog();
    Radiant radiant = Radiant.getInstance(getActivity());
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(radiant.primaryTextColor());

    positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
    positiveButton.setTextColor(radiant.accentColor());
    positiveButton.setEnabled(false);

    editFileName.setOnFocusChangeListener(new View.OnFocusChangeListener() {

      @Override public void onFocusChange(View v, boolean hasFocus) {
        fromUser = hasFocus;
      }
    });

    editScriptName.addTextChangedListener(scriptNameTextWatcher);
    editFileName.addTextChangedListener(fileNameTextWatcher);

    KeyboardUtils.showKeyboard(editScriptName, true);
  }

  public static final class CreateScriptEvent {

    public final String name;
    public final String filename;

    CreateScriptEvent(String name, String filename) {
      this.name = name;
      this.filename = filename;
    }

  }

}
