package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.pref;

import android.content.Context;
import android.content.SharedPreferences;

import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.pojoClasses.ServerModel;

public class SharedPreference {

    private static final String APP_PREFS_NAME = "VPNProxy";

    private SharedPreferences mPreference;
    private SharedPreferences.Editor mPrefEditor;
    private Context context;

    private static final String SERVER_COUNTRY_LONG = "server_country_long";
    private static final String SERVER_COUNTRY_SHORT = "server_country_short";
    private static final String SERVER_SPEED = "server_speed";
    private static final String SERVER_PING = "server_ping";
    private static final String SERVER_PROTOCOL = "server_protocol";
    private static final String SERVER_IP_ADDRESS = "server_ip";
    private static final String SERVER_HOSTNAME = "server_hostname";
    private static final String SERVER_OVPN = "server_ovpn";
    private static final String SERVER_PORT = "server_port";

    public SharedPreference(Context context) {
        this.mPreference = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE);
        this.mPrefEditor = mPreference.edit();
        this.context = context;
    }

    /**
     * Save server details
     *
     * @param serverModel details of ovpn server
     */
    public void saveServer(ServerModel serverModel) {
        mPrefEditor.putString(SERVER_HOSTNAME, serverModel.getHostName());
        mPrefEditor.putString(SERVER_IP_ADDRESS, serverModel.getIpAddress());
        mPrefEditor.putString(SERVER_COUNTRY_LONG, serverModel.getCountryLong());
        mPrefEditor.putString(SERVER_COUNTRY_SHORT, serverModel.getCountryShort());
        mPrefEditor.putLong(SERVER_SPEED, serverModel.getSpeed());
        mPrefEditor.putString(SERVER_PING, serverModel.getPing());
        mPrefEditor.putString(SERVER_PROTOCOL, serverModel.getProtocol());
        mPrefEditor.putString(SERVER_OVPN, serverModel.getOvpnConfigData());
        mPrefEditor.putInt(SERVER_PORT, serverModel.getPort());
        mPrefEditor.commit();
    }

    /**
     * Get server data from shared preference
     *
     * @return server model object
     */
    public ServerModel getServer() {

        ServerModel serverModel = new ServerModel(
                mPreference.getString(SERVER_HOSTNAME,"Japan"),
                mPreference.getString(SERVER_IP_ADDRESS,"x.x.x.x"),
                mPreference.getString(SERVER_PING,"10ms"),
                mPreference.getLong(SERVER_SPEED,10),
                mPreference.getString(SERVER_COUNTRY_LONG,"Japan"),
                mPreference.getString(SERVER_COUNTRY_SHORT,"Japan"),
                mPreference.getString(SERVER_OVPN,"null"),
                mPreference.getInt(SERVER_PORT,402),
                mPreference.getString(SERVER_PROTOCOL,"UDP")
        );

        return serverModel;
    }

    public Boolean isPrefsHasServer() {
        return mPreference.contains(SERVER_IP_ADDRESS);
    }
}
