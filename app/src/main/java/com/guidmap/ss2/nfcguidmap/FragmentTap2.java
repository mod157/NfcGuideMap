
package com.guidmap.ss2.nfcguidmap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by SunJae on 2016-10-30.
 */

public class FragmentTap2 extends Fragment implements View.OnClickListener{
    TextView textSet;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragmenttap2, container, false );
        initSet(view);
        return view;
    }

    private void initSet(View view){
        textSet = (TextView) view.findViewById(R.id.textSet);
        textSet.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.textSet :
                break;

        }
    }
}
