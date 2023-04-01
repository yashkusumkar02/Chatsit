package com.chatsit.chat.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.chatsit.chat.R;
import com.chatsit.chat.auth.GenerateOTPActivity;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager screenPager;
    IntroViewPagerAdapter introViewPagerAdapter;
    TabLayout tabIndicator;
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //ini view
        tabIndicator = findViewById(R.id.indicator);

        //Skip
        findViewById(R.id.skip).setOnClickListener(view -> loadLastScreen());

        //Fill list screen
        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem(getString(R.string.one_des),getString(R.string.one_title),R.drawable.one));
        mList.add(new ScreenItem(getString(R.string.two_des),getString(R.string.two_title),R.drawable.two));
        mList.add(new ScreenItem(getString(R.string.three_des),getString(R.string.three_title),R.drawable.four));
        mList.add(new ScreenItem(getString(R.string.three_des),getString(R.string.three_title),R.drawable.four));

        //Setup viewpager
        screenPager = findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this,mList);
        screenPager.setAdapter(introViewPagerAdapter);

        //setup tabLayout with pagerView
        tabIndicator.setupWithViewPager(screenPager);

        //Next btn click
        findViewById(R.id.next).setOnClickListener(view -> {

            position = screenPager.getCurrentItem();
            if (position < mList.size()){
                position++;
                screenPager.setCurrentItem(position);
            }
            //When reached last
            if (position == mList.size()) {

                loadLastScreen();

            }
        });

        //tabLayout last
        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == mList.size()-1){
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void loadLastScreen() {
        Intent intent = new Intent(getApplicationContext(), GenerateOTPActivity.class );
        startActivity(intent);
        finish();
    }

}