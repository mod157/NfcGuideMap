package com.guidmap.ss2.nfcguidmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class SetImageActivity extends AppCompatActivity implements View.OnClickListener{
    int[] iconList = new int[]{R.drawable.point,R.drawable.icon_1,R.drawable.icon_2,R.drawable.icon_3,R.drawable.icon_4,R.drawable.icon_5,R.drawable.icon_6,R.drawable.icon_7,
            R.drawable.icon_8,R.drawable.icon_9,R.drawable.icon_10,R.drawable.icon_12,R.drawable.icon_13,R.drawable.icon_14,R.drawable.icon_15};
    Button btnSetOK, btnSetList, btnSetIcon;
    TextView btnback;
    String upLoadServerUri = null;
    AlertDialog.Builder alert_confirm;
    String absolutePath = "",tagID = "";
    ImageView imageView;
    JSONObject json;
    Handler h;
    RecyclerView recyclerView;
    RecyclerView.Adapter Adapter;
    RecyclerView.LayoutManager layoutManager;
    GestureDetector gestureDetector;
    boolean checkDel = false;
    int iconblock = -1;
    float ISize = 0.5f;
    private Socket socket;
    {
        try {
            socket = IO.socket("http://218.209.45.76:3000");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    FrameLayout iconLayout;
    int iconNum;
    ArrayList<LinearLayout> icons;
    Window win;
    int xDelta, yDelta;
    float old_Pos = -1f;
    float new_Pos = -1f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        win = getWindow();
        win.setContentView(R.layout.activity_set_image);
        btnSetIcon = (Button) findViewById(R.id.btnSetIcon);
        btnSetList = (Button) findViewById(R.id.btnSetList);
        btnSetOK =  (Button) findViewById(R.id.btnSetOK);
        btnback = (TextView) findViewById(R.id.btnback);
        imageView = (ImageView) findViewById(R.id.imageSetImage);
       // attacher = new PhotoViewAttacher(imageView);
        btnSetIcon.setOnClickListener(this);
        btnSetList.setOnClickListener(this);
        btnSetOK.setOnClickListener(this);
        btnback.setOnClickListener(this);
        iconLayout = (FrameLayout) findViewById(R.id.IconLayout);
        upLoadServerUri = "http://218.209.45.76:3000/api/photo";

        RecyclerSet();
        jsonSet();

        h = new Handler();
        gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });


        icons = new ArrayList<LinearLayout>();

    }

    private void jsonSet(){
        Intent getintent = getIntent();
            try {
                json = new JSONObject(getintent.getStringExtra("TagData"));
                tagID = json.getString("TagID");
                absolutePath = json.getString("TagPath");
                Log.v("SunSet", json.toString() + "\n" + tagID + " : " + absolutePath);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                Bitmap selPhoto = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(absolutePath)));
                imageView.setImageBitmap(selPhoto);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v("Sun_TagData", tagID + " : " + absolutePath);
    }

    private void contentIcon(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout linear = (LinearLayout) inflater.inflate(R.layout.addiconview, null);
        linear.setId(iconNum*100+iconblock);
        linear.setBackgroundResource(R.drawable.baselayout_w_blow);
        ((ImageView) linear.findViewById(R.id.iconimage)).setImageResource(iconList[iconblock]);

        if(iconblock !=0) {
            ((FrameLayout) linear.findViewById(R.id.iconbackground)).setBackgroundResource(R.drawable.baselayout_w);
            ((FrameLayout) linear.findViewById(R.id.iconbackground)).setScaleX(((FrameLayout) linear.findViewById(R.id.iconbackground)).getScaleX()*ISize);
            ((FrameLayout) linear.findViewById(R.id.iconbackground)).setScaleY(((FrameLayout) linear.findViewById(R.id.iconbackground)).getScaleY()*ISize);
        }else{
             ((ImageView) linear.findViewById(R.id.iconimage)).setScaleX((float) (((ImageView) linear.findViewById(R.id.iconimage)).getScaleX()*(ISize+0.1)));
             ((ImageView) linear.findViewById(R.id.iconimage)).setScaleY((float) (((ImageView) linear.findViewById(R.id.iconimage)).getScaleY()*(ISize+0.1)));
        }
    //   Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconList[iconblock]);
     //   linear.setImageBitmap(bitmap);
        LinearLayout.LayoutParams paramslinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linear.setBackgroundResource(R.drawable.baselayout_w_blow);
            }
        });
        linear.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if(checkDel == true) {
                            ((ViewManager) icons.get(linear.getId() / 100).getParent()).removeView(icons.get(linear.getId() / 100));
                            icons.remove(linear.getId()/100);
                            break;
                        }
                        linear.setBackgroundResource(R.drawable.baselayout_w_blow);
                        xDelta = (int) (X - icons.get(linear.getId()/100).getTranslationX());
                        yDelta = (int) (Y - icons.get(linear.getId()/100).getTranslationY());

                       // yDelta = (int) (Y - linear.getTranslationY());
                        break;
                    case MotionEvent.ACTION_UP:
                        old_Pos = -1f;
                        new_Pos = -1f;
                        linear.setBackgroundResource(R.drawable.baselayout_w_nop);
                        Log.v("SunMove : " + linear.getId()/100,linear.getTranslationX() + " : " + linear.getTranslationY());
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
                        if(event.getPointerCount() >1 && checkDel == false){
                            float x_ = event.getX(0) - event.getX(1);
                            float y_ = event.getY(0) - event.getY(1);
                            new_Pos = (float)Math.sqrt(x_ * x_ + y_ * y_);
                            float scale = new_Pos / old_Pos;
                            Log.v("scale1",scale+" : x,y : " + linear.getScaleX() + " : " + linear.getScaleY());
                            Log.v("scale1-1",scale+" : x,y : " + linear.getScaleX()*scale + " : " + linear.getScaleY()*scale);
                            linear.setScaleX(scale);
                            linear.setScaleY(scale);
                            Log.v("scale2",scale+" : x,y : " + linear.getScaleX() + " : " + linear.getScaleY());
                        }else {
                            linear.setTranslationX(X - xDelta);
                            linear.setTranslationY(Y - yDelta);
                            Log.v("SunSet Move", linear.getTranslationX()  + " , " + linear.getTranslationY() + "\n" + icons.get(linear.getId()/100).getTranslationX() + " , " + icons.get(linear.getId()/100).getTranslationY());
                        }
                        break;

                }
                return true;
            }
        });
          win.addContentView(linear,paramslinear);
   //    addContentView(image, paramslinear);

        icons.add(linear);
    }

    private void RecyclerSet(){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        ArrayList<Item> items = new ArrayList<>();
        for(int i = 0; i<iconList.length; i++) {
            ItemCreater(items, iconList[i]);
        }

        // StaggeredGrid 레이아웃을 사용한다
        layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
        //layoutManager = new LinearLayoutManager(this);
        //layoutManager = new GridLayoutManager(this,3);

        // 지정된 레이아웃매니저를 RecyclerView에 Set 해주어야한다.
        recyclerView.setAdapter(new RecyclerViewAdapter(items,R.layout.item_view));
        recyclerView.setLayoutManager(new LinearLayoutManager(SetImageActivity.this, LinearLayoutManager.HORIZONTAL, false));
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
                    iconblock = rv.getChildLayoutPosition(child);
                    if(iconblock != -1) {
                        Log.d("SunSetRecy",e.getRawX()+","+e.getRawY());
                        contentIcon();
                        iconNum++;
                        iconblock = -1;
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



    private void ItemCreater(ArrayList<Item> items, int icon){
        Item item = new Item();
        item.setImage(icon);
        items.add(item);
    }
    public void AlertDialog_Set(String str) {
        alert_confirm = new AlertDialog.Builder(this);
        TextView messageText = new TextView(this);
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

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.btnSetOK:
                IconClass.set_Icon(icons);
                for (int i = 0; i < icons.size(); i++) {
                    try {
                        ((ViewManager) icons.get(i).getParent()).removeView(icons.get(i));
                    }catch(Exception e){

                    }
                }
                Intent intent = new Intent(SetImageActivity.this, PointActivity.class);
                try{
                    json.put("Size",ISize+"");
                }catch(JSONException e) {

                }
                intent.putExtra("TagData", json.toString());
                startActivity(intent);
                break;
            case R.id.btnSetIcon:
                if (recyclerView.getVisibility() == View.GONE)
                    recyclerView.setVisibility(View.VISIBLE);
                else
                    recyclerView.setVisibility(View.GONE);
                break;
            case R.id.btnSetList:
                if (icons.size() == 0){
                    Toast.makeText(SetImageActivity.this, "삭제할 아이콘이 없습니다", Toast.LENGTH_SHORT);
                    break;
                }

                if(checkDel == false && icons.size() != 0) {
                    checkDel = true;
                    for(int i = 0 ; i<icons.size(); i++) {
                        icons.get(i).setBackgroundResource(R.drawable.baselayout_w_blow_red);
                    }
                }else {
                    checkDel = false;
                    for(int i = 0 ; i<icons.size(); i++) {
                        icons.get(i).setBackgroundResource(R.drawable.baselayout_w_nop);
                        Log.v("checkDel",i+"");
                    }
                }
                break;
            case R.id.btnback:
                finish();
                break;
        }
    }


}
