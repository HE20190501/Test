package com.example.administrator.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class AutomaticActivity extends AppCompatActivity {

    private Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic);

        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new AutomaticActivity.mClick());
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fast, menu);
        return true;
    }
    class mClick implements View.OnClickListener {   //(1)实现接口
        @Override
        public void onClick(View v) {      //(2)重写抽象方法
            Intent intent = new Intent(AutomaticActivity.this, FreedomActivity.class);
            startActivity(intent);
        }
    }
}
