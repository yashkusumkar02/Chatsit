package com.chatsit.chat.welcome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.chatsit.chat.R;

import java.util.List;
import java.util.Objects;

public class IntroViewPagerAdapter extends PagerAdapter {

    final Context mContext;
    final List<ScreenItem> mListScreen;


    public IntroViewPagerAdapter(Context mContext, List<ScreenItem> mListScreen) {
        this.mContext = mContext;
        this.mListScreen = mListScreen;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View layoutScreen = Objects.requireNonNull(inflater).inflate(R.layout.layout_intro_screen,null);

        ImageView logo = layoutScreen.findViewById(R.id.logo);
        TextView title = layoutScreen.findViewById(R.id.title);
        TextView dec = layoutScreen.findViewById(R.id.dec);

        dec.setText(mListScreen.get(position).getDescription());
        title.setText(mListScreen.get(position).getTitle());
        logo.setImageResource(mListScreen.get(position).getScreenImg());

        container.addView(layoutScreen);

        return layoutScreen;

    }

    @Override
    public int getCount() {
        return mListScreen.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
