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

package com.jrummyapps.busybox.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.radiant.Radiant;
import com.jrummyapps.android.radiant.activity.RadiantActivity;
import com.jrummyapps.android.transitions.FabDialogMorphSetup;
import com.jrummyapps.android.transitions.TransitionUtils;
import com.jrummyapps.android.util.KeyboardUtils;
import com.jrummyapps.android.util.ResUtils;
import com.jrummyapps.busybox.R;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CreateScriptActivity extends RadiantActivity implements View.OnClickListener {

  public static final String EXTRA_SCRIPT_NAME = "script_name";
  public static final String EXTRA_FILE_NAME = "file_name";

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

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_create_script);

    FabDialogMorphSetup.setupSharedEelementTransitions(this, findViewById(R.id.container), ResUtils.dpToPx(2));

    if (getWindow().getSharedElementEnterTransition() != null) {
      getWindow().getSharedElementEnterTransition().addListener(new TransitionUtils.TransitionListenerAdapter() {

        @Override public void onTransitionEnd(Transition transition) {
          editScriptName.postDelayed(new Runnable() {

            @Override public void run() {
              KeyboardUtils.showKeyboard(editScriptName, true);
            }
          }, 250);
        }
      });
    } else {
      editScriptName.postDelayed(new Runnable() {

        @Override public void run() {
          KeyboardUtils.showKeyboard(editScriptName, true);
        }
      }, 250);
    }

    positiveButton = getViewById(R.id.positive_button);
    editScriptName = getViewById(R.id.script_name);
    editFileName = getViewById(R.id.file_name);

    editFileName.setOnFocusChangeListener(new View.OnFocusChangeListener() {

      @Override public void onFocusChange(View v, boolean hasFocus) {
        fromUser = hasFocus;
      }
    });

    editScriptName.addTextChangedListener(scriptNameTextWatcher);
    editFileName.addTextChangedListener(fileNameTextWatcher);
    positiveButton.setOnClickListener(this);
  }

  @Override public void onBackPressed() {
    dismiss(null);
  }

  @Override public void onClick(View v) {
    if (v == positiveButton) {
      Analytics.newEvent("created script").log();
      Intent intent = new Intent();
      intent.putExtra(EXTRA_SCRIPT_NAME, editScriptName.getText().toString());
      intent.putExtra(EXTRA_FILE_NAME, editFileName.getText().toString());
      setResult(Activity.RESULT_OK, intent);
      finish();
    }
  }

  @Override public int getThemeResId() {
    if (getRadiant().getBaseTheme() == Radiant.BaseTheme.DARK) {
      return R.style.Radiant_Dark_NoActionBar_MaterialDialog;
    }
    return R.style.Radiant_Light_NoActionBar_MaterialDialog;
  }

  public void dismiss(View view) {
    setResult(Activity.RESULT_CANCELED);
    finishAfterTransition();
  }

}
