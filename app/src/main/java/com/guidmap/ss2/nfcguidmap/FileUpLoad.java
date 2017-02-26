package com.guidmap.ss2.nfcguidmap;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FileUpLoad extends AppCompatActivity implements  View.OnClickListener {
	//String uploadFilePath = "/storage/9016-4EF8/DCIM/100ANDRO/";
	//String uploadFileName = "DSC_0127.JPG";
	ImageView imageView;
	TextView messageText;
	String tagID = "";
	String name, pw;
	String absolutePath = "";
	int serverResponseCode = 0;
	ProgressDialog dialog = null;
	String upLoadServerUri = null;
	Button btnMapOk,btnMapView;
	Bitmap selPhoto;
	JSONObject json;
	String fileType;
	String type;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fileupload);

        imageView=(ImageView)findViewById(R.id.imageView);
		upLoadServerUri = "http://218.209.45.76:3000/api/photo";
		Intent intent = getIntent();
		tagID = intent.getExtras().getString("TagData_id");
		name = intent.getStringExtra("TagData_name");
		pw = intent.getStringExtra("TagData_pw");
		type = intent.getExtras().getString("TagData_Type");

		Log.v("Sun_fileup", tagID + " " + name + "  " + pw);
		btnMapOk = (Button) findViewById(R.id.btnMapOk);
		btnMapView = (Button) findViewById(R.id.btnMapView);
		btnMapView.setOnClickListener(this);
		btnMapOk.setOnClickListener(this);
		if(selPhoto !=null){
			imageView.setImageBitmap(selPhoto);
		}
    }
    final int REQ_SELECT=0;
   
    //갤러리 호출해서 이미지 읽어오기
    public void push(View v){
    	//사진 읽어오기 위한 uri 작성하기.
    	 Uri uri = Uri.parse("content://media/external/images/media");
    	 //무언가 보여달라는 암시적 인텐트 객체 생성하기.
         Intent intent = new Intent(Intent.ACTION_VIEW, uri);
         //인텐트에 요청을 덧붙인다. 
         intent.setAction(Intent.ACTION_GET_CONTENT);
         //모든 이미지
         intent.setType("image/*");
         //결과값을 받아오는 액티비티를 실행한다.
         startActivityForResult(intent, REQ_SELECT);
    }
	//카메라로 찍기
	public void takePicture(View v){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public void JSONPacking(){
		json = new JSONObject();
		try {
			json.put("TagID", tagID);
			json.put("TagName", name);
			json.put("TagPW",pw);
			json.put("TagPath",absolutePath);
			json.put("TagFileType",fileType);
		}catch(JSONException e){

		}
	}



    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (intent == null) return;
    	try{
			//인텐트에 데이터가 담겨 왔다면
			if(intent.getData() != null){
				//해당경로의 이미지를 intent에 담긴 이미지 uri를 이용해서 Bitmap형태로 읽어온다.
			   selPhoto = Images.Media.getBitmap(getContentResolver(), intent.getData());

			    //이미지의 크기 조절하기.
			   // selPhoto = Bitmap.createScaledBitmap(selPhoto, , 1000, true);
			    //image_bt.setImageBitmap(selPhoto);//썸네일
			    //화면에 출력해본다.
			    imageView.setImageBitmap(selPhoto);
			    Log.e("선택 된 이미지 ", "selPhoto : " + selPhoto);
			   
			}
		}catch(FileNotFoundException e) {
		    e.printStackTrace();
		}catch(IOException e) {
		    e.printStackTrace();
		}
		//선택한 이미지의 uri를 읽어온다.
		Uri selPhotoUri = intent.getData();

	    //절대경로를 획득한다!!! 중요~
//	    Cursor c = getContentResolver().query(Uri.parse(selPhotoUri.toString()), null,null,null,null);
//	    c.moveToNext();
	    //업로드할 파일의 절대경로 얻어오기("_data") 로 해도 된다.
//	    String absolutePath2 = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA+1));
		absolutePath = getPath(this,selPhotoUri);
//		Log.d("Sun FilePath",absolutePath2+"\n"+ absolutePath);
	    Log.e("###파일의 절대 경로###", absolutePath+"");
/*
	   //파일 업로드 시작!
		dialog = ProgressDialog.show(FileUpLoad.this, "", "Uploading file...", true);
		new Thread(new Runnable() {
			public void run() {
				uploadFile(absolutePath);
			}
		}).start();
*/    }

	public static String getPath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] {
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
		}

		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {column};
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig){
// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {
		Intent intent= null;
		JSONPacking();
		switch(v.getId()) {
			case R.id.btnMapOk:
				intent = new Intent(FileUpLoad.this, SetImageActivity.class);
				fileType = "Image";
				Log.v("Sun File -> Set", json.toString());
				break;
			case R.id.btnMapView:
				intent = new Intent(FileUpLoad.this,  MapActivity.class);
				fileType = "Map";
				Log.v("Sun File -> Map", json.toString());
				break;
		}

		JSONPacking();
		intent.putExtra("Type",type);
		intent.putExtra("TagData", json.toString());
		startActivity(intent);
	}
}