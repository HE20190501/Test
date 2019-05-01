package com.example.administrator.myapplication;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FastActivity extends AppCompatActivity implements View.OnClickListener {
    private RadioGroup choose_led;
    private RadioButton led_woshi, led_keting,led_shufang,led_woshi2;
    private EditText sendText = null;
    private Button send;

    public int flag = 0;

    private ConnectedThread connectedThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast);

        choose_led = (RadioGroup) findViewById(R.id.led_choose);
        led_woshi = (RadioButton) findViewById(R.id.led_woshi);
        led_keting = (RadioButton) findViewById(R.id.led_keting);
        led_shufang = (RadioButton) findViewById(R.id.led_shufang);
        led_woshi2 = (RadioButton) findViewById(R.id.led_woshi2);
        send = (Button) findViewById(R.id.send);

        send.setOnClickListener(new mClick());
        choose_led.setOnCheckedChangeListener(new MyRadioButtonListener());//注意是给RadioGroup绑定监视器
    }

    @Override  //***************调用 menu/menu_main.xml 布局生成菜单栏*****************
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    class mClick implements View.OnClickListener {   //(1)实现接口
        @Override
        public void onClick(View v) {      //(2)重写抽象方法
            Intent intent = new Intent(FastActivity.this, TimeActivity.class);
            startActivity(intent);
        }
    }

   class MyRadioButtonListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // 选中状态改变时被触发
            switch (checkedId) {
                case R.id.led_woshi:
                case R.id.led_keting:
                    flag=1;
                    break;
            }
        }
    }


    //******************建立连接成功后/用ConnectedThread 收发数据**********************
    // 客户端与服务器端建立连接成功后，需要ConnectedThread 类接收发送数据：
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream input = null;
            OutputStream output = null;

            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.inputStream = input;
            this.outputStream = output;
        }

        public void run() {
            StringBuilder recvText = new StringBuilder();
            byte[] buff = new byte[1024];
            int bytes;

            Bundle tmpBundle = new Bundle();
            Message tmpMessage = new Message();
            tmpBundle.putString("notice", "连接成功");

            while (true) {
                try {
                    bytes = inputStream.read(buff);
                    String str = new String(buff, "ISO-8859-1");
                    str = str.substring(0, bytes);

                    // 收到数据，单片机发送上来的数据以"#"结束，这样手机知道一条数据发送结束
                    //Log.e("read", str);
                    if (!str.endsWith("#")) {
                        recvText.append(str);
                        continue;
                    }
                    recvText.append(str.substring(0, str.length() - 1)); // 去除'#'

                    Bundle bundle = new Bundle();
                    Message message = new Message();

                    bundle.putString("recv", recvText.toString());
                    recvText.replace(0, recvText.length(), "");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_recv_view:   //清空接收&发送框
                // recvView.setText("");// 清空接收框
                sendText.setText("");// 清空发送栏
                break;

            case R.id.send: // 发送数据，默认以"@#"结尾
                if (connectedThread == null) {
                    Toast.makeText(this, "未连接设备", Toast.LENGTH_SHORT).show();
                    break;
                }
                if(flag == 1){
                    sendText.setText("A1");
                }
                String inputText = sendText.getText().toString() + "@#"; // 发送给单片机数据以"@#结尾"，这样单片机知道一条数据发送结束
                //Toast.makeText(MainActivity.this, inputText, Toast.LENGTH_SHORT).show();
                connectedThread.write(inputText.getBytes());
                break;

            default:
                break;
        }
    }
}