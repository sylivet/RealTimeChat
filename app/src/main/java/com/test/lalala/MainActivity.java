package com.test.lalala;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    EditText ipEditText;
    Button connectButton;
    String ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipEditText=findViewById(R.id.ip);
        connectButton=findViewById(R.id.connectButton);
        ip= String.valueOf(ipEditText.getText());
        Intent intent = new Intent(this,MainActivity2.class);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("ip",ip);
                startActivity(intent);
            }
        });

    }
}