package com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.database.ServerConstant.ServerEntry;

import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.pojoClasses.ServerModel;

import java.util.ArrayList;
import java.util.List;


public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "serverdata.db";
    private static DbHelper instance;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ServerDatabase.SQL_CREATE_SERVER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ServerDatabase.SQL_DELETE_SERVER);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void save(List<ServerModel> serverModels) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();

        deleteOld(db);

        for (ServerModel serverModel : serverModels) {
            ContentValues values = getContentValues(serverModel);
            db.insert(ServerEntry.TABLE_NAME, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    /** Deletes all servers except for starred */
    public void deleteOld(SQLiteDatabase db) {
        db.delete(ServerEntry.TABLE_NAME,
                ServerEntry.COLUMN_NAME_IS_STARRED + " = 0",
                null);
    }

    public void setStarred(String ipAddress, boolean isStarred) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ServerEntry.COLUMN_NAME_IS_STARRED, isStarred ? 1 : 0);
        db.update(ServerEntry.TABLE_NAME, values,
                ServerConstant.ServerEntry.COLUMN_NAME_IP_ADDRESS + " = ?", new String[]{ipAddress});
    }

    public List<ServerModel> getAll() {
        SQLiteDatabase db = this.getWritableDatabase();

        List<ServerModel> serverModels = new ArrayList<>();
        Cursor cursor = db.query(ServerConstant.ServerEntry.TABLE_NAME,
                ServerConstant.ServerEntry.ALL_COLUMNS, null, null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                serverModels.add(cursorToServer(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();

        return serverModels;
    }

    private ContentValues getContentValues(ServerModel serverModel) {
        ContentValues values = new ContentValues();
        values.put(ServerEntry.COLUMN_NAME_HOST_NAME, serverModel.hostName);
        values.put(ServerEntry.COLUMN_NAME_IP_ADDRESS, serverModel.ipAddress);
        values.put(ServerEntry.COLUMN_NAME_SCORE, serverModel.score);
        values.put(ServerEntry.COLUMN_NAME_PING, serverModel.ping);
        values.put(ServerEntry.COLUMN_NAME_SPEED, serverModel.speed);
        values.put(ServerEntry.COLUMN_NAME_COUNTRY_LONG, serverModel.countryLong);
        values.put(ServerEntry.COLUMN_NAME_COUNTRY_SHORT, serverModel.countryShort);
        values.put(ServerEntry.COLUMN_NAME_VPN_SESSIONS, serverModel.vpnSessions);
        values.put(ServerEntry.COLUMN_NAME_UPTIME, serverModel.uptime);
        values.put(ServerEntry.COLUMN_NAME_TOTAL_USERS, serverModel.totalUsers);
        values.put(ServerEntry.COLUMN_NAME_TOTAL_TRAFFIC, serverModel.totalTraffic);
        values.put(ServerEntry.COLUMN_NAME_LOG_TYPE, serverModel.logType);
        values.put(ServerEntry.COLUMN_NAME_OPERATOR, serverModel.operator);
        values.put(ServerEntry.COLUMN_NAME_OPERATOR_MESSAGE, serverModel.message);
        values.put(ServerEntry.COLUMN_NAME_CONFIG_DATA, serverModel.ovpnConfigData);
        values.put(ServerEntry.COLUMN_NAME_PORT, serverModel.port);
        values.put(ServerEntry.COLUMN_NAME_PROTOCOL, serverModel.protocol);
        values.put(ServerEntry.COLUMN_NAME_IS_OLD, 0);
        values.put(ServerEntry.COLUMN_NAME_IS_STARRED, serverModel.isStarred ? 1 : 0);
        return values;
    }

    private ServerModel cursorToServer(Cursor cursor) {
        ServerModel serverModel = new ServerModel();
        serverModel.hostName = cursor.getString(1);
        serverModel.ipAddress = cursor.getString(2);
        serverModel.score = cursor.getInt(3);
        serverModel.ping = cursor.getString(4);
        serverModel.speed = cursor.getLong(5);
        serverModel.countryLong = cursor.getString(6);
        serverModel.countryShort = cursor.getString(7);
        serverModel.vpnSessions = cursor.getLong(8);
        serverModel.uptime = cursor.getLong(9);
        serverModel.totalUsers = cursor.getLong(10);
        serverModel.totalTraffic = cursor.getString(11);
        serverModel.logType = cursor.getString(12);
        serverModel.operator = cursor.getString(13);
        serverModel.message = cursor.getString(14);
        serverModel.ovpnConfigData = cursor.getString(15);
        serverModel.port = cursor.getInt(16);
        serverModel.protocol = cursor.getString(17);
        serverModel.isStarred = cursor.getInt(19) == 1;
        return serverModel;
    }
}
