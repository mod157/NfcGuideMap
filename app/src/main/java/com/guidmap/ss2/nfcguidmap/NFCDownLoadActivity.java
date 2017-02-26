package com.guidmap.ss2.nfcguidmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import static net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOff;
import static net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading;


/**
 * Created by SunJae on 2016-11-14.
 */

public class NFCDownLoadActivity extends AppCompatActivity implements MapView.MapViewEventListener, View.OnClickListener{
    int[] iconList = new int[]{R.drawable.starting_point,R.drawable.point,R.drawable.icon_1,R.drawable.icon_2,R.drawable.icon_3,R.drawable.icon_4,R.drawable.icon_5,R.drawable.icon_6,R.drawable.icon_7,
            R.drawable.icon_8,R.drawable.icon_9,R.drawable.icon_10,R.drawable.icon_12,R.drawable.icon_13,R.drawable.icon_14,R.drawable.icon_15};
    int[] iconList_small = new int[]{R.drawable.point_small,R.drawable.icon_1_small,R.drawable.icon_2_small,R.drawable.icon_3_small,R.drawable.icon_4_small,R.drawable.icon_5_small,R.drawable.icon_6_small,R.drawable.icon_7_small,
            R.drawable.icon_8_small,R.drawable.icon_9_small,R.drawable.icon_10_small,R.drawable.icon_12_small,R.drawable.icon_13_small,R.drawable.icon_14_small,R.drawable.icon_15_small,R.drawable.starting_point};
        ImageView mImageView;
        Bitmap mBmp = null;
        String fileName = "";
    int[] iconNumber;
    int width, height;
    float iconSize = 0.35f;
    int layout_x, layout_y;
    boolean trakingLock= false;
    GestureDetector gestureDetector;
    String startPoint;
    JSONObject json;
    ArrayList<MapMarker> mapmarkers = new ArrayList<MapMarker>();
    MapView mapView;
    String title, detail;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    Button btniconS, btnlocationS;
    static final String API_KEY = "00531dfe0f20de8b2af6bc202045da6c";
    ArrayList<LinearLayout> icons;
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
            setContentView(R.layout.activty_nfcdown);
            mImageView = (ImageView)findViewById(R.id.imageDownImage);
            btniconS = (Button) findViewById(R.id.btnIconSelect);
            btnlocationS = (Button) findViewById(R.id.btnlocationSelect);
            btnlocationS.setOnClickListener(this);
            btniconS.setOnClickListener(this);
            DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
            int new_width = dm.widthPixels;
            int new_height = dm.heightPixels;
            Intent getintent = getIntent();
            //attacher = new PhotoViewAttacher(mImageView);
            fileName = getintent.getStringExtra("TagData_FileName");
            iconSize = Float.parseFloat(getintent.getStringExtra("TagData_Size"));
            width = Integer.parseInt(getintent.getStringExtra("TagData_width"));
            height = Integer.parseInt(getintent.getStringExtra("TagData_height"));
            startPoint = getintent.getStringExtra("TagData_StartPoint");
            layout_x = (int)((new_width-width)/2);
            layout_y = (int)((new_height-height)/2);
            String addr = "http://218.209.45.76/imageup/uploads/"+fileName;
            Log.v("SunDown_dd", getintent.getStringExtra("TagData_FileName") + " : " + addr);
            // 서버에서 이미지 다운로드를 수행하는 쓰레드
            new HttpReqTask().execute(addr);

                json = new JSONObject();
                try {
                    json.put("TagID", getintent.getStringExtra("TagData_TagID").toString());
                    if(!fileName.equals("Map")) {
                        socket.emit("SetIcon", json);
                        btnlocationS.setVisibility(View.GONE);
                    }else {
                        Log.v("MapCr","OK");
                        mapView = new MapView(this);
                        mapView.setDaumMapApiKey(API_KEY);
                        mapView.setMapViewEventListener(this);
                        MapView.setMapTilePersistentCacheEnabled(true);
                        mapView.setCalloutBalloonAdapter(new NFCDownLoadActivity.CustomCalloutBalloonAdapter());
                        LinearLayout mapViewContainer = (LinearLayout) findViewById(R.id.frameMapView);
                        mapViewContainer.addView(mapView);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            socket.on("SetIcon",listen_tag);
            RecyclerSet();
            icons = new ArrayList<LinearLayout>();
        }
    private void ItemCreater(ArrayList<Item> items, int icon){
        Item item = new Item();
        item.setImage(icon);
        items.add(item);
    }
    private void RecyclerSet(){
        gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.downrecyclerView);

        ArrayList<Item> items = new ArrayList<>();
        ItemCreater(items,R.drawable.refresh_button);
        for(int i = 0; i<iconList.length; i++) {
            ItemCreater(items, iconList[i]);
        }

        // StaggeredGrid 레이아웃을 사용한다
        layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
        //layoutManager = new LinearLayoutManager(this);
        //layoutManager = new GridLayoutManager(this,3);

        // 지정된 레이아웃매니저를 RecyclerView에 Set 해주어야한다.
        recyclerView.setAdapter(new RecyclerViewAdapter(items,R.layout.item_view));
        recyclerView.setLayoutManager(new LinearLayoutManager(NFCDownLoadActivity.this, LinearLayoutManager.HORIZONTAL, false));
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
                    int iconNum = rv.getChildLayoutPosition(child);
                    if(iconNum == 0){
                        refresh_icon();
                        return true;
                    }
                    iconNum -= 2;
                    Log.v("remove",iconNum+"");
                    if(iconNum == -1)
                        iconNum = 15;
                    if(fileName.equals("Map")) {
                        refresh_icon();
                        for (int i = 0; i < mapmarkers.size(); i++) {
                           // Log.v("Mapremove",i + ": "+ mapmarkers.get(i).icon + ", " + iconList_small[iconNum]+ (mapmarkers.get(i).icon == iconList_small[iconNum]));
                            if (mapmarkers.get(i).icon != iconList_small[iconNum]) {
                               // Log.v("remove", i + "");
                                mapView.removePOIItem(mapmarkers.get(i).marker2);
                            }
                        }
                    }else {
                        //커스텀일 때

                    }
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
    private void refresh_icon(){
        if(fileName.equals("Map")) {
            mapView.removeAllPOIItems();
            for (int i = 0; i < mapmarkers.size(); i++) {
                mapView.addPOIItem(mapmarkers.get(i).marker2);
            }
        }else{
            for (int i = 0; i < icons.size(); i++) {
                ((ViewManager) icons.get(i).getParent()).removeView(icons.get(i));
            }
        }
    }
    private Emitter.Listener listen_tag = new Emitter.Listener() {
        public void call(Object... args) {
            final JSONObject obj = (JSONObject)args[0];
            //서버에서 보낸 JSON객체를 사용할 수 있습니다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v("Sun", fileName+"  listen OK\n"+obj);
                    try {
                        int icon = Integer.parseInt(obj.get("MarkerNum").toString());
                         title = obj.get("Title").toString();
                         detail = obj.get("Detail").toString();
                        if(!fileName.equals("Map")) {
                            float x = Float.parseFloat(obj.get("locationX").toString());
                            float y = Float.parseFloat(obj.get("locationY").toString());
                            setLinear(icon, x, y, title, detail);
                        }else {
                            String[] txtArr = startPoint.split(" : ") ;
                            double startX = Double.parseDouble(txtArr[0]);
                            double startY = Double.parseDouble(txtArr[1]);
                            Log.v("SunDownMap"+startPoint,startX + " : " + startY +"    "+  iconSize);
                            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(startX, startY),(int)iconSize,true);
                            double x = Double.parseDouble(obj.get("locationX").toString());
                            double y = Double.parseDouble(obj.get("locationY").toString());
                            try {
                                SetMap(icon, x, y, title, detail);
                            }catch(Exception e){

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private void SetMap(int icon, double x, double y, String title, String detail){
        MapMarker mapmaker = new MapMarker();
        mapmaker.creatMaker(mapView, x, y, iconList_small[icon], title, detail);
        Log.v("SunDown",title+" " + detail);
        mapmaker.SetTag(mapmarkers.size());
        mapmarkers.add(mapmaker);
    }
    private void setLinear(int icon, float x, float y,String title, String detail){
        icon++;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout linear = (LinearLayout) inflater.inflate(R.layout.addiconview, null);
        linear.setBackgroundResource(R.drawable.baselayout_w_nop);
        ((ImageView) linear.findViewById(R.id.iconimage)).setImageResource(iconList[icon]);

        if(icon > 1) {
            ((FrameLayout) linear.findViewById(R.id.iconbackground)).setBackgroundResource(R.drawable.baselayout_w);
            ((FrameLayout) linear.findViewById(R.id.iconbackground)).setScaleX(((FrameLayout) linear.findViewById(R.id.iconbackground)).getScaleX()*iconSize);
            ((FrameLayout) linear.findViewById(R.id.iconbackground)).setScaleY(((FrameLayout) linear.findViewById(R.id.iconbackground)).getScaleY()*iconSize);
        }else{
            ((TextView) linear.findViewById(R.id.title)).setText(title);
            ((TextView) linear.findViewById(R.id.detail)).setText(detail);
            ((ImageView) linear.findViewById(R.id.iconimage)).setScaleX((float) (((ImageView) linear.findViewById(R.id.iconimage)).getScaleX()*(iconSize+0.1)));
            ((ImageView) linear.findViewById(R.id.iconimage)).setScaleY((float) (((ImageView) linear.findViewById(R.id.iconimage)).getScaleY()*(iconSize+0.1)));
        }
        LinearLayout.LayoutParams paramslinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        linear.setTranslationX(x+layout_x);
        linear.setTranslationY(y+layout_y);
        Log.v("SunDown", x+":"+y+"\n"+x*layout_x + " : " + y*layout_y);
        addContentView(linear, paramslinear);
       // icons.add(linear);
    }

        // 서버에서 전달 받은 데이터를 Bitmap 이미지에 저장
        public boolean loadWebImage(String strUrl) {
            try {
                // 스트림 데이터를 Bitmap 에 저장
                InputStream is = new URL(strUrl).openStream();
                mBmp = BitmapFactory.decodeStream(is);
                is.close();
            } catch(Exception e) {
                Log.d("tag", "Image Stream error.");
                return false;
            }
            return true;
        }

        // 1번째 버튼의 이벤트 함수
        public void onBtnLoad1() {
            String addr = "http://218.209.45.76/imageup/uploads/"+".png";
            // 서버에서 이미지 다운로드를 수행하는 쓰레드
            new HttpReqTask().execute(addr);
        }

        // 2번째 버튼의 이벤트 함수
        public void onBtnParse2() {
            String addr ="http://218.209.45.76/imageup/uploads/1479052573014.png";
            String fileName = "download.jpg";
            // 서버에서 이미지 다운로드를 수행하는 쓰레드
            new HttpReqTask().execute(addr, fileName);
        }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        socket.emit("SetIcon", json);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnIconSelect:
                if(recyclerView.getVisibility() == View.GONE)
                    recyclerView.setVisibility(View.VISIBLE);
                else
                    recyclerView.setVisibility(View.GONE);
                break;
            case R.id.btnlocationSelect:
                if(trakingLock == true){
                    mapView.setCurrentLocationTrackingMode(TrackingModeOff);
                    mapView.setShowCurrentLocationMarker(false);
                    trakingLock = false;
                }else{
                    mapView.setCurrentLocationTrackingMode(TrackingModeOnWithoutHeading);
                    trakingLock = true;
                }

                break;
        }
    }


    // 서버에서 이미지 다운로드를 수행하는 쓰레드
        private class HttpReqTask extends AsyncTask<String,String,String> {
            @Override // 쓰레드 주업무를 수행하는 함수
            protected String doInBackground(String... arg) {
                boolean result = false;
                if( arg.length == 1 )
                    // 서버에서 전달 받은 데이터를 Bitmap 이미지에 저장
                    result = loadWebImage(arg[0]);
                else {
                    // 서버에서 다운로드 한 데이터를 파일로 저장
                    result = downloadFile(arg[0], arg[1]);
                    if( result ) {
                        // 파일을 로딩해서 Bitmap 객체로 생성
                        String sdRootPath = Environment.getDataDirectory().getAbsolutePath();
                        String filePath = sdRootPath + "/" + arg[1];
                        mBmp = BitmapFactory.decodeFile("/0/"+arg[1]);
                    }
                }

                if( result )
                    return "True";
                return "";
            }

            // 쓰레드의 업무가 끝났을 때 결과를 처리하는 함수
            protected void onPostExecute(String result) {
                if( result.length() > 0 )
                    // 서버에서 다운받은 Bitmap 이미지를 ImageView 에 표시
                    mImageView.setImageBitmap(mBmp);
            }
        }

        // 서버에서 다운로드 한 데이터를 파일로 저장
        boolean downloadFile(String strUrl, String fileName) {
            try {
                URL url = new URL(strUrl);
                // 서버와 접속하는 클라이언트 객체 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // 입력 스트림을 구한다
                InputStream is = conn.getInputStream();
                // 파일 저장 스트림을 생성
                FileOutputStream fos = openFileOutput(fileName, 0);

                // 입력 스트림을 파일로 저장
                byte[] buf = new byte[1024];
                int count;
                while( (count = is.read(buf)) > 0 ) {
                    fos.write(buf, 0, count);
                }
                // 접속 해제
                conn.disconnect();
                // 파일을 닫는다
                fos.close();
            } catch (Exception e) {
                Log.d("tag", "Image download error.");
                return false;
            }
            return true;
        }
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            int i;
            for(i = 0; i< mapmarkers.size(); i++){
                if(mapmarkers.get(i).GetTag() == poiItem.getTag() )
                    break;
            }
            Log.v("LogBallon[",i+"]###########"+mapmarkers.get(i));
            ((TextView) mCalloutBalloon.findViewById(R.id.textMapTitle)).setText(mapmarkers.get(i).title);
            ((TextView) mCalloutBalloon.findViewById(R.id.textMapDetail)).setText(mapmarkers.get(i).detail);
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }
    }
