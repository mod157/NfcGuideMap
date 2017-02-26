package com.guidmap.ss2.nfcguidmap;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static java.lang.Integer.toHexString;

public class EnrollActivity extends AppCompatActivity {
    AlertDialog.Builder alert_confirm;
    TextView mTextView;
    private NfcAdapter mNfcAdapter; // NFC 어댑터
    private PendingIntent pendingIntent;
    Intent intent;
    String id, fileName;
    int intentStatus;
    String IconSize;
    String height,width;
    String startPoint;
    private Socket socket;
    {
        try {
            socket = IO.socket("http://218.209.45.76:3000");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    DbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        //DB class 선언==========================
        dbHelper = new DbHelper();
        //=======================================
        dbHelper.database = openOrCreateDatabase(dbHelper.dbName, MODE_WORLD_WRITEABLE, null);
        dbHelper.createTable();
        //====================================================

        mTextView = (TextView) findViewById(R.id.textMessage);
        // NFC 어댑터를 구한다
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // NFC 어댑터가 null 이라면 칩이 존재하지 않는 것으로 간주
        if (mNfcAdapter == null) {
            mTextView.setText("This phone is not NFC enable.");
            return;
        }
        setIntentStatus();
        socket.connect();
        socket.on("Tag_Status", listen_tag);
        mTextView.setText("NFC 태그를 스캔해주세요.");
        intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    }
    private void setIntentStatus(){
        int status;
        Intent intent;
        intent = getIntent();
        //Numberforamt
        status = Integer.parseInt(intent.getExtras().getString("status"));
        intentStatus = status;
    }
    private void AlertDialog_Set(int SearchTag, String contextName){
        Log.v("SunDialog", intentStatus + ", " + SearchTag +", " +id);
        if(intentStatus == 1) {
            if (SearchTag == 0) {
                AlertDialog("등록되어있지 않은 태그입니다.\n" +
                        "새로 등록하시겠습니까?");
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
                                Intent intent = new Intent(EnrollActivity.this, ConfirmcreatorActivity.class);
                                intent.putExtra("TagData", id);
                                intent.putExtra("TagData_Type", "Create");
                                startActivity(intent);
                                finish();
                            }
                        });
            } else {
                AlertDialog(contextName + "\n다운 받으시겠습니까?");
                alert_confirm.setCancelable(true).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return;
                            }
                        }).setPositiveButton("다운",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                Intent intent = new Intent(EnrollActivity.this, NFCDownLoadActivity.class);
                                intent.putExtra("TagData_TagID",id);
                                intent.putExtra("TagData_Size",IconSize);
                                intent.putExtra("TagData_FileName", fileName);
                                intent.putExtra("TagData_width",width);
                                intent.putExtra("TagData_height",height);
                                intent.putExtra("TagData_StartPoint",startPoint);
                                Log.v("SunEn->Down", id + " : " + fileName);
                                startActivity(intent);
                                finish();
                            }
                        });
            }
        }else{
            if (SearchTag == 0) {
                AlertDialog("새로운 태그입니다.\n새로 등록하시겠습니까?");
                alert_confirm.setCancelable(true).setPositiveButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                return;
                            }
                        }).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                Intent intent = new Intent(EnrollActivity.this, ConfirmcreatorActivity.class);
                                intent.putExtra("TagData", id);
                                intent.putExtra("TagData_Type", "Create");
                                startActivity(intent);
                                finish();
                            }
                        });
            } else {
                AlertDialog("이미 존재하는 태그입니다.\n삭제하시겠습니까?");
                alert_confirm.setCancelable(true).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return;
                            }
                        }).setPositiveButton("삭제",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                Intent intent = new Intent(EnrollActivity.this, ConfirmcreatorActivity.class);
                                intent.putExtra("TagData", id);
                                intent.putExtra("TagData_Type", "Change");
                                startActivity(intent);
                                finish();
                            }
                        });
            }
        }
        AlertDialog alert = alert_confirm.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        alert.setCanceledOnTouchOutside(true);
        if(!EnrollActivity.this.isFinishing())
        {
            alert.show();
        }

    }
    private Emitter.Listener listen_tag = new Emitter.Listener() {
        public void call(Object... args) {
            Log.v("Sun", "listen OK");
            final JSONObject obj = (JSONObject)args[0];
            //서버에서 보낸 JSON객체를 사용할 수 있습니다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int SearchTag = 0;
                    String contextName = "";
                    try {
                        Log.v("Sun", SearchTag + " " + obj );
                        SearchTag = Integer.parseInt(obj.get("Tag").toString());
                        contextName = obj.get("TagName").toString();
                        fileName = obj.get("FileName").toString();
                        IconSize = obj.get("Size").toString();
                        width = obj.getString("width");
                        height = obj.getString("height");
                        startPoint = obj.getString("StartPoint");

                        Log.v("SunFilename",fileName);
                        AlertDialog_Set(SearchTag,contextName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public static String byteArrayToHexString(byte[] b) {
        Log.v("tag","11");
        int len = b.length;
        String data = new String();
        for (int i = 0; i < len; i++){
            data += toHexString((b[i] >> 4) & 0xf);
            data += toHexString(b[i] & 0xf);
        }
        Log.v("tag",data);
        return data;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Ndef ndefTag = null;
        String tagid = "";
        try {
            Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            ndefTag = Ndef.get(myTag);
            // 태그 ID
            tagid = byteArrayToHexString(myTag.getId());
            // 태그 크기
            int size = ndefTag.getMaxSize();
            // 쓰기 가능 여부
            boolean writable = ndefTag.isWritable();
            // 태그 타입
            String type = ndefTag.getType();

            if (intent == null)
                return;

            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(getTextAsNdef(), detectedTag);
            mTextView.setText("Tag ID : " + tagid);
            id = tagid;
            sendMessage("Tag");

        }catch(Exception e){
            Log.v("Sun_Error", e+"");
            mTextView.setText("허용되지 않은 NFC Tag입니다.");
        }
    }

    private void writeTag(NdefMessage textAsNdef, Tag detectedTag) {
        Log.v("Tag","Write");
        try{
            NdefFormatable ndefform = NdefFormatable.get(detectedTag);
            if(ndefform == null){
                Toast.makeText(this,"Tag is not ndef format!",Toast.LENGTH_SHORT);
            }
            ndefform.connect();
            ndefform.format(textAsNdef);
            ndefform.close();
        }catch(Exception e){}
    }

    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        try {
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (messages == null) return;

            for (int i = 0; i < messages.length; i++)
                setReadTagData((NdefMessage) messages[0]);
        }catch(Exception e){
            Log.e("NFC error", e+"");
        }
    }

    public void onPause() {
        if (mNfcAdapter != null) {
            //disable를 쓰면 화면이 있을때만사용
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        super.onPause();
    }

    public void setReadTagData(NdefMessage ndefmsg) {
        if(ndefmsg == null ) {
            return ;
        }
        String msgs = "";
        msgs += ndefmsg.toString() + "\n";
        NdefRecord[] records = ndefmsg.getRecords() ;
        for(NdefRecord rec : records) {
            byte [] payload = rec.getPayload() ;
            String textEncoding = "UTF-8" ;
            if(payload.length > 0)
                textEncoding = ( payload[0] & 0200 ) == 0 ? "UTF-8" : "UTF-16";

            Short tnf = rec.getTnf();
            String type = String.valueOf(rec.getType());
            String payloadStr = new String(rec.getPayload(), Charset.forName(textEncoding));
        }
        Log.v("tag","1");
    }
    public void AlertDialog(String str){
        alert_confirm = new AlertDialog.Builder(EnrollActivity.this);
        TextView messageText = new TextView(this);
        TextView title = new TextView(this);
        title.setText("태그 완료");
        title.setTextColor(Color.parseColor("#000000"));
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        alert_confirm.setCustomTitle(title);
        messageText.setText(str);
        messageText.setGravity(Gravity.CENTER);
        messageText.setPadding(10,50,10,0);
        messageText.setTextSize(20);
        messageText.setTextColor(Color.parseColor("#000000"));
        alert_confirm.setView(messageText);
    }

    private NdefMessage getTextAsNdef() {
        byte[] textBytes = mTextView.getText().toString().getBytes();

        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[] {},
                textBytes);

        return new NdefMessage(new NdefRecord[] {textRecord});
    }

    private NdefMessage getUriAsNdef() {
        byte[] textBytes = mTextView.getText().toString().getBytes();

        NdefRecord record1 = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                new String("U").getBytes(Charset.forName("US-ASCII")),
                new byte[0],
                textBytes) ;

        return new NdefMessage(new NdefRecord[] {record1});
    }

    private void sendMessage(String name){
        JSONObject sendText;
        String message = id+"";
        sendText = new JSONObject();
        try {
            sendText.put("text", message);
            socket.emit(name, sendText);
        } catch (JSONException e) {
            Log.v("socket error : ", e.toString());
        }
    }

}
