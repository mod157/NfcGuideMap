package com.guidmap.ss2.nfcguidmap;

/**
 * Created by SunJae on 2016-12-03.
 */

public class ListItem {
    private int iconDrawable ;
    private String titleStr ;
    private String descStr ;

    public void setIcon(int icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setDesc(String desc) {
        descStr = desc ;
    }

    public int getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getDesc() {
        return this.descStr ;
    }
}
