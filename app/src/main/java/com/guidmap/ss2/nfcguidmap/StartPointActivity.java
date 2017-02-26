package com.guidmap.ss2.nfcguidmap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import static net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOff;
import static net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading;

public class StartPointActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.CurrentLocationEventListener{
    MapView mapView;
    static final String API_KEY = "00531dfe0f20de8b2af6bc202045da6c";
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
        setContentView(R.layout.activity_start_point);
        mapView = new MapView(this);
        mapView.setDaumMapApiKey(API_KEY);
        mapView.setMapViewEventListener(this);
        MapView.setMapTilePersistentCacheEnabled(true);
        LinearLayout mapViewContainer = (LinearLayout) findViewById(R.id.startPointLayout);
        mapViewContainer.addView(mapView);
        mapView.setCurrentLocationEventListener(this);
        socket.connect();
        socket.on("FindTag",listen);
    }
    private Emitter.Listener listen = new Emitter.Listener() {
        public void call(Object... args) {
            double startx, starty;
            final JSONObject obj = (JSONObject)args[0];
            //서버에서 보낸 JSON객체를 사용할 수 있습니다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v("Sun", "listen OK\n"+obj);
                    try {

                        String[] txtArr = obj.getString("StartPoint").split(" : ") ;
                        double startX = Double.parseDouble(txtArr[0]);
                        double startY = Double.parseDouble(txtArr[1]);
                        MapMarker mapMarker = new MapMarker();
                        mapMarker.creatMaker(mapView,startX,startY,R.drawable.starting_point,"","");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    @Override
    public void onMapViewInitialized(MapView mapView) {
        socket.emit("FindTag","1");

        mapView.setCurrentLocationTrackingMode(TrackingModeOnWithoutHeading);
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
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        Log.v("Down","Map");
        mapView.setMapCenterPointAndZoomLevel(mapPoint,5,true);
        mapView.setCurrentLocationTrackingMode(TrackingModeOff);
        mapView.setShowCurrentLocationMarker(false);
        Log.v("Show",mapView.isShowingCurrentLocationMarker()+"");
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }
}
