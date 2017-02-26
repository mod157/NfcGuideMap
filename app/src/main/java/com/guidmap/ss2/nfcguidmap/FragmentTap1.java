package com.guidmap.ss2.nfcguidmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by SunJae on 2016-10-30.
 */

public class FragmentTap1 extends Fragment implements View.OnClickListener{
    Button btnEnroll, btnReset,btnList,btnSearchMap;
    Intent intent;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragmenttap1, container, false );
        initSet(view);
        return view;
    }
    private void initSet(View view){
        btnEnroll = (Button) view.findViewById(R.id.btnEnroll);
        btnReset = (Button) view.findViewById(R.id.btnrefresh);
        btnList = (Button) view.findViewById(R.id.btnList);
        btnSearchMap = (Button) view.findViewById(R.id.btnTagSearch);

        btnEnroll.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnList.setOnClickListener(this);
        btnSearchMap.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnEnroll:
                //등록하기 page 이동
                intent = new Intent(getActivity(), EnrollActivity.class);
                intent.putExtra("status","1");
                startActivity(intent);
                Log.v("Sun_Fr1_Enrool OK","intent");
                break;
            case R.id.btnTagSearch:
                //지도 화면 띄우기
                intent = new Intent(getActivity(), StartPointActivity.class);
                startActivity(intent);
                break;
            case R.id.btnList:
                /*intent = new Intent(getActivity(), ListActivity.class);
                startActivity(intent);
                */
                getActivity().finish();
                break;
            case R.id.btnrefresh:
                intent = new Intent(getActivity(), EnrollActivity.class);
                intent.putExtra("status","2");
                startActivity(intent);
                Log.d("Sun_Fr1_refresh OK","");
                break;
        }
    }
}
