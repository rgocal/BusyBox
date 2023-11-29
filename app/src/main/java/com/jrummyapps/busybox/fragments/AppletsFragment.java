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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaredrummler.fastscrollrecyclerview.FastScrollRecyclerView;
import com.jrummyapps.android.app.ActivityMonitor;
import com.jrummyapps.android.radiant.fragments.RadiantSupportFragment;
import com.jrummyapps.android.widget.jazzylistview.JazzyHelper;
import com.jrummyapps.android.widget.jazzylistview.JazzyRecyclerViewScrollListener;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.activities.AboutActivity;
import com.jrummyapps.busybox.dialogs.AppletUsageDialog;
import com.jrummyapps.busybox.utils.Utils;

import java.util.List;
import java.util.Locale;

public class AppletsFragment extends RadiantSupportFragment {

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_busybox_applets, container, false);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    FastScrollRecyclerView recyclerView = getViewById(R.id.recycler);
    JazzyRecyclerViewScrollListener scrollListener = new JazzyRecyclerViewScrollListener();
    scrollListener.setTransitionEffect(JazzyHelper.SLIDE_IN);
    recyclerView.addOnScrollListener(scrollListener);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.setAdapter(new RecyclerAdapter());
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

  static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
      implements FastScrollRecyclerView.SectionedAdapter {

    private final List<String> applets = Utils.getBusyBoxApplets();

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      return new ViewHolder(inflater.inflate(R.layout.item_busybox_applet, parent, false));
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      holder.text.setText(applets.get(position));
    }

    @Override public int getItemCount() {
      return applets.size();
    }

    @NonNull @Override public String getSectionName(int position) {
      return applets.get(position).substring(0, 1).toUpperCase(Locale.ENGLISH);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

      TextView text;

      ViewHolder(View itemView) {
        super(itemView);
        text = (TextView) itemView.findViewById(R.id.text);
        itemView.setOnClickListener(new View.OnClickListener() {

          @Override public void onClick(View v) {
            Activity activity;
            if (v.getContext() instanceof Activity) {
              activity = (Activity) v.getContext();
            } else {
              activity = ActivityMonitor.getInstance().getCurrentActivity();
            }
            AppletUsageDialog.show(activity, text.getText().toString());
          }
        });
      }
    }

  }

}
