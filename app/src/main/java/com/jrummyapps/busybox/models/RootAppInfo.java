package com.jrummyapps.busybox.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class RootAppInfo implements Parcelable {

    @SerializedName("app_name")
    private String appName;
    @SerializedName("description")
    private String description;
    @SerializedName("url")
    private String url;
    @SerializedName("icon")
    private String iconUri;
    @SerializedName("pname")
    private String packageName;

    RootAppInfo(Parcel in) {
        appName = in.readString();
        description = in.readString();
        url = in.readString();
        iconUri = in.readString();
        packageName = in.readString();
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(description);
        dest.writeString(url);
        dest.writeString(iconUri);
        dest.writeString(packageName);
    }

    public static final Creator<RootAppInfo> CREATOR = new Creator<RootAppInfo>() {

        @Override public RootAppInfo createFromParcel(Parcel in) {
            return new RootAppInfo(in);
        }

        @Override public RootAppInfo[] newArray(int size) {
            return new RootAppInfo[size];
        }

    };

    public String getAppName() {
        return appName;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getIconUri() {
        return iconUri;
    }

    public String getPackageName() {
        return packageName;
    }

}
