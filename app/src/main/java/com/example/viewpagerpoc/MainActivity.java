package com.example.viewpagerpoc;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView imgHome;
    ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_layout);

         imgHome = (ImageView)findViewById(R.id.imgHome);
         imgBack = (ImageView)findViewById(R.id.imgBack);
        imgHome.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        //Grid view setup..
        GridView gridView = (GridView) findViewById(R.id.gridView);
        final AppsAdapter appsAdapter = new AppsAdapter(this, Constants.getAppsData());
        gridView.setAdapter(appsAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                AppsModel appsModel = appsAdapter.getItem(position);
                Intent launchIntent = null;
                if(appsModel.getAppLaunch()){
                    //launch app
                    launchIntent = getPackageManager().getLaunchIntentForPackage(appsModel.getPackageId());
                }
                else {
                    //launch activity
                    launchIntent = new Intent(MainActivity.this, appsModel.getActivityName());
                }

                try {
                    startActivity(launchIntent);
                }
                catch (Exception e){
                    Toast.makeText(MainActivity.this, "Something went wrong launching the app/activity", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Display confirmation here, finish() activity.
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private List<ApplicationInfo> getInstalledAppsFromDevice(){
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        return packages;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgHome:
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                break;

            case R.id.imgBack:
                finish();
                break;
        }
    }
}
