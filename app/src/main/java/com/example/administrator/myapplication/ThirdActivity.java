package com.example.administrator.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ThirdActivity extends AppCompatActivity {
    public static BluetoothAdapter bluetoothAdapter = null;
    private TextView noticeView = null;
    private ConnectThread connectThread = null;
    private ConnectedThread connectedThread = null;
    public static final int RECV_VIEW = 0;
    public static final int NOTICE_VIEW = 1;
    private Button turnOnOff = null;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        noticeView = (TextView) findViewById(R.id.notice_view);
        button2 = (Button) findViewById(R.id.button2);

        button2.setOnClickListener(new mClick());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return;
        }
        new Thread() {
            public void run() {

                if (!bluetoothAdapter.isEnabled()) {
                    noticeView.setText("蓝牙未开启");
                } else {
                    noticeView.setText("蓝牙已开启");
                }
                if (bluetoothAdapter != null) {
                    // 开启蓝牙
                    int REQUEST_ENABLE_BT = 1;
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_ENABLE_BT);
                        noticeView.setText("开启蓝牙成功");
                        //Toast.makeText(this, "开启蓝牙成功", Toast.LENGTH_SHORT).show();
                        // 查询配对设备 建立连接，只能连接第一个配对的设备
                    }
                }
            }
        }.start();
    }

    class mClick implements View.OnClickListener {   //(1)实现接口
        @Override
        public void onClick(View v) {      //(2)重写抽象方法
            Intent intent = new Intent(ThirdActivity.this, KENHActivity.class);
            startActivity(intent);
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //*******************如果蓝牙未开启，开启蓝牙设备**********************
        if (id == R.id.start_bluetooth) {  // 菜单栏中 开启蓝牙 控件/按钮
            if (bluetoothAdapter != null) {
                // 开启蓝牙
                int REQUEST_ENABLE_BT = 1;
                if (!bluetoothAdapter.isEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    noticeView.setText("开启蓝牙成功");
                    //Toast.makeText(this, "开启蓝牙成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
        else if (id == R.id.show_devices) {  // 菜单栏中 查询配对设备 控件/按钮
            if (bluetoothAdapter != null) {  //判断蓝牙适配器是否开启 ***开启执行下一步***
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // 查询配对设备
                List<String> devices = new ArrayList<String>();
                Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : bondedDevices) {
                    devices.add(device.getName() + "-" + device.getAddress());
                }
                StringBuilder text = new StringBuilder();
                for (String device : devices) {
                    text.append(device + "\n");
                }
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        else if (id == R.id.find_devices) {  // 菜单栏中 搜索设备 控件/按钮
            Toast.makeText(this, "该功能暂时不可用", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.connect_devices) {  // 菜单栏中 连接设备 控件/按钮
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                return true;
            }
            //************************查询配对设备 建立连接****************************
            // 查询配对设备 建立连接，只能连接第一个配对的设备
            List<String> devices = new ArrayList<String>();
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                connectThread = new ConnectThread(device);
                connectThread.start();
            /*    if(noticeView.getText().toString().equals("连接成功")){
                    Intent intent = new Intent(ThirdActivity.this, FastActivity.class);
                    startActivity(intent);
                }  */
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isOn = false;
    public android.os.Handler handler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = null;
            switch (msg.what) {
                case RECV_VIEW:
                    if (isOn == false) {
                        isOn = true;
                        turnOnOff.setText("OFF");
                    }
                    bundle = msg.getData();
                    String recv = bundle.getString("recv");

                    if (recv.isEmpty() || recv.contains(" ") || recv.contains("#")) {
                        break;
                    }
                    int num = Integer.valueOf(recv) / 2; // 0-60s

                case NOTICE_VIEW:
                    bundle = msg.getData();
                    String notice = bundle.getString("notice");
                    noticeView.setText(notice);
                    break;

                default:
                    break;
            }
        }
    };

    //*******************************服务器端建立监听********************************
    //如果是服务器端，需要建立监听，注意监听的是某个服务的UUID，服务器监听类如下：
    public class ConnectThread extends Thread {
        private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.socket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
                connectedThread = new ConnectedThread(socket);
                connectedThread.start();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
                return;
            }
            //manageConnectedSocket(socket);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //******************建立连接成功后/用ConnectedThread 收发数据**********************
    // 客户端与服务器端建立连接成功后，需要ConnectedThread 类接收发送数据：
    public class ConnectedThread extends Thread {
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
            tmpMessage.what = NOTICE_VIEW;
            tmpMessage.setData(tmpBundle);
            handler.sendMessage(tmpMessage);
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
                    message.what = RECV_VIEW;
                    message.setData(bundle);
                    handler.sendMessage(message);
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
}
