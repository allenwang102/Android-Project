package com.itheima.robot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RobotActivity extends AppCompatActivity {
    private ListView listView;
    private ChatAdapter adapter;
    private List<ChatBean> chatBeanList;
    private EditText et_send_msg;
    private Button btn_send;

    private static final String WEB_SITE = "http://tuling.com/openapi/api/v2";
    private static final String KEY = "48132f492137417f83069267af5cb2f7";
    private String sendMsg;
    private String welcome[];
    private MHandler mHandler;
    public static final int MSG_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatBeanList = new ArrayList<ChatBean>();
        mHandler = new MHandler();
        welcome = getResources().getStringArray(R.array.welcome);
        initView();

    }

    public void initView() {
        listView = (ListView) findViewById(R.id.list);
        et_send_msg = (EditText) findViewById(R.id.et_send_msg);
        btn_send = (Button) findViewById(R.id.btn_send);
        adapter = new ChatAdapter(chatBeanList, this);
        listView.setAdapter(adapter);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendData();
            }
        });
        et_send_msg.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View view,int keyCode,KeyEvent keyEvent){
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendData();
                }
                return false;
            }
        });
        int position = (int) (Math.random() * welcome.length - 1);
        showData(welcome[position]);
    }

    private void sendData() {
        sendMsg = et_send_msg.getText().toString();
        if (TextUtils.isEmpty(sendMsg)) {
            Toast.makeText(this, "您还未输任何信息哦", Toast.LENGTH_LONG).show();
            return;
        }
        et_send_msg.setText("");
        sendMsg = sendMsg.replaceAll("", "").replaceAll("\n", "").trim();
        ChatBean chatBean = new ChatBean();
        chatBean.setMessage(sendMsg);
        chatBean.setState(chatBean.SEND);
        chatBeanList.add(chatBean);
        adapter.notifyDataSetChanged();
        getDataFromServer();
    }

    private void getDataFromServer() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(WEB_SITE + "?key=" + KEY + "&info=" + sendMsg).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Message msg = new Message();
                msg.what = MSG_OK;
                msg.obj = res;
                mHandler.sendMessage(msg);
            }
    });
    }

    class MHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case MSG_OK:
                    if (msg.obj != null) {
                        String vlResult = (String) msg.obj;
                        paresData(vlResult);
                    }
                    break;
            }
        }
    }

    private void paresData(String JsonData) {
        try {
            JSONObject obj = new JSONObject(JsonData);
            String content = obj.getString("text");
            int code = obj.getInt("code");
            updateView(code, content);
        } catch (JSONException e) {
            e.printStackTrace();
            showData("主人，你的网络不好哦");
        }
    }

    private void showData(String message) {
        ChatBean chatBean = new ChatBean();
        chatBean.setMessage(message);
        chatBean.setState(ChatBean.RECEIVE);
        chatBeanList.add(chatBean);
        adapter.notifyDataSetChanged();
    }

    private void updateView(int code, String content) {
        switch (code) {
            case 4004:
                showData("主人，今天我累了，明天再来找我玩吧");
                break;
            case 40005:
                showData("主人，你说的是外星语吗？");
                break;
            case 400006:
                showData("主人，今天我要去约会哦，暂不接客啦");
                break;
            case 4000007:
                showData("主人，明天再和你玩啦，我生病了，呜呜……");
                break;
            default:
                showData(content);
                break;
        }
    }

    protected long exitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(RobotActivity.this, "再按一次退出智能聊天程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                RobotActivity.this.finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
}


