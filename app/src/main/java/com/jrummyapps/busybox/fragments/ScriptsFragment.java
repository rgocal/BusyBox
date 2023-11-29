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

package com.jrummyapps.busybox.fragments;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.jrummyapps.android.analytics.Analytics;
import com.jrummyapps.android.fileproperties.activities.FilePropertiesActivity;
import com.jrummyapps.android.files.FileIntents;
import com.jrummyapps.android.os.Os;
import com.jrummyapps.android.radiant.fragments.RadiantSupportFragment;
import com.jrummyapps.android.transitions.FabDialogMorphSetup;
import com.jrummyapps.android.util.FileUtils;
import com.jrummyapps.android.view.ViewHolder;
import com.jrummyapps.android.widget.jazzylistview.JazzyListView;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.activities.AboutActivity;
import com.jrummyapps.busybox.activities.CreateScriptActivity;
import com.jrummyapps.busybox.database.Database;
import com.jrummyapps.busybox.database.ShellScriptTable;
import com.jrummyapps.busybox.dialogs.CreateScriptDialog;
import com.jrummyapps.busybox.models.ShellScript;
import com.jrummyapps.busybox.tasks.ScriptLoader;
import com.jrummyapps.texteditor.activities.TextEditorActivity;
import com.jrummyapps.texteditor.shell.activities.ScriptExecutorActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ScriptsFragment extends RadiantSupportFragment
    implements AdapterView.OnItemClickListener, View.OnClickListener {

  public static final int REQUEST_CREATE_SCRIPT = 27;

  private FloatingActionButton fab;
  private JazzyListView listView;
  private Adapter adapter;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    EventBus.getDefault().register(this);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_scripts, container, false);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    listView = getViewById(android.R.id.list);
    fab = getViewById(R.id.fab);
    onRestoreInstanceState(savedInstanceState);
    listView.setOnItemClickListener(this);
    fab.setOnClickListener(this);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (adapter != null) {
      outState.putParcelableArrayList("scripts", adapter.scripts);
    }
  }

  public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
    if (savedInstanceState == null || !savedInstanceState.containsKey("scripts")) {
      new ScriptLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      ArrayList<ShellScript> scripts = savedInstanceState.getParcelableArrayList("scripts");
      adapter = new Adapter(scripts);
      listView.setAdapter(adapter);
    }
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.add(0, R.id.action_settings, 0, R.string.settings)
        .setIcon(R.drawable.ic_settings_white_24dp)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    menu.add(0, R.id.action_info, 0, R.string.about)
        .setIcon(R.drawable.ic_information_white_24dp)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.action_info) {
      startActivity(new Intent(getActivity(), AboutActivity.class));
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CREATE_SCRIPT && resultCode == Activity.RESULT_OK) {
      String name = data.getStringExtra(CreateScriptActivity.EXTRA_SCRIPT_NAME);
      String filename = data.getStringExtra(CreateScriptActivity.EXTRA_FILE_NAME);
      createScript(name, filename);
      return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    final ShellScript script = adapter.getItem(position);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      // hack to get the popupmenu color working on Android 6.0+ for custom color schemes
      ContextCompat.getDrawable(getActivity(), R.drawable.bg_popup_dark);
      ContextCompat.getDrawable(getActivity(), R.drawable.bg_popup_light);
    }

    PopupMenu popupMenu = new PopupMenu(getActivity(), view);

    popupMenu.getMenu().add(0, 1, 0, R.string.run).setIcon(R.drawable.ic_play_white_24dp);
    popupMenu.getMenu().add(0, 2, 0, R.string.edit).setIcon(R.drawable.ic_edit_white_24dp);
    popupMenu.getMenu().add(0, 3, 0, R.string.info).setIcon(R.drawable.ic_information_white_24dp);
    popupMenu.getMenu().add(0, 4, 0, R.string.delete).setIcon(R.drawable.ic_delete_white_24dp);

    getRadiant().tint(popupMenu.getMenu()).forceIcons().apply(getActivity());

    Analytics.newEvent("clicked script")
        .put("script_name", script.name)
        .put("script_file", script.path)
        .log();

    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

      @Override public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case 1: { // run
            Intent intent = new Intent(getActivity(), ScriptExecutorActivity.class);
            intent.putExtra(FileIntents.INTENT_EXTRA_PATH, script.path);
            startActivity(intent);
            return true;
          }
          case 2: { // edit
            Intent intent = new Intent(getActivity(), TextEditorActivity.class);
            intent.putExtra(FileIntents.INTENT_EXTRA_PATH, script.path);
            startActivity(intent);
            return true;
          }
          case 3: { // info
            Intent intent = new Intent(getActivity(), FilePropertiesActivity.class);
            intent.putExtra(FileIntents.INTENT_EXTRA_FILE, new File(script.path));
            intent.putExtra(FilePropertiesActivity.EXTRA_DESCRIPTION, script.info);
            startActivity(intent);
            return true;
          }
          case 4: { // delete
            ShellScriptTable table = Database.getInstance().getTable(ShellScriptTable.NAME);
            boolean deleted = table.delete(script) != 0;
            if (deleted) {
              adapter.scripts.remove(script);
              adapter.notifyDataSetChanged();
              new File(script.path).delete();
            }
            return true;
          }
          default:
            return false;
        }
      }
    });

    popupMenu.show();
  }

  @Override public void onClick(View view) {
    if (view == fab) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Intent intent = new Intent(getActivity(), CreateScriptActivity.class);
        intent.putExtra(FabDialogMorphSetup.EXTRA_SHARED_ELEMENT_START_COLOR, getRadiant().accentColor());
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
            getActivity(), view, getString(R.string.morphing_dialog_transition));
        getActivity().startActivityForResult(intent, REQUEST_CREATE_SCRIPT, options.toBundle());
      } else {
        new CreateScriptDialog().show(getActivity().getFragmentManager(), "CreateScriptDialog");
      }
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEvent(ScriptLoader.ScriptsLoadedEvent event) {
    adapter = new Adapter(event.scripts);
    listView.setAdapter(adapter);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEvent(CreateScriptDialog.CreateScriptEvent event) {
    createScript(event.name, event.filename);
  }

  private void createScript(String name, String filename) {
    File file = new File(getActivity().getFilesDir(), "scripts/" + filename);
    ShellScript script = new ShellScript(name, file.getAbsolutePath());
    int errorMessage = 0;

    for (ShellScript shellScript : adapter.scripts) {
      if (shellScript.path.equals(script.path) || shellScript.name.equals(script.name)) {
        errorMessage = R.string.a_script_with_that_name_already_exists;
        break;
      }
    }

    try {
      FileUtils.touch(file);
      //noinspection OctalInteger
      Os.chmod(file.getAbsolutePath(), 0755);
    } catch (IOException e) {
      errorMessage = R.string.an_error_occurred_while_creating_the_file;
      Crashlytics.logException(e);
    }

    if (errorMessage != 0) {
      Snackbar snackbar = Snackbar.make(getViewById(R.id.fab), errorMessage, Snackbar.LENGTH_LONG);
      View view = snackbar.getView();
      TextView messageText = (TextView) view.findViewById(R.id.snackbar_text);
      if (getRadiant().isDark()) {
        messageText.setTextColor(getRadiant().primaryTextColor());
        view.setBackgroundColor(getRadiant().backgroundColorDark());
      } else {
        messageText.setTextColor(Color.WHITE);
      }
      snackbar.show();
      return;
    }

    ShellScriptTable table = Database.getInstance().getTable(ShellScriptTable.NAME);
    table.insert(script);
    adapter.scripts.add(script);
    adapter.notifyDataSetChanged();

    Intent intent = new Intent(getActivity(), TextEditorActivity.class);
    intent.putExtra(FileIntents.INTENT_EXTRA_PATH, script.path);
    startActivity(intent);
  }

  private final class Adapter extends BaseAdapter {

    private final ArrayList<ShellScript> scripts;

    public Adapter(ArrayList<ShellScript> scripts) {
      this.scripts = scripts;
    }

    @Override public int getCount() {
      return scripts.size();
    }

    @Override public ShellScript getItem(int position) {
      return scripts.get(position);
    }

    @Override public long getItemId(int position) {
      return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
      final ShellScript script = getItem(position);
      final ViewHolder holder;
      if (convertView == null) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        convertView = inflater.inflate(R.layout.item_script, parent, false);
        holder = new ViewHolder(convertView);

        ImageView imageView = holder.find(R.id.icon);
        imageView.setColorFilter(getRadiant().accentColor());
        imageView.setImageResource(R.drawable.ic_code_array_white_24dp);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }
      holder.setText(R.id.text, script.name);
      return convertView;
    }

  }

}
