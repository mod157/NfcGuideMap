package com.guidmap.ss2.nfcguidmap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by 김상현 on 2016-03-18.
 */
public class DbHelper {

    SQLiteDatabase database;    //SQLite database 생성하는 객체명
    String dbName;  //DB 명
    String createTable1;
    String tableName1;
    int num, checknum;

    ArrayList<String> arrname;
    ArrayList<String> arrtime;
    ArrayList<String> arrid_bssid;
    public DbHelper() {
        arrname = new ArrayList<String>();
        arrtime = new ArrayList<String>();
        arrid_bssid = new ArrayList<String>();

        tableName1 = null;
        dbName = "NFC_db";
        createTable1 = "CREATE TABLE 'set_table' ('id' integer primary key autoincrement, 'name' text, 'time' text);";
        //createTable1: set_table 테이블 생성 sql
    }

    public void closeDatabase(){
        database.close();
    }
    public void createTable() {
        String checksql = "select * from sqlite_master Where Name = 'set_table';";
        //sqlite_master 테이블을 통해 앱 구동에 필요한 테이블이 생성되었는지 확인
        Cursor check = database.rawQuery(checksql, null);
        check.moveToFirst();
        while (!check.isAfterLast()) {
            if (check.getString(1).equals("set_table")) {
                tableName1 = "set_table";
            }
            check.moveToNext();
        }
        checknum = check.getCount();
        check.close();
        if (checknum == 0) {
            //앱 구동에 필요한 테이블이 하나도 생성 안된 경우
            try {
                database.execSQL(createTable1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void insertData(String name, String time){
        database.beginTransaction();
        try{
            String sql1 = "INSERT INTO set_table ('name', 'time') VALUES ('"+ name +"', '"+ time +"');";
            database.execSQL(sql1); //sql1 실행
            database.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            database.endTransaction();
        }
    }

    public void showallData(){
        String sql2 = "select \"name\", \"time\" from set_table";
        Cursor result = database.rawQuery(sql2, null);  //sql2 실행 후 커서로 받아서 값을 뽑아냄
        result.moveToFirst();
        while(!result.isAfterLast()){
            arrname.add(result.getString(0));
            arrtime.add(result.getString(1));
            result.moveToNext();
        }
        num = result.getCount();
        result.close();
    }

    public void delete_name(String name, String time){
        database.beginTransaction();
        try{
            String de_sql1 = "Delete from set_table where \"name\" = '"+ name +"' and \"time\" = '"+ time +"';";
            database.execSQL(de_sql1);
            database.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            database.endTransaction();
        }
    }

    public ArrayList<String> select_ssid(String name, String ssid){
        ArrayList<String> resArr2 = new ArrayList<String>();

        String sel_sql1 = "select \"volume\", \"light\", \"vibe\", \"connect\" from set_table where \"name\" = '"+ name +"' and \"ssid\" = '"+ ssid +"';";
        //name과 ssid을 이용해 DB에서 설정값을 뽑아오는 sql문
        Cursor selCur = database.rawQuery(sel_sql1, null);
        selCur.moveToFirst();
        while(!selCur.isAfterLast()){
            resArr2.add(selCur.getString(0));
            resArr2.add(selCur.getString(1));
            resArr2.add(selCur.getString(2));
            resArr2.add(selCur.getString(3));
            selCur.moveToNext();
        }
        selCur.close();
        return resArr2;
    }

    public void delete_bssid(String id_name, String id_ssid){
        database.beginTransaction();
        try{
            String de_sql2 = "Delete from bssid_table where \"id_name\" = '"+ id_name +"' and \"id_ssid\" = '"+ id_ssid +"';";
            //bssid는 여러개이므로 목록 수정 시 기존의 bssid를 모두 삭제 후 새로 bssid를 저장
            //같은 Wifi Zone이어도 위치에 따라 인식하는 bssid의 종류와 개수는 다를 수 있다.
            database.execSQL(de_sql2);
            database.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            database.endTransaction();
        }
    }
}