package com.example.administrator.myapplication;

        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;
        import android.content.Intent;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.os.Message;
        import android.support.v7.app.AppCompatActivity;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.RadioButton;
        import android.widget.RadioGroup;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public int flagroom = 0;
    public int flagjing = 0;

    public static final int RECV_VIEW = 0;
    public static final int NOTICE_VIEW = 1;

    private static BluetoothAdapter bluetoothAdapter = null;

    private ConnectThread connectThread = null;
    private  ConnectedThread connectedThread = null; //用 ConnectedThread 收发数据

    private TextView noticeView = null;
    private Button turnOnOff = null;
    ScrollView scrollView = null;
    private TextView recvView = null;
    private Button clearRecvView = null;
    private EditText sendText = null;
    private Button send = null ;

    //
    private Button time_send ,button2;
    private EditText time_text = null;

    private RadioGroup choose_led,choose_changjing,choose_diy;
    private RadioButton led_woshi,led_keting;
    private RadioButton changjing_zhengchang,changjing_qichuang,changjing_lijia;
    private RadioButton diy_1,diy_2,diy_3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // 获取BluetoothAdapter (本地蓝牙适配器)      如果蓝牙未开启，开启蓝牙设备
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return;
        }

        // 注册监听事件
        noticeView = (TextView) findViewById(R.id.notice_view);
        turnOnOff = (Button) findViewById(R.id.turn_on_off);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        recvView = (TextView) findViewById(R.id.recv_view);
        clearRecvView = (Button) findViewById(R.id.clear_recv_view);
        sendText = (EditText) findViewById(R.id.send_text);
        send = (Button) findViewById(R.id.send);

        time_send = (Button) findViewById(R.id.time_send);
        time_text = (EditText) findViewById(R.id.time_text);

        choose_led = (RadioGroup) findViewById(R.id.led_choose);
        choose_changjing = (RadioGroup) findViewById(R.id.changjing_choose);
        choose_diy = (RadioGroup) findViewById(R.id.diy_choose);
        //房间选择
        led_woshi = (RadioButton) findViewById(R.id.led_woshi);
        led_keting = (RadioButton) findViewById(R.id.led_keting);
        //正常场景
        changjing_zhengchang = (RadioButton) findViewById(R.id.changjing_zhengchang);
        changjing_qichuang = (RadioButton) findViewById(R.id.changjing_qichuang);
        changjing_lijia = (RadioButton) findViewById(R.id.changjing_lijia);
        //diy场景
        diy_1 = (RadioButton) findViewById(R.id.diy_1);
        diy_2 = (RadioButton) findViewById(R.id.diy_2);
        diy_3 = (RadioButton) findViewById(R.id.diy_3);

        choose_led.setOnCheckedChangeListener(new roomRadioButtonListener() );//注意是给RadioGroup绑定监视器
        choose_changjing.setOnCheckedChangeListener(new jingRadioButtonListener() );//注意是给RadioGroup绑定监视器
        choose_diy.setOnCheckedChangeListener(new diyRadioButtonListener() );//注意是给RadioGroup绑定监视器

        button2 = (Button) findViewById(R.id.button2);

        button2.setOnClickListener(this);
        turnOnOff.setOnClickListener(this);
        clearRecvView.setOnClickListener(this);
        send.setOnClickListener(this);
        time_send.setOnClickListener(this);

        if (!bluetoothAdapter.isEnabled()) {
            noticeView.setText("蓝牙未开启");
        }
        else {
            noticeView.setText("蓝牙已开启");
        }
        noticeView.setBackgroundColor(Color.GRAY); //noticeView的背景色设置为灰
    }

    class roomRadioButtonListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // 选中状态改变时被触发
            switch (checkedId) {
                case R.id.led_keting:
                    flagroom=1;
                    break;
                case R.id.led_woshi :
                    flagroom=2;
                    break;
                default:
                    flagroom=0;
                    break;
            }
        }
    }
    class jingRadioButtonListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group1, int checkedId) {
            // 选中状态改变时被触发
            switch (checkedId) {
                case R.id.changjing_zhengchang :
                    flagjing=1;
                    break;
                case R.id.changjing_qichuang:
                    flagjing=2;
                    break;
                case R.id.changjing_lijia:
                    flagjing=3;
                    break;
                default:
                    flagjing=0;
                    break;

            }
        }
    }
    class diyRadioButtonListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // 选中状态改变时被触发
            switch (checkedId) {
                case R.id.diy_1:
                    flagjing=4;
                    break;
                case R.id.diy_2 :
                    flagjing=5;
                    break;
                case R.id.diy_3 :
                    flagjing=6;
                    break;
                default:
                    flagjing=0;
                    break;
            }
        }
    }


    @Override   //********************button触发****************************//
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_recv_view:   //清空接收&发送框&定时栏
                recvView.setText("");// 清空接收框
                sendText.setText("");// 清空发送栏
                time_text.setText("");//清空定时栏
                break;

            case R.id.button2:
                Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
                startActivity(intent);

            case R.id.send:
              //  Intent intent = new Intent(MainActivity.this,FastActivity.class);
              //  startActivity(intent);
//                String a = String.valueOf(flagroom);
//                sendText.setText(a);
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (connectedThread == null) {
                    Toast.makeText(this, "未连接设备", Toast.LENGTH_SHORT).show();
                    break;
                }
                //一键场景选择
                if(flagroom==1 && flagjing==1){
                    sendText.setText("A1");
                }
                else if(flagroom==1 && flagjing==2){
                    sendText.setText("A2");
                }
                else if(flagroom==1 && flagjing==3){
                    sendText.setText("A3");
                }
                else if(flagroom==2 && flagjing==1){
                    sendText.setText("B1");
                }
                else if(flagroom==2 && flagjing==2){
                    sendText.setText("B2");
                }
                else if(flagroom==2 && flagjing==3){
                    sendText.setText("B3");
                }
                //diy场景选择
                else if(flagroom==1 && flagjing==4){
                    sendText.setText("A4");
                }
                else if(flagroom==1 && flagjing==5){
                    sendText.setText("A5");
                }
                else if(flagroom==1 && flagjing==6){
                    sendText.setText("A6");
                }
                else if(flagroom==2 && flagjing==4){
                    sendText.setText("B4");
                }
                else if(flagroom==2 && flagjing==5){
                    sendText.setText("B5");
                }
                else if(flagroom==2 && flagjing==6){
                    sendText.setText("B6");
                }
                else{
                    sendText.setText("H");
                }
                // + "@#"
                time_text.setText("");  //清空定时栏
                String inputText = sendText.getText().toString(); // 发送给单片机数据以"@#结尾"，这样单片机知道一条数据发送结束
                //Toast.makeText(MainActivity.this, inputText, Toast.LENGTH_SHORT).show();
                connectedThread.write(inputText.getBytes());

            case R.id.time_send:
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (connectedThread == null) {
                    Toast.makeText(this, "未连接设备", Toast.LENGTH_SHORT).show();
                    break;
                }
                sendText.setText("");   //清空输入框
                String intimeText = time_text.getText().toString(); // 发送给单片机数据以"@#结尾"，这样单片机知道一条数据发送结束
                //Toast.makeText(MainActivity.this, inputText, Toast.LENGTH_SHORT).show();
                connectedThread.write(intimeText.getBytes());

            /*   if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (connectedThread == null) {
                    Toast.makeText(this, "未连接设备", Toast.LENGTH_SHORT).show();
                    break;
                }
                String inputText = sendText.getText().toString() + "@#"; // 发送给单片机数据以"@#结尾"，这样单片机知道一条数据发送结束
                //Toast.makeText(MainActivity.this, inputText, Toast.LENGTH_SHORT).show();
                connectedThread.write(inputText.getBytes());
                break;
         */
            default:
                break;
        }
    }
    private boolean isOn = false;

    private android.os.Handler handler = new android.os.Handler() {
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
                    recvView.append(recv + "\n");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN); // 滚动到底部

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

    @Override  //***************调用 menu/menu_main.xml 布局生成菜单栏*****************
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
                //Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
    //*******************************服务器端建立监听********************************
    //如果是服务器端，需要建立监听，注意监听的是某个服务的UUID，服务器监听类如下：
    private class ConnectThread extends Thread {
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
