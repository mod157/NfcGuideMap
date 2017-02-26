package com.guidmap.ss2.nfcguidmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class ConfirmcreatorActivity extends AppCompatActivity {
    Button btnCreator;
    EditText editCreator, editPassword;
    String id, name, pw;
    String type;
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
        setContentView(R.layout.activity_confirmcreator);
        btnCreator = (Button)findViewById(R.id.btncreator);
        editCreator = (EditText)findViewById(R.id.editCreator);
        editPassword = (EditText)findViewById(R.id.editPassword);
        Intent getData = getIntent();
        //Create이면 생성, change이면 변경
        type = getData.getExtras().getString("TagData_Type");
        id = getData.getExtras().getString("TagData");
        btnCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editCreator.getText().toString();
                pw = editPassword.getText().toString();
                if(name.equals("") && pw.equals("")){
                    Toast.makeText(ConfirmcreatorActivity.this,"공백이 존재합니다.",Toast.LENGTH_SHORT);
                    return;
                }
                Encryption encryption = new Encryption();
                encryption.encryption(pw);
                pw = encryption.getPassword();
                if(type.equals("Create")) {
                    create_intent();
                }
                if(type.equals("Change")){
                    sendMessage(id,name,pw);
                }
            }
        });
        socket.on("Checked",listen);
    }
    private void create_intent(){
        Intent intent = new Intent(ConfirmcreatorActivity.this, FileUpLoad.class);
        intent.putExtra("TagData_id", id);
        intent.putExtra("TagData_name", name);
        intent.putExtra("TagData_pw", pw);
        intent.putExtra("TagData_Type", type);

        Log.v("Sun_creator", "id : " + id + "\nname : " + name + "\npw : " + pw);
        startActivity(intent);
        return;
    }
    private Emitter.Listener listen = new Emitter.Listener() {
        public void call(Object... args) {
            final JSONObject obj = (JSONObject)args[0];
            //서버에서 보낸 JSON객체를 사용할 수 있습니다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v("SunConfim", "listen OK\n"+obj);
                    try {
                        String status = obj.getString("Checked");
                        if(status.equals("1")) {
                            create_intent();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private void sendMessage(String id, String name, String pw){
        JSONObject sendText;
        sendText = new JSONObject();
        try {
            sendText.put("TagID", id);
            sendText.put("TagCreator", name);
            sendText.put("TagPassWord",pw);
            socket.emit("TagUserCheck", sendText);
        } catch (JSONException e) {
            Log.v("socket error : ", e.toString());
        }
    }
}
