package com.guidmap.ss2.nfcguidmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;

import static net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOff;
import static net.daum.mf.map.api.MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading;

public class PointActivity extends AppCompatActivity implements View.OnClickListener , MapView.MapViewEventListener{
    MapView mapView;
    static final String API_KEY = "00531dfe0f20de8b2af6bc202045da6c";
    Button btnUpload, btnMapName;
    TextView btnback;
    EditText editMapName;
    int serverResponseCode = 0;
    ProgressDialog dialogSet = null;
    String upLoadServerUri = null;
    AlertDialog.Builder alert_confirm;
    String absolutePath = "",tagID = "", mapName = "", startPoint = "1";
    ImageView imageView;
    PhotoViewAttacher attacher;
    JSONObject json;
    LinearLayout mapLinear;
    MapMarker mapmaker;
    Handler h;
    float old_Pos = -1f;
    float new_Pos = -1f;
    int xDelta, yDelta;
    float size;
    int iconList = R.drawable.starting_point;
    boolean lock = false;
    LinearLayout mapViewContainer;
    ArrayList<LinearLayout> array = IconClass.get_Icon();
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
        setContentView(R.layout.activity_point);
        mapLinear = (LinearLayout) findViewById(R.id.MapName);
        btnUpload = (Button) findViewById(R.id.btnupload);
        btnback = (TextView) findViewById(R.id.btnPointback);
        btnMapName = (Button) findViewById(R.id.btnmapName);
        editMapName = (EditText) findViewById(R.id.editMapName);
        imageView = (ImageView) findViewById(R.id.imagePointImage);
        attacher = new PhotoViewAttacher(imageView);
        upLoadServerUri = "http://218.209.45.76:3000/api/photo";
        mapSet();
        Intent getintent = getIntent();
        try {
            json = new JSONObject(getintent.getStringExtra("TagData"));
            tagID = json.getString("TagID");
            size = Float.parseFloat(json.getString("Size"));
            absolutePath = json.getString("TagPath");
            Log.v("SunSet", json.toString() + "\n"+tagID + " : " + absolutePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Bitmap selPhoto = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(absolutePath)));
            imageView.setImageBitmap(selPhoto);
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("Sun_TagData",tagID + " : " + absolutePath);
        btnUpload.setOnClickListener(this);
        btnback.setOnClickListener(this);
        btnMapName.setOnClickListener(this);
        contentIcon();
    }
    private void mapSet(){
        mapView = new MapView(this);
        mapView.setDaumMapApiKey(API_KEY);
        mapView.setMapViewEventListener(this);
        MapView.setMapTilePersistentCacheEnabled(true);
        mapViewContainer = (LinearLayout) findViewById(R.id.CustomStartPointMap);
        mapViewContainer.addView(mapView);
    }
    private void contentIcon(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout linear = (LinearLayout) inflater.inflate(R.layout.addiconview, null);
        linear.setBackgroundResource(R.drawable.baselayout_w_blow);
        ((ImageView) linear.findViewById(R.id.iconimage)).setImageResource(iconList);
        ((ImageView) linear.findViewById(R.id.iconimage)).setScaleX((float) (((ImageView) linear.findViewById(R.id.iconimage)).getScaleX()*(size+0.1)));
        ((ImageView) linear.findViewById(R.id.iconimage)).setScaleY((float) (((ImageView) linear.findViewById(R.id.iconimage)).getScaleY()*(size+0.1)));

        //   Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconList[iconblock]);
        //   linear.setImageBitmap(bitmap);
        LinearLayout.LayoutParams paramslinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        linear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        linear.setBackgroundResource(R.drawable.baselayout_w_blow);
                        xDelta = (int) (X - linear.getTranslationX());
                        yDelta = (int) (Y - linear.getTranslationY());

                        // yDelta = (int) (Y - linear.getTranslationY());
                        break;
                    case MotionEvent.ACTION_UP:
                        old_Pos = -1f;
                        new_Pos = -1f;
                        linear.setBackgroundResource(R.drawable.baselayout_w_nop);
                        Log.v("SunMove : ",linear.getTranslationX() + " : " + linear.getTranslationY());
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        float x = event.getX(0) - event.getX(1);
                        float y = event.getY(0) - event.getY(1);
                        old_Pos = (float)Math.sqrt(x*x + y*y);
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        old_Pos = -1f;
                        new_Pos = -1f;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(event.getPointerCount() >1){
                            float x_ = event.getX(0) - event.getX(1);
                            float y_ = event.getY(0) - event.getY(1);
                            new_Pos = (float)Math.sqrt(x_ * x_ + y_ * y_);
                            float scale = new_Pos / old_Pos;
                            Log.v("scale1",scale+" : x,y : " + linear.getScaleX() + " : " + linear.getScaleY());
                            Log.v("scale1-1",scale+" : x,y : " + linear.getScaleX()*scale + " : " + linear.getScaleY()*scale);
                            linear.setScaleX(scale);
                            linear.setScaleY(scale);
                        }else {
                            linear.setTranslationX(X - xDelta);
                            linear.setTranslationY(Y - yDelta);
                        }
                        break;

                }
                return true;
            }
        });
        addContentView(linear,paramslinear);
        array.add(linear);
        //    addContentView(image, paramslinear);
    }

    public void AlertDialog(String str){
        alert_confirm = new AlertDialog.Builder(this);
        TextView messageText = new TextView(this);
        TextView title = new TextView(this);
        title.setText("태그 완료");
        title.setTextColor(Color.parseColor("#000000"));
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        // listView.setDivider(new ColorDrawable(Color.GRAY)); // set color
        // listView.setDividerHeight(1); // set height
        // alert_confirm.setCustomTitle(title);
        messageText.setText(str);
        messageText.setGravity(Gravity.CENTER);
        messageText.setPadding(10,50,10,0);
        messageText.setTextSize(20);
        messageText.setTextColor(Color.parseColor("#000000"));
        alert_confirm.setView(messageText);
    }

    public int uploadFile(String sourceFileUri) {
        final String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            dialogSet.dismiss();
            Log.e("uploadFile", "Source File not exist :"
                    + fileName);
/*
            runOnUiThread(new Runnable() {
                public void run() {
                    //messageText.setText("Source File not exist :"
                          //  + fileName );
                }
            });
*/
            return 0;

        } else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                //	conn.setRequestProperty("TagId", tagID);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()),8192);
                    final StringBuilder response = new StringBuilder();
                    String strLine = null;
                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                        Log.v("Tag",strLine);
                    }
                    input.close();

                    runOnUiThread(new Runnable() {
                        public void run() {

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    + response.toString();

                            //	messageText.setText(msg);
                            Toast.makeText(PointActivity.this, "업로드가 완료 되었습니다.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialogSet.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(PointActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialogSet.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(PointActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file Exception", "Exception : " + e.getMessage(), e);
            }
            dialogSet.dismiss();
            return serverResponseCode;

        } // End else block
    }
    private void sendMessage(String name){
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        String message = mapmaker.latitude+" : " + mapmaker.longitude;
        try {
            //icon번호, x,y 좌표,
            json.put("mapName",mapName);
            json.put("startPoint", message);
            json.put("Size", size);
            json.put("width", width);
            json.put("height", height);
            json.put("FileName","");
            socket.emit(name, json);
        } catch (JSONException e) {
            Log.v("socket error : ", e.toString());
        }
    }
    private void pointXY(int icon, float X, float Y){
        Log.d("SunpointXY","OK");
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("TagID",tagID);
            jsonObject.put("IconNumber",icon+"");
            jsonObject.put("Context","");
            jsonObject.put("Title","");
            jsonObject.put("locationX",X+"");
            jsonObject.put("locationY",Y+"");
            socket.emit("PointXY", jsonObject);
        }catch(JSONException e){
            Log.v("Point socket XY", e.toString());
        }
    }
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPointback:
                finish();
                break;
            case R.id.btnupload:
                if(lock == false) {
                    mapViewContainer.setVisibility(View.VISIBLE);
                    ((ViewManager) array.get(array.size()-1).getParent()).removeView(array.get(array.size()-1));
                    break;
                }
                if(mapName.equals("") && mapLinear.getVisibility() == View.GONE && lock == true) {
                    mapLinear.setVisibility(View.VISIBLE);
                    break;
                }
                if(mapName.equals("")){
                    Toast.makeText(PointActivity.this,"맵 이름을 정해주십시오.",Toast.LENGTH_SHORT);
                }
                Log.d("SunSetImage","fileUPload");

                break;
            case R.id.btnmapName:
                if(!editMapName.getText().toString().equals("")) {
                    mapName = editMapName.getText().toString();
                    mapLinear.setVisibility(View.GONE);
                    AlertDialog("저장 하시겠습니까?");
                    Log.d("SunSetImage","Start");
                    alert_confirm.setCancelable(true).setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'No'
                                    return;
                                }
                            }).setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'YES'
                                    //파일 업로드 시작!
                                    dialogSet = ProgressDialog.show(PointActivity.this, "", "업로드 중...", true);
                                    new Thread(new Runnable() {
                                        public void run() {
                                            uploadFile(absolutePath);
                                            sendMessage("DataInput");
                                            for(int i = 0; i<array.size(); i++){
                                                pointXY(array.get(i).getId()%100,array.get(i).getTranslationX(),array.get(i).getTranslationY());
                                                Log.v("SunPoint","point icon : "+ array.get(i).getId()%100+","+array.get(i).getTranslationX()+","+array.get(i).getTranslationY());
                                            }
                                            alert_confirm.setCancelable(true).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(PointActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    startActivity(intent);
                                                }
                                            }).setNegativeButton("예", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    return;
                                                }
                                            });
                                            Intent intent = new Intent(PointActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            startActivity(intent);
                                        }
                                    }).start();
                                }
                            });

                    AlertDialog alert = alert_confirm.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
                    alert.setCanceledOnTouchOutside(true);
                    alert.show();
                }else
                    Toast.makeText(PointActivity.this,"맵 이름을 정해주십시오.",Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        mapView.setCurrentLocationTrackingMode(TrackingModeOnWithoutHeading);
        mapView.setShowCurrentLocationMarker(false);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        mapView.setCurrentLocationTrackingMode(TrackingModeOff);
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        if (lock == false) {
                // map.position();
                mapmaker = new MapMarker();
                mapmaker.creatMaker(mapView, mapPoint, R.drawable.starting_point, "","");
                Log.v("Map.Single", mapPoint.getMapPointGeoCoord().latitude + " / " + mapPoint.getMapPointGeoCoord().longitude);
                lock = true;

        } else
           mapmaker.position(MapPoint.mapPointWithGeoCoord(mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude));

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
}
