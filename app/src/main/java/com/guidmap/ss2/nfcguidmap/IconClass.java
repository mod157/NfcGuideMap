package com.guidmap.ss2.nfcguidmap;

import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by SunJae on 2016-12-03.
 */

public class IconClass {
    static ArrayList<LinearLayout> array = new ArrayList<LinearLayout>();
    public static void set_Icon(ArrayList arraylist){
        array = arraylist;
    }

    public static ArrayList get_Icon(){
        return array;
    }
}
