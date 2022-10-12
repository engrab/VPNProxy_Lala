package com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.pojoClasses.ServerModel;

import java.io.File;
import java.io.FileOutputStream;

public class OvpnUtils {

    private static final String FILE_EXTENSION = ".ovpn";
    private static final String OPENVPN_PKG_NAME = "net.openvpn.openvpn";
    private static final String OPENVPN_MIME_TYPE = "application/x-openvpn-profile";

    public static String humanReadableCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp-1);
        return String.format("%.2f %s" + (si ? "bps" : "B"),
                bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Writes and saves OVPN profile to a file
     *
     * @param context The context of an application
     * @param serverModel The {@link ServerModel} that contains OVPN profile
     */
    private static void saveConfigData(@NonNull Context context, @NonNull ServerModel serverModel) {
        File file;
        FileOutputStream outputStream;

        try {
            file = getFile(context, serverModel);
            outputStream = new FileOutputStream(file);
            outputStream.write(serverModel.ovpnConfigData.getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an empty file for OVPN profile
     *
     * @param context The context of an application
     * @param serverModel The {@link ServerModel} that contains OVPN profile
     */
    private static File getFile(@NonNull Context context, @NonNull ServerModel serverModel) {
        File filePath;
        if (!Environment.isExternalStorageRemovable() || isExternalStorageWritable()) {
            filePath = context.getExternalCacheDir();
        } else {
            filePath = context.getCacheDir();
        }
        return new File(filePath, serverModel.countryShort + "_" + serverModel.hostName + "_" +
                serverModel.protocol.toUpperCase() + FILE_EXTENSION);
    }

    /**
     * @return Whether the external storage is available for read and write.
     */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static int getDrawableResource(@NonNull Context context, @NonNull String resource) {
        return context.getResources()
                .getIdentifier(resource, "drawable", context.getPackageName());
    }

    /**
     * Shows an intent chooser to share OVPN profile.
     *
     * @param activity The context of an activity
     * @param serverModel The {@link ServerModel} that contains OVPN profile
     */
    public static void shareOvpnFile(@NonNull Activity activity, @NonNull ServerModel serverModel) {
        File file = getFile(activity, serverModel);
        if (!file.exists()) {
            saveConfigData(activity, serverModel);
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getFile(activity, serverModel)));
        activity.startActivity(Intent.createChooser(intent, "Share Profile using"));
    }
}
