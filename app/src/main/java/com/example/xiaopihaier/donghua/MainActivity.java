package com.example.xiaopihaier.donghua;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LuckPan mLuckpan;
    private ImageView mStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentVIew();
    }

    private void IntentVIew() {
        mLuckpan = (LuckPan) findViewById(R.id.id_luckPan);
        mStartBtn = (ImageView) findViewById(R.id.id_start_btn);
        mStartBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_start_btn:
                if (!mLuckpan.isStart()) {
                    mLuckpan.luckyStart(1);
                    mStartBtn.setImageResource(R.mipmap.stop);
                } else {
                    if (!mLuckpan.isShouldEnd()) {
                        mLuckpan.luckyEnd();
                        mStartBtn.setImageResource(R.mipmap.start);
                    }
                }
                break;
        }
    }
}
