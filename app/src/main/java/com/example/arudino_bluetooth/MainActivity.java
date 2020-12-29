package com.example.arudino_bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CardView PurposeCard, SettingCard, SmokingCard, WhatCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //defining Cards

        PurposeCard = (CardView) findViewById(R.id.purpose_card);
        SettingCard = (CardView) findViewById(R.id.setting_card);
        SmokingCard = (CardView) findViewById(R.id.smoking_card);
        WhatCard = (CardView) findViewById(R.id.what_card);
        // Add click listener to the cards

        PurposeCard.setOnClickListener(this);
        SettingCard.setOnClickListener(this);

        SmokingCard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nosmokeguide.go.kr/lay2/program/S1T50C56/nosmoke/noSmoke_selftest/noSmoke_selftest1_3q.do"));
                startActivity(intent);
            }
        });

        WhatCard.setOnClickListener(this);

    }

    public void onClick(View v){
        Intent intent;

        switch(v.getId()){
            case R.id.purpose_card : intent = new Intent(this,PurposeActivity.class); startActivity(intent); break;
            case R.id.setting_card : intent = new Intent(this,SettingActivity.class); startActivity(intent); break;
            case R.id.what_card : intent = new Intent(this,WhatActivity.class); startActivity(intent); break;
        }

    }
}
