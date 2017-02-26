package com.guidmap.ss2.nfcguidmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import static net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOff;
import static net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading;

public class MapActivity extends AppCompatActivity implements MapView.MapViewEventListener, View.OnClickListener, MapView.POIItemEventListener, MapView.CurrentLocationEventListener{
    int[] iconList = new int[]{R.drawable.point,R.drawable.icon_1,R.drawable.icon_2,R.drawable.icon_3,R.drawable.icon_4,R.drawable.icon_5,R.drawable.icon_6,R.drawable.icon_7,
            R.drawable.icon_8,R.drawable.icon_9,R.drawable.icon_10,R.drawable.icon_12,R.drawable.icon_13,R.drawable.icon_14,R.drawable.icon_15,R.drawable.starting_point};
    int[] iconList_small = new int[]{R.drawable.point_small,R.drawable.icon_1_small,R.drawable.icon_2_small,R.drawable.icon_3_small,R.drawable.icon_4_small,R.drawable.icon_5_small,R.drawable.icon_6_small,R.drawable.icon_7_small,
            R.drawable.icon_8_small,R.drawable.icon_9_small,R.drawable.icon_10_small,R.drawable.icon_12_small,R.drawable.icon_13_small,R.drawable.icon_14_small,R.drawable.icon_15_small,R.drawable.starting_point};
    ProgressDialog dialogSet = null;
    LocationManager mLM;
    double latitude, longitude;
    EditText editMapTitle, editMapDetail,editMapName;
    GestureDetector gestureDetector;
    static final String API_KEY = "00531dfe0f20de8b2af6bc202045da6c";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private MapPoint.GeoCoordinate mapPointGeo;    // 현위치를 받을 point 객체
    ArrayList<MapMarker> mapmarkers = new ArrayList<MapMarker>();
    MapView mapView;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    //MapMarker map1=new MapMarker();
    int num = 0, i = 0;
    MapPOIItem marker = new MapPOIItem();
    Button btnMapIcon, btnMapDel, btnMapOK,btnStr, btnMapNameOK;
    int iconNum = 15;
    String title ="";
    String detail = "";
    LinearLayout strL;
    Boolean lock=true;
    Boolean delLock = false;
    LinearLayout mapNameLayout;
    JSONObject json;

    private Socket socket;
    {
        try {
            socket = IO.socket("http://218.209.45.76:3000");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // MapView 객체생성 및 API Key 설정
       mapView = new MapView(this);
        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
       // registerLocationUpdates();
        mapView.setDaumMapApiKey(API_KEY);
        mapView.setMapViewEventListener(this);
        MapView.setMapTilePersistentCacheEnabled(true);
        RelativeLayout mapViewContainer = (RelativeLayout) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapNameLayout = (LinearLayout) findViewById(R.id.MapNameLayout);
        editMapName = (EditText) findViewById(R.id.editMapName);
        editMapDetail = (EditText) findViewById(R.id.editMapDetail);
        editMapTitle = (EditText)findViewById(R.id.editMapTitle);
        strL = (LinearLayout) findViewById(R.id.StringLayout);
        btnMapNameOK = (Button) findViewById(R.id.btnSocketOK);
        btnMapDel = (Button) findViewById(R.id.btnMapDel);
        btnMapIcon = (Button) findViewById(R.id.btnMapIcon);
        btnMapOK = (Button) findViewById(R.id.btnMapSOK);
        btnStr = (Button) findViewById(R.id.btnStr);
        btnStr.setOnClickListener(this);
        btnMapNameOK.setOnClickListener(this);
        btnMapDel.setOnClickListener(this);
        btnMapOK.setOnClickListener(this);
        btnMapIcon.setOnClickListener(this);
        mapView.setPOIItemEventListener(this);
        gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mapView.setCurrentLocationEventListener(this);
        //mapView.setMapViewEventListener(this);

        dialogSet = ProgressDialog.show(MapActivity.this, "", "잠시만 기다려 주세요.", true);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        RecyclerSet();
        Intent getintent = getIntent();
        try {
            json = new JSONObject(getintent.getStringExtra("TagData"));
        }catch(JSONException e){

        }

    }
/*
    private void registerLocationUpdates() {
        mLM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                100, 1, mLocationListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                100, 1, mLocationListener);
        // mLM.removeUpdates(mLocationListener);
//1000은 1초마다, 1은 1미터마다 해당 값을 갱신한다는 뜻으로, 딜레이마다 호출하기도 하지만
//위치값을 판별하여 일정 미터단위 움직임이 발생 했을 때에도 리스너를 호출 할 수 있다.
    }

    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

//여기서 위치값이 갱신되면 이벤트가 발생한다.
//값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
//Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                longitude = location.getLongitude();    //경도
                latitude = location.getLatitude();         //위도
                Log.v("SunTude", longitude + ":" + latitude);
               // float accuracy = location.getAccuracy();        //신뢰도
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true);
                mLM.removeUpdates(mLocationListener);
                 dialogSet.dismiss();
            } else {
//Network 위치제공자에 의한 위치변화
//Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            }

            if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Log.v("ddd","ddd");

        }
        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };*/
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
        dialogSet.dismiss();
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        mapView.setCurrentLocationTrackingMode(TrackingModeOnWithoutHeading);

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        Log.v("CenterPoint","OK");

       // dialogSet.dismiss();
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        Log.v("SunMap","OK");
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
            if (num == 0) {
                if(lock == true) {

                    // map.position();
                    MapMarker mapmaker = new MapMarker();
                    mapmaker.creatMaker(mapView, mapPoint, iconList_small[iconNum], title, detail);
                    mapmaker.SetTag(num*100+iconNum);
                    mapmarkers.add(mapmaker);
                    Log.v("Map.Single", mapmaker.marker2.getTag() + " : " + mapPoint.getMapPointGeoCoord().latitude + " / " + mapPoint.getMapPointGeoCoord().longitude);
                    i++;
                    num++;
                }
            } else
                mapmarkers.get(i - 1).position(MapPoint.mapPointWithGeoCoord(mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude));

    }
    private void ItemCreater(ArrayList<Item> items, int icon){
        Item item = new Item();
        item.setImage(icon);
        items.add(item);
    }

    private void RecyclerSet(){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerMapView);

        ArrayList<Item> items = new ArrayList<>();
        for(int i = 0; i<iconList.length-1; i++) {
            ItemCreater(items, iconList[i]);
        }

        // StaggeredGrid 레이아웃을 사용한다
        layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
        //layoutManager = new LinearLayoutManager(this);
        //layoutManager = new GridLayoutManager(this,3);

        // 지정된 레이아웃매니저를 RecyclerView에 Set 해주어야한다.
        recyclerView.setAdapter(new RecyclerViewAdapter(items,R.layout.item_view));
        recyclerView.setLayoutManager(new LinearLayoutManager(MapActivity.this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e)
            {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if(child!=null&&gestureDetector.onTouchEvent(e)) {
                    Log.d("SunSet","onInterceptTouchEvent");
                    Log.d("SunSet","AdapterPosition=>"+rv.findViewHolderForAdapterPosition(rv.getChildLayoutPosition(child)));
                    Log.d("SunSet","LayoutPosition=>"+rv.findViewHolderForLayoutPosition(rv.getChildLayoutPosition(child)));
                    Log.d("SunSet", "getChildViewHolder=>" + rv.getChildViewHolder(child).itemView);
                    Log.d("SunSet","postion : "+ rv.getChildLayoutPosition(child));
                    iconNum = rv.getChildLayoutPosition(child);
                    if(iconNum == 0)
                        strL.setVisibility(View.VISIBLE);
                    num=0;
                    lock = true;
                }
                return false;
            }
            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e)
            {
                Log.d("SunSet","onTouchEvent");

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
            {
                Log.d("SunSet","onRequestDisallowInterceptTouchEvent");
            }
        });
    }
    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        Log.v("Map.DoubleTap",mapmarkers.size()+"");
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        Log.v("Map.Pressed",mapPoint.getMapPointGeoCoord().latitude+"/"+mapPoint.getMapPointGeoCoord().longitude);
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        Log.v("Map.Drag",mapPoint.getMapPointGeoCoord().latitude+"");
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
      //  Log.v("Map.moved",mapPoint.getMapPointGeoCoord()+"");
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    private void sendMessage(String name){
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        String message = mapmarkers.get(0).latitude+" : "+mapmarkers.get(0).longitude;
        try {
            //icon번호, x,y 좌표,
            json.put("FileName","Map");
            json.put("mapName",editMapName.getText().toString());
            json.put("startPoint", message);
            json.put("Size", mapView.getZoomLevel());
            json.put("width", width);
            json.put("height", height);
            socket.emit(name, json);
        } catch (JSONException e) {
            Log.v("socket error : ", e.toString());
        }
    }
    private void pointXY(int icon,double X, double Y, String title, String detail){
        Log.d("SunpointXY","OK");
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("TagID",json.getString("TagID").toString());
            jsonObject.put("IconNumber",icon+"");
            jsonObject.put("Context", detail);
            jsonObject.put("Title",title);
            jsonObject.put("locationX",X+"");
            jsonObject.put("locationY",Y+"");
            socket.emit("PointXY", jsonObject);
        }catch(JSONException e){
            Log.v("Point socket XY", e.toString());
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnMapIcon:
                if (recyclerView.getVisibility() == View.GONE)
                    recyclerView.setVisibility(View.VISIBLE);
                else
                    recyclerView.setVisibility(View.GONE);
                break;
            case R.id.btnMapDel:
                if(delLock == false) {
                    delLock = true;
                }else
                    delLock = false;
                break;
            case R.id.btnMapSOK:
                if(mapNameLayout.getVisibility() == View.GONE)
                    mapNameLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btnSocketOK:
                sendMessage("DataInput");
                //setTag를 이용해서 아이콘 이름 획득, 경 위도;
                for(int i = 0; i<mapmarkers.size(); i++){
                    pointXY(mapmarkers.get(i).GetTag()%100, mapmarkers.get(i).latitude, mapmarkers.get(i).longitude, mapmarkers.get(i).title, mapmarkers.get(i).detail);
                    Log.v("SunMap","icon : "+ mapmarkers.get(i).GetTag()%100+","+ mapmarkers.get(i).latitude+","+ mapmarkers.get(i).longitude);
                }
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.btnStr:
                if(delLock == true)
                    delLock = false;
                title = editMapTitle.getText().toString();
                detail = editMapDetail.getText().toString();
                Log.v("SunStr",title + "," + detail);
                editMapTitle.setText("");
                editMapDetail.setText("");
                strL.setVisibility(View.GONE);
                break;
        }
    }
    @Override
    public void onBackPressed() {
        Log.v("Sunmap", "back");
       if(dialogSet.isShowing()) {
           dialogSet.dismiss();
       }else
           finish();
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        Log.v("SunMap","Select");
        if(delLock == true)
            mapView.removePOIItem(mapPOIItem);
        else
            Log.v("SunMap",mapPOIItem.getItemName());
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        Log.v("SunMap","Touch");
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        Log.v("SunMap","Touch2");
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
        Log.v("SunMap","Move" + mapPoint.getMapPointGeoCoord().latitude +","+ mapPoint.getMapPointGeoCoord().longitude);
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        Log.d("SunSet","update");
        mapView.setMapCenterPointAndZoomLevel(mapPoint,mapView.getZoomLevel(),true);
        mapView.setCurrentLocationTrackingMode(TrackingModeOff);
        mapView.setShowCurrentLocationMarker(false);
        Log.v("Show",mapView.isShowingCurrentLocationMarker()+"");
        dialogSet.dismiss();

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
        Log.d("SunSet","onHend");
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
        Log.d("SunSet","onTouchFail");
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
        Log.d("SunSet","onTouchCan");
    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            ((TextView) mCalloutBalloon.findViewById(R.id.textMapTitle)).setText(title);
            ((TextView) mCalloutBalloon.findViewById(R.id.textMapDetail)).setText(detail);
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

}