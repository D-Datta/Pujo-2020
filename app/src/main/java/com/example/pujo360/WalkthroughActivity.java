package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pujo360.preferences.IntroPref;

import co.gofynd.gravityview.GravityView;

public class WalkthroughActivity extends AppCompatActivity {

    private TextView tvNext;
    private TextView tvSkip;
    private ViewPager viewPager;
    private LinearLayout layoutDots, intro_oneLL;
    private IntroPref introPref;
    private int[] layouts;
    private TextView[] dots;
    private MyViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);


        introPref = new IntroPref(this);
        if(introPref.isFirstTimeLaunch()){
            launchHomeScreen();
            finish();
        }

        tvNext = findViewById(R.id.tvNext);
        tvSkip = findViewById(R.id.tvSkip);
        viewPager = findViewById(R.id.viewPager);
        layoutDots = findViewById(R.id.layoutDots);

        layouts = new int[]{
                R.layout.intro_one,
                R.layout.intro_two,
                R.layout.intro_three,
                R.layout.intro_four
        };




        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = WalkthroughActivity.this.getItem(+1);
                if (current < layouts.length) {
                    viewPager.setCurrentItem(current);
                } else {
                    WalkthroughActivity.this.launchHomeScreen();
                }
            }
        });

        viewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);

        addBottomDots(0);

    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener(){

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            addBottomDots(position);

            if(position == layouts.length-1){
                tvNext.setText("Start");
            }
            else{
                tvNext.setText("Next");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void addBottomDots(int currentPage){
        dots = new TextView[layouts.length];
        int[] activeColors = getResources().getIntArray(R.array.active);
        int[] inactiveColors = getResources().getIntArray(R.array.inactive);
        layoutDots.removeAllViews();

        for(int i=0;i<dots.length;i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(50);
            dots[i].setTextColor(inactiveColors[currentPage]);
            layoutDots.addView(dots[i]);
        }

        if(dots.length>0){
            dots[currentPage].setTextColor(activeColors[currentPage]);
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {

        LayoutInflater layoutInflater;
        public MyViewPagerAdapter(){

        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position],container,false);

            ImageView img;
            GravityView gravityView;
            boolean isSupported = false;
            ///////////////SET IMAGE BITMAP/////////////////////
            if(position == 0) {
                ImageView lighting_image = view.findViewById(R.id.lighting);
                ImageView durga_image = view.findViewById(R.id.ma_durga);

                Display display = getWindowManager().getDefaultDisplay();
                int displayWidth = display.getWidth();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeResource(getResources(), R.drawable.fairy_lights, options);
                BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);

                int width = options.outWidth;
                if (width > displayWidth) {
                    options.inSampleSize = Math.round((float) width / (float) displayWidth);
                }
                options.inJustDecodeBounds = false;

                Bitmap scaledBitmap1 =  BitmapFactory.decodeResource(getResources(), R.drawable.fairy_lights, options);
                lighting_image.setImageBitmap(scaledBitmap1);

                Bitmap scaledBitmap2 =  BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);
                durga_image.setImageBitmap(scaledBitmap2);
            }
            ///////////////SET IMAGE BITMAP/////////////////////

            if(position==1){

            }

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    private int getItem(int i)
    {
        return viewPager.getCurrentItem()+1;
    }

    private void launchHomeScreen() {
        introPref.setIsFirstTimeLaunch(false);
        startActivity(new Intent(WalkthroughActivity.this,LoginActivity.class));
        finish();
    }

}
