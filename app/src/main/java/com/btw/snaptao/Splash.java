package com.btw.snaptao;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

/**
 * Created by thp on 2015/12/22.
 * <p/>
 * 启动页面
 */
public class Splash extends Activity {
    public static int TIME = 3000;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        sp = getSharedPreferences("start", MODE_PRIVATE);
//        editor = sp.edit();
//        if (sp.getInt("start", 0) == 0) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
//                    editor.putInt("start", 1);
//                    editor.commit();
                    Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                    Splash.this.startActivity(mainIntent);
                    Splash.this.finish();
                }

            }, TIME);

//        } else {
//            Intent mainIntent = new Intent(Splash.this, MainActivity.class);
//            Splash.this.startActivity(mainIntent);
//            Splash.this.finish();
//        }


    }
}
