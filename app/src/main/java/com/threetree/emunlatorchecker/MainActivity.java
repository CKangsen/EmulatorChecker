package com.threetree.emunlatorchecker;

import android.Manifest;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.threetree.EmulatorCheckerSdk;

//import com.squareup.leakcanary.LeakCanary;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//			// This process is dedicated to LeakCanary for heap analysis.
//			// You should not init your app in this process.
//			return;
//		}
//		LeakCanary.install(this.getApplication());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},0);
        }

        findViewById(R.id.btn_moni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EmulatorCheckerSdk.checkIsEmulator(MainActivity.this, new EmulatorCheckerSdk.CheckerCallback() {
                    @Override
                    public void onCheckDone(boolean result) {
                        TextView textView = (TextView) findViewById(R.id.btn_moni);
                        textView.setText(" 是否模拟器 " + result);
                    }
                });
            }
        });
    }
}
