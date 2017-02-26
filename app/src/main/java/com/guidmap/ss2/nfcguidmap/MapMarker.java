package com.guidmap.ss2.nfcguidmap;

import android.util.Log;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

/**
 * Created by Dong on 2016-11-15.
 */

public class MapMarker {
    MapPOIItem marker2= new MapPOIItem();
    double latitude, longitude;
    String title, detail;
    int icon;
    public void creatMaker(MapView mapView,double x, double y,int icon,String title, String detail){
        latitude = x;
        longitude = y;
        this.icon = icon;
        this.title = title;
        this.detail = detail;
        addMarker(mapView,icon);
    }
    public void creatMaker(MapView mapView,MapPoint mapPoint,int icon,String title,String detail){
        Log.v("SunMarker",icon +"," + title);
        latitude = mapPoint.getMapPointGeoCoord().latitude;
        longitude = mapPoint.getMapPointGeoCoord().longitude;
        this.icon = icon;
        this.title = title;
        this.detail = detail;
   // marker2.setTag(1);
        addMarker(mapView,icon);

    //    latitude=mapPoint.getMapPointGeoCoord().latitude;
      //  longtitude=mapPoint.getMapPointGeoCoord().longitude;
    }
    public  void addMarker(MapView mapView,int icon){
        Log.v("icon",icon+"");
        marker2.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
        marker2.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
        marker2.setCustomImageResourceId(icon); // 마커 이미지.
        marker2.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
        marker2.setCustomImageAnchor(0.5f, 1.0f);
        marker2.setItemName(title);
        marker2.setDraggable(true);
        marker2.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker2.setCustomSelectedImageResourceId(icon);// 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        //2130837679
        if(icon == 2130837679)
            marker2.setShowCalloutBalloonOnTouch(true);
        else
            marker2.setShowCalloutBalloonOnTouch(false);
        mapView.addPOIItem(marker2);
    }
    public void SetTag(int num){
        marker2.setTag(num);
    }
    public int GetTag(){
        return marker2.getTag();
    }
    public void position(MapPoint mapPoint){

        marker2.setMapPoint(MapPoint.mapPointWithGeoCoord(mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude));
        latitude = mapPoint.getMapPointGeoCoord().latitude;
        longitude = mapPoint.getMapPointGeoCoord().longitude;

        //marker2.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude,longtitude));
        //mapView.addPOIItem(marker2);
    }





}
