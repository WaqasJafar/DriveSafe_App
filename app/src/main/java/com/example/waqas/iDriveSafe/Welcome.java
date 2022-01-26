package com.example.fyp.iDriveSafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

public class Welcome extends AppCompatActivity {

    LinearLayout linear1, linear2;
    Button buttonmenu;
    Animation uptodown,downtoup;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        buttonmenu = (Button)findViewById(R.id.buttonmenu);

        linear1 = (LinearLayout)findViewById(R.id.linear1);
        linear2 =(LinearLayout)findViewById(R.id.linear2);
        uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
        downtoup = AnimationUtils.loadAnimation(this,R.anim.downtoup);
        linear1.setAnimation(uptodown);
        linear2.setAnimation(downtoup);

        buttonmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,MainActivity.class);
                startActivity(intent);
            }
        });





    }
}