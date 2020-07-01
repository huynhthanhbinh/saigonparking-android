package com.vtb.parkingmap.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

@SuppressLint("all")
@SuppressWarnings("all")
public final class SaigonParkingDatabase extends SQLiteOpenHelper {


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
        Cursor cursor = getWritableDatabase().rawQuery(sql, null);
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
    }

    public void saveNewLoginInformation(String username, String accessToken, String refreshToken) {
        Log.d("BachMap", String.format("Authentication information:%nUsername: %s%nAccessToken: %s%nRefreshToken: %s%n",
                username, accessToken, refreshToken));

        /* Function Body will be writen by Nach Map Dit Thui */

    }
}