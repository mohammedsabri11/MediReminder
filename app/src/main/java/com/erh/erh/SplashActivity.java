package com.erh.erh;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
         findViewById(R.id.Continue).setOnClickListener(this);


    }

    @Override
    protected void onStart() {

        if (SettingPrefManager.getInstance(getApplicationContext()).isFirstTime() ) {
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
            finish();
        }
        super.onStart();

    }

    @Override
    public void onClick(View view) {
        if (view .getId()== R.id.Continue) {
            SettingPrefManager.getInstance(getApplicationContext()).setFirstTime();
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
            finish();
        }
    }
}


