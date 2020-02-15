package com.nextsol.digitalcompass.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.nextsol.digitalcompass.R;

public class AdsActivity extends AppCompatActivity {
    CountDownTimer countDownTimer;
    TextView textViewads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        textViewads = (TextView) findViewById(R.id.textviewAds);
        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewads.setText("Ads will close after: " + millisUntilFinished/1000  + "seconds");
            }

            @Override
            public void onFinish() {
                finish();
            }
        };
        countDownTimer.start();

    }
}
