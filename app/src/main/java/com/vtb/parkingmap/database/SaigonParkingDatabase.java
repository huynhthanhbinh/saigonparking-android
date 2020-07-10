package com.vtb.parkingmap.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vtb.parkingmap.database.SaigonParkingDatabaseEntity.SaigonParkingDatabaseEntityBuilder;

import java.util.HashMap;
import java.util.Locale;
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
@Getter
public final class SaigonParkingDatabase extends SQLiteOpenHelper {

    public static final String USERNAME_KEY = "username";
    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";

    //BOOKING
    public static final String BOOKINGID_KEY = "bookingid";


    private Map<String, String> authKeyValueMap = new HashMap<>();
    private SaigonParkingDatabaseEntity bookingEntity;

    public SaigonParkingDatabase(@Nullable Context context,
                                 @Nullable String name,
                                 @Nullable SQLiteDatabase.CursorFactory factory,
                                 int version) {

        super(context, name, factory, version);
    }

    public SaigonParkingDatabaseEntity getCurrentBookingEntity() {
        bookingEntity = getFirstRowOfBookingTable();
        return bookingEntity;
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
        authKeyValueMap = getAllRowsOfAuthTable();

        queryData("CREATE TABLE IF NOT EXISTS SAIGON_PARKING_BOOKING ( " +
                "PARKING_LOT_ID INTEGER PRIMARY KEY, " +
                "PARKING_LOT_LAT REAL, " +
                "PARKING_LOT_LNG REAL, " +
                "TARGET_LAT REAL, " +
                "TARGET_LNG REAL, " +
                "CURRENT_LAT REAL, " +
                "CURRENT_LNG REAL, " +
                "MODE INTEGER, " +
                "BOOKING_ID NVARCHAR(1000) " +
                " )");

        bookingEntity = getFirstRowOfBookingTable();
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

        authKeyValueMap = getAllRowsOfAuthTable();
        authKeyValueMap.forEach((key, value) ->
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

    public void deleteAccessToken() {
        deleteRow(ACCESS_TOKEN_KEY);
        authKeyValueMap = getAllRowsOfAuthTable();
        authKeyValueMap.forEach((key, value) ->
                Log.d("BachMap", String.format("Key: %s, Value: %s", key, value)));
    }

    public void updateRefreshToken(String newRefreshToken) {
        updateRow(REFRESH_TOKEN_KEY, newRefreshToken);
        authKeyValueMap = getAllRowsOfAuthTable();
        authKeyValueMap.forEach((key, value) ->
                Log.d("BachMap", String.format("Key: %s, Value: %s", key, value)));
    }

    public void saveNewAccessToken(String newAccessToken) {
        insertRow(ACCESS_TOKEN_KEY, newAccessToken);
        authKeyValueMap = getAllRowsOfAuthTable();
        authKeyValueMap.forEach((key, value) ->
                Log.d("BachMap", String.format("Key: %s, Value: %s", key, value)));
    }

    public void emptyTable() {
        queryData("DELETE FROM SAIGON_PARKING_TABLE WHERE SP_KEY <> ''");
        authKeyValueMap = getAllRowsOfAuthTable();
        authKeyValueMap.forEach((key, value) ->
                Log.d("BachMap", String.format("Key: %s, Value: %s", key, value)));
    }

    private Map<String, String> getAllRowsOfAuthTable() {
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

    private SaigonParkingDatabaseEntity getFirstRowOfBookingTable() {
        Cursor cursor = getWritableDatabase()
                .rawQuery("SELECT " +
                        "B.PARKING_LOT_ID, " +
                        "B.PARKING_LOT_LAT, " +
                        "B.PARKING_LOT_LNG, " +
                        "B.TARGET_LAT, " +
                        "B.TARGET_LNG, " +
                        "B.CURRENT_LAT, " +
                        "B.CURRENT_LNG, " +
                        "B.MODE, " +
                        "B.BOOKING_ID " +
                        "FROM SAIGON_PARKING_BOOKING B " +
                        "LIMIT 1 ", null);

        SaigonParkingDatabaseEntityBuilder entity = SaigonParkingDatabaseEntity.builder();
        if (cursor.moveToNext()) {
            entity.id(cursor.getLong(0));
            entity.latitude(cursor.getDouble(1));
            entity.longitude(cursor.getDouble(2));
            entity.position3lat(cursor.getDouble(3));
            entity.position3long(cursor.getDouble(4));
            entity.mylat(cursor.getDouble(5));
            entity.mylong(cursor.getDouble(6));
            entity.tmptype(cursor.getInt(7));
            entity.bookingid(cursor.getString(8));
        }
        return entity.build();
    }

    public void InsertBookingTable(SaigonParkingDatabaseEntity saigonParkingDatabaseEntity) {
        queryData(String.format(Locale.US,
                "INSERT INTO SAIGON_PARKING_BOOKING VALUES(%d,%f,%f,%f,%f,%f,%f,%d,'%s')",
                saigonParkingDatabaseEntity.getId(),
                saigonParkingDatabaseEntity.getLatitude(),
                saigonParkingDatabaseEntity.getLongitude(),
                saigonParkingDatabaseEntity.getPosition3lat(),
                saigonParkingDatabaseEntity.getPosition3long(),
                saigonParkingDatabaseEntity.getMylat(),
                saigonParkingDatabaseEntity.getMylong(),
                saigonParkingDatabaseEntity.getTmptype(),
                saigonParkingDatabaseEntity.getBookingid()
        ));

        SaigonParkingDatabaseEntity entity = getFirstRowOfBookingTable();
        Log.d("BachMap", "InsertBookingTable: " + entity);
        bookingEntity = entity;
    }

    public void DeleteBookTable(long parkingLotId) {

        Log.d("BachMap", getFirstRowOfBookingTable().toString());
        queryData(String.format("DELETE FROM SAIGON_PARKING_BOOKING WHERE PARKING_LOT_ID = %d", parkingLotId));
        Log.d("BachMap", getFirstRowOfBookingTable().toString());
    }
}