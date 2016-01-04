package com.btw.snaptao.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thp on 2015/12/21.
 */
public class LocationDB extends SQLiteOpenHelper {
    public static String TAG = "LocationDB";
    private static int VERSION = 1;

    public LocationDB(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public LocationDB(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public LocationDB(Context context, String name) {
        this(context, name, VERSION);

    }

    //    KEY AUTOINCREMENT
    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * 创建position 表
         * lat 纬度
         * lng 经度
         */
        String sql = "create table position(id INTEGER PRIMARY KEY,lat double,lng double)";
        db.execSQL(sql);
        insertData(db, 111, 111);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 插入数据
     *
     * @param db
     * @param lat 纬度
     * @param lng 经度
     */

    public void insertData(SQLiteDatabase db, double lat, double lng) {
        String sql = "insert into position values(?,?,?)";
        db.execSQL(sql, new String[]{"1", String.valueOf(lat), String.valueOf(lng)});
    }

    public void updateData(SQLiteDatabase db, double lat, double lng) {


        db.execSQL("update position SET lat=?, lng=? WHERE id=?", new Object[]{lat, lng, 1});

        db.close();

    }

    /**
     * 返回查询数据
     *
     * @return
     */
    public ArrayList<Map<String, Double>> getCursorToList() {
        ArrayList<Map<String, Double>> result = new ArrayList<Map<String, Double>>();

        String sql = "select * from position";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            Map<String, Double> map = new HashMap<String, Double>();
            map.put("lat", cursor.getDouble(1));
            map.put("lng", cursor.getDouble(2));
            result.add(map);
        }
        cursor.close();
        return result;
    }
}
