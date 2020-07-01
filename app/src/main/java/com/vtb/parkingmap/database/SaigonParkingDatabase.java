package com.vtb.parkingmap.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@SuppressLint("all")
@SuppressWarnings("all")
/**
 *
 * Implement all necessary database queries
 *
 * @author bvt
 */
public final class SaigonParkingDatabase extends SQLiteOpenHelper {

    private static final String USERNAME_KEY = "username";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    @Getter
    private Map<String, String> keyValueMap = new HashMap<>();

    public SaigonParkingDatabase(@Nullable Context context,
                                 @Nullable String name,
                                 @Nullable SQLiteDatabase.CursorFactory factory,
                                 int version) {

        super(context, name, factory, version);
    }

    //truy vấn không trả kết quả : CREATE , INSERT , UPDATE , DELETE
    public void queryData(String sql) {
        getWritableDatabase().execSQL(sql);
    }

    //truy vấn có trả kết quả: SELECT
    public String getData(String sql) {
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        cursor.moveToFirst();
        String data = cursor.getString(0);
        cursor.close();
        return data;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void createDatabaseIfNotExist() {
        queryData("CREATE TABLE IF NOT EXISTS SAIGON_PARKING_TABLE ( " +
                " SP_KEY NVARCHAR(100) PRIMARY KEY, " +
                " SP_VALUE NVARCHAR(1000) " +
                " )");
        keyValueMap = getAllRows();
    }

    public void saveNewLoginInformation(String username, String accessToken, String refreshToken) {
        Log.d("BachMap", String.format("Authentication information:%nUsername: %s%nAccessToken: %s%nRefreshToken: %s%n",
                username, accessToken, refreshToken));

        /* Function Body will be writen by Bach Map Dit Thui */
        int nRow = Integer.valueOf(getData("SELECT COUNT(SP_KEY) FROM SAIGON_PARKING_TABLE"));
        Log.d("BachMap", String.format("Number of rows currently in table: %d rows", nRow));

        if (nRow == 3) { /* UPDATE ROWS */
            updateRow(USERNAME_KEY, username);
            updateRow(ACCESS_TOKEN_KEY, accessToken);
            updateRow(REFRESH_TOKEN_KEY, refreshToken);

        } else { /* INSERT ROWS */
            insertRow(USERNAME_KEY, username);
            insertRow(ACCESS_TOKEN_KEY, accessToken);
            insertRow(REFRESH_TOKEN_KEY, refreshToken);
        }

        keyValueMap = getAllRows();
        keyValueMap.forEach((key, value) ->
                Log.d("BachMap", String.format("Key: %s, Value: %s", key, value)));
    }

    private void updateRow(String key, String value) {
        queryData(String
                .format("UPDATE SAIGON_PARKING_TABLE SET SP_VALUE= '%s' WHERE SP_KEY= '%s'", value, key));
    }

    private void insertRow(String key, String value) {
        queryData(String
                .format("INSERT INTO SAIGON_PARKING_TABLE VALUES('%s', '%s')", key, value));
    }

    private void deleteRow(String key) {
        queryData(String
                .format("DELETE FROM SAIGON_PARKING_TABLE WHERE SP_KEY= '%s'", key));
    }

    private Map<String, String> getAllRows() {
        Cursor cursor = getWritableDatabase()
                .rawQuery("SELECT * FROM SAIGON_PARKING_TABLE", null);

        Map<String, String> keyValueHashMap = new HashMap<>();
        while (cursor.moveToNext()) {
            String key = cursor.getString(0);
            String value = cursor.getString(1);
            keyValueHashMap.put(key, value);
        }
        return keyValueHashMap;
    }
}