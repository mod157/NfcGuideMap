package com.guidmap.ss2.nfcguidmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;

/**
 * Created by SunJae on 2016-11-17.
 */

public class ListActivity extends AppCompatActivity {
    DbHelper dbHelper;
    ListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //=========구현===============================================
        dbHelper = new DbHelper();

        dbHelper.database = openOrCreateDatabase(dbHelper.dbName, MODE_WORLD_WRITEABLE, null);

        dbHelper.createTable();
        dbHelper.showallData();        //dbHelper.showallData2()는 bssid 목록 확인하는 메소드//dbHelper.showallData()는 기존 메소드
        dbHelper.closeDatabase();
        //============================================================

        for(int i=0; i < dbHelper.num; i++){
          //  listAdapter.("⚪ 　"+dbHelper.arrname.get(i).toString()+"　"+dbHelper.arrssid.get(i).toString()+"　");
            if(dbHelper.arrname.get(i).toString().equals("기본") == true) {
                Log.v("db","dbHelper.arrname.get(i).toString().equals(\"기본\")");
           //     basename = true;
            }
        }
    }
}
