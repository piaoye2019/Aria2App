package com.gianlu.aria2app.Profile;

import android.content.Context;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProfileItem implements Serializable {
    public String fileName;
    public String globalProfileName;
    public boolean singleMode;
    public boolean notificationsEnabled;
    public STATUS status;
    public String statusMessage;
    public Long latency = -1L;
    boolean isDefault;

    ProfileItem() {
    }

    public static boolean exists(Context context, String fileName) {
        if (fileName == null) return false;

        try {
            context.openFileInput(fileName);
            return true;
        } catch (FileNotFoundException ex) {
            return false;
        }
    }

    public static boolean isSingleMode(Context context, String fileName) throws JSONException, IOException {
        if (fileName == null) return false;

        FileInputStream in = context.openFileInput(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return new JSONObject(builder.toString()).isNull("conditions");
    }

    public static List<ProfileItem> getProfiles(Context context) {
        List<ProfileItem> profiles = new ArrayList<>();
        File files[] = context.getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(".profile");
            }
        });

        for (File profile : files) {
            try {
                if (ProfileItem.isSingleMode(context, profile.getName())) {
                    profiles.add(SingleModeProfileItem.fromName(context, profile.getName()));
                } else {
                    profiles.add(MultiModeProfileItem.fromName(context, profile.getName()));
                }
            } catch (Exception ignored) {
            }
        }

        return profiles;
    }

    public static String getProfileName(String fileName) {
        if (fileName == null) return null;

        return new String(Base64.decode(fileName.replace(".profile", "").getBytes(), Base64.DEFAULT));
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    void setLatency(Long latency) {
        this.latency = latency;
    }

    void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public enum STATUS {
        ONLINE,
        OFFLINE,
        ERROR,
        UNKNOWN
    }
}