package com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.utils;

import android.util.Base64;

import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.pojoClasses.ServerModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class CsvParser {

    private static final int HOST_NAME = 0;
    private static final int IP_ADDRESS = 1;
    private static final int SCORE = 2;
    private static final int PING = 3;
    private static final int SPEED = 4;
    private static final int COUNTRY_LONG = 5;
    private static final int COUNTRY_SHORT = 6;
    private static final int VPN_SESSION = 7;
    private static final int UPTIME = 8;
    private static final int TOTAL_USERS = 9;
    private static final int TOTAL_TRAFFIC = 10;
    private static final int LOG_TYPE = 11;
    private static final int OPERATOR = 12;
    private static final int MESSAGE = 13;
    private static final int OVPN_CONFIG_DATA = 14;

    private static final int PORT_INDEX = 2;
    private static final int PROTOCOL_INDEX = 1;

    public static ServerModel stringToServer(String line) {
        String[] vpn = line.split(",");

        ServerModel serverModel = new ServerModel();
        serverModel.hostName = vpn[HOST_NAME];
        serverModel.ipAddress = vpn[IP_ADDRESS];
        serverModel.score = Integer.parseInt(vpn[SCORE]);
        serverModel.ping = vpn[PING];
        serverModel.speed = Long.parseLong(vpn[SPEED]);
        serverModel.countryLong = vpn[COUNTRY_LONG];
        serverModel.countryShort = vpn[COUNTRY_SHORT];
        serverModel.vpnSessions = Long.parseLong(vpn[VPN_SESSION]);
        serverModel.uptime = Long.parseLong(vpn[UPTIME]);
        serverModel.totalUsers = Long.parseLong(vpn[TOTAL_USERS]);
        serverModel.totalTraffic = vpn[TOTAL_TRAFFIC];
        serverModel.logType = vpn[LOG_TYPE];
        serverModel.operator = vpn[OPERATOR];
        serverModel.message = vpn[MESSAGE];
        serverModel.ovpnConfigData = new String(Base64.decode(
                vpn[OVPN_CONFIG_DATA], Base64.DEFAULT));

        String[] lines = serverModel.ovpnConfigData.split("[\\r\\n]+");
        serverModel.port = getPort(lines);
        serverModel.protocol = getProtocol(lines);

        return serverModel;
    }

    public static List<ServerModel> parse(Response response) {
        List<ServerModel> serverModels = new ArrayList<>();
        InputStream in = null;
        BufferedReader reader = null;

        try {
            in = response.body().byteStream();
            reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("*") && !line.startsWith("#")) {
                    serverModels.add(stringToServer(line));
                }
            }

        } catch (IOException ignored) {
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (in != null)
                    in.close();
            } catch (IOException ignored) {
            }
        }

        return serverModels;
    }

    /**
     * @return Port used in OVPN file ("remote <HOSTNAME> <PORT>")
     * */
    private static int getPort(String[] lines) {
        int port = 0;
        for (String line : lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("remote")) {
                    port = Integer.parseInt(line.split(" ")[PORT_INDEX]);
                    break;
                }
            }
        }
        return port;
    }

    /**
     * @return Protocol used in OVPN file. ("proto <TCP/UDP>")
     * */
    private static String getProtocol(String[] lines) {
        String protocol = "";
        for (String line : lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("proto")) {
                    protocol = line.split(" ")[PROTOCOL_INDEX];
                    break;
                }
            }
        }
        return protocol;
    }
}
