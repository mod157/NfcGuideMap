package com.guidmap.ss2.nfcguidmap;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, TabHost.OnTabChangeListener{
    long backKeyTime=0;
    TabHost tabHost;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPager();
        initTap();
        permission();
    }

    private void initPager(){
        mPager = (ViewPager)findViewById(R.id.view_pager);
        List<Fragment> listFragments = new ArrayList<>();
        listFragments.add(new FragmentTap1());
        listFragments.add(new FragmentTap2());
        PagerAdapter fPagerAdapter = new PagerAdapter(getSupportFragmentManager(), listFragments);
        mPager.setAdapter(fPagerAdapter);
        mPager.setOnPageChangeListener(this);
    }

    private void initTap(){
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        String[] tabNames = {"목록","설정"};
        for(int i=0; i<tabNames.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = tabHost.newTabSpec(tabNames[1]);
            tabSpec.setIndicator(tabNames[i]);
            tabSpec.setContent(new FakeContent(getApplicationContext()));
            tabHost.addTab(tabSpec);
        }

        //탭 높이 크기
        tabHost.getTabWidget().getChildAt(0).getLayoutParams().height = 150;
        tabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 150;

        //텍스트 사이즈 칼라 변경
        for (int i=0; i<tabHost.getTabWidget().getChildCount(); i++) {
            LinearLayout relLayout = (LinearLayout)tabHost.getTabWidget().getChildAt(i);
            TextView tv = (TextView)relLayout.getChildAt(1);
            tv.setTextSize(20);
            tv.setTextColor(Color.parseColor("#ffffff"));
            // tv.setTextSize나 tv.setTextColor 혹은 tv.setTextAppearance를 호출.
        }
        tabHost.setOnTabChangedListener(this);
    }

    private void permission(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
             //   Toast.makeText(MainActivity.this, "NFC 권한 허가", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {
                Toast.makeText(MainActivity.this, "NFC 권한 거부\n" + arrayList.toString(), Toast.LENGTH_SHORT).show();
                finish();
            }
        };
        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("접근 권한이 필요합니다.")
                .setDeniedMessage("권한설정을 하지 않으면 이용하기가 어렵습니다.")
                .setPermissions(android.Manifest.permission.NFC)
                .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .setPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    public class FakeContent implements TabHost.TabContentFactory{
        Context context;
        public FakeContent(Context context){
            this.context = context;
        }
        @Override
        public View createTabContent(String tag) {
            View fakeView = new View(context);
            fakeView.setMinimumHeight(0);
            fakeView.setMinimumWidth(0);
            return fakeView;
        }
    }

    @Override
    public void onBackPressed() {
        Toast toast;

        if (System.currentTimeMillis() > backKeyTime + 2000) {
            backKeyTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyTime + 2000) {
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        tabHost.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onTabChanged(String tabId) {
        int selectPage = tabHost.getCurrentTab();
        mPager.setCurrentItem(selectPage);
    }
}
