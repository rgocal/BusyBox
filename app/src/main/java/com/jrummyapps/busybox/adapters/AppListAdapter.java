package com.jrummyapps.busybox.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrummyapps.android.view.ViewHolder;
import com.jrummyapps.busybox.R;
import com.jrummyapps.busybox.models.RootAppInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends BaseAdapter {

    private final ArrayList<RootAppInfo> appInfos = new ArrayList<>();

    private OnClickListener clickListener;

    public AppListAdapter(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override public int getCount() {
        return appInfos.size();
    }

    @Override public RootAppInfo getItem(int position) {
        return appInfos.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_app, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final RootAppInfo rootAppInfo = getItem(position);
        ImageView imageView = holder.find(R.id.app_icon);
        TextView appName = holder.find(R.id.app_name);
        TextView appInfo = holder.find(R.id.app_info);
        appName.setText(rootAppInfo.getAppName());
        appInfo.setText(rootAppInfo.getDescription());
        Picasso.with(parent.getContext())
                .load(rootAppInfo.getIconUri())
                .placeholder(R.drawable.ic_image_gray_50dp)
                .into(imageView);

        TextView downloadTv = holder.find(R.id.download_tv);
        downloadTv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                clickListener.onClick(rootAppInfo);
            }
        });
        return convertView;
    }

    public void setAppInfos(@NonNull List<RootAppInfo> appInfos) {
        this.appInfos.clear();
        this.appInfos.addAll(appInfos);
    }

    public interface OnClickListener {
        void onClick(RootAppInfo rootAppInfo);
    }

}
