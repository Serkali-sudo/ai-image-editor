package com.serhat.aieditor;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Utils {
    public static boolean DEBUG = true;

    public static void hideKeyboard(Activity appCompatActivity) {
        View view = appCompatActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    appCompatActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        return Math.round(px / density);
    }

//    public static int px2dp(Context context, int px) {
//        Resources r = context.getResources();
//        DisplayMetrics metrics = r.getDisplayMetrics();
//        float dp = TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_PX,
//                px,
//                metrics
//        );
//        return (int) dp;
//    }

    public static void hideKeyboard(Activity appCompatActivity, EditText editText) {
        InputMethodManager imm = (InputMethodManager)
                appCompatActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

    }


    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUG) {
            Log.w(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (DEBUG) {
            Log.v(tag, message);
        }
    }
}
