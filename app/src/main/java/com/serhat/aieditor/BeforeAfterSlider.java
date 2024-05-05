package com.serhat.aieditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

public class BeforeAfterSlider extends ConstraintLayout {
    public BeforeAfterSlider(Context context) {
        super(context);
        init(null);
    }

    public BeforeAfterSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BeforeAfterSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setFore(Bitmap bitmap) {
        ImageView foreground_image = findViewById(R.id.foreground_image);
        foreground_image.setImageBitmap(bitmap);
    }

    public void setBack(Bitmap bitmap) {
        ImageView background_image = findViewById(R.id.background_image);
        background_image.setImageBitmap(bitmap);
    }


//    public void setTestImages() {
//        ImageView foreground_image = findViewById(R.id.foreground_image);
//        foreground_image.setImageResource(R.drawable.test1);
//        ImageView background_image = findViewById(R.id.background_image);
//        background_image.setImageResource(R.drawable.test2);
//    }



    private void init(AttributeSet attrs) {
        View.inflate(getContext(), R.layout.before_after_slider, this);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.BeforeAfterSlider);
        try {
//            int drawableBackground = ta.getResourceId(R.styleable.ImageCompareSlider_background_image, 0);
//            if (drawableBackground != 0) {
//                Drawable drawable = AppCompatResources.getDrawable(getContext(), drawableBackground);
//                ImageView background_image = findViewById(R.id.background_image);
//                background_image.setImageDrawable(drawable);
//            }
//            int drawableForeground = ta.getResourceId(R.styleable.ImageCompareSlider_foreground_image, 0);
//            if (drawableForeground != 0) {
//                Drawable drawable = AppCompatResources.getDrawable(getContext(), drawableForeground);
//                ImageView foreground_image = findViewById(R.id.foreground_image);
//                foreground_image.setImageDrawable(drawable);
//            }
            int drawableSliderIcon = ta.getResourceId(R.styleable.BeforeAfterSlider_slider_icon, 0);
            if (drawableSliderIcon != 0) {
                Drawable drawable = AppCompatResources.getDrawable(getContext(), drawableSliderIcon);
                ImageView slider_image = findViewById(R.id.slider_image);
                slider_image.setImageDrawable(drawable);
                slider_image.setBackgroundResource(R.drawable.drag_shape);
            }
        } finally {
            ta.recycle();
        }

        SeekBar sbImageSeek = findViewById(R.id.sbImageSeek);
        sbImageSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setImageWidth(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        View background_image = findViewById(R.id.background_image);
        View foreground_image = findViewById(R.id.foreground_image);
        View slider_bar = findViewById(R.id.slider_bar);

        background_image.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = foreground_image.getLayoutParams();
                layoutParams.height = background_image.getHeight();
                layoutParams.width = background_image.getWidth();
                foreground_image.setLayoutParams(layoutParams);

                sbImageSeek.setMax(background_image.getWidth());
                sbImageSeek.setProgress((sbImageSeek.getMax() / 2));

                ViewGroup.LayoutParams sliderBarLayoutParams = slider_bar.getLayoutParams();
                sliderBarLayoutParams.height = background_image.getHeight();
                slider_bar.setLayoutParams(sliderBarLayoutParams);
            }
        });
    }

    private void setImageWidth(int progress) {
        if (progress <= 0)
            return;

        View target = findViewById(R.id.target);
        ViewGroup.LayoutParams lp = target.getLayoutParams();
        lp.width = progress;
        target.setLayoutParams(lp);
    }
}