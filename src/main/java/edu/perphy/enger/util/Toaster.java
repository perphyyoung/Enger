package edu.perphy.enger.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author MaTianyu
 * @date 2014-07-31
 */
public class Toaster {

    private Toast mToast;
    private Context context;

    public Toaster(Context context) {
        this.context = context.getApplicationContext();
    }

    public void showCenterToast(String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @SuppressLint("ShowToast")
    public Toast getSingletonToast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        return mToast;
    }

    @SuppressLint("ShowToast")
    public Toast getSingletonToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        return mToast;
    }

    @SuppressLint("ShowToast")
    public Toast getSingleLongToast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context, resId, Toast.LENGTH_LONG);
        } else {
            mToast.setText(resId);
        }
        return mToast;
    }

    @SuppressLint("ShowToast")
    public Toast getSingleLongToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
        }
        return mToast;
    }

    public Toast getToast(int resId) {
        return Toast.makeText(context, resId, Toast.LENGTH_SHORT);
    }

    public Toast getToast(String text) {
        return Toast.makeText(context, text, Toast.LENGTH_SHORT);
    }

    public Toast getLongToast(int resId) {
        return Toast.makeText(context, resId, Toast.LENGTH_LONG);
    }

    public Toast getLongToast(String text) {
        return Toast.makeText(context, text, Toast.LENGTH_LONG);
    }

    public void showSingletonToast(int resId) {
        getSingletonToast(resId).show();
    }


    public void showSingletonToast(String text) {
        getSingletonToast(text).show();
    }

    public void showSingleLongToast(int resId) {
        getSingleLongToast(resId).show();
    }


    public void showSingleLongToast(String text) {
        getSingleLongToast(text).show();
    }

    public void showToast(int resId) {
        getToast(resId).show();
    }

    public void showToast(String text) {
        getToast(text).show();
    }

    public void showLongToast(int resId) {
        getLongToast(resId).show();
    }

    public void showLongToast(String text) {
        getLongToast(text).show();
    }

}
