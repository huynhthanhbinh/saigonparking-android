package com.vtb.parkingmap.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

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
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    //truy vấn có trả kết quả: SELECT
    public String getData(String sql) {
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
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
}