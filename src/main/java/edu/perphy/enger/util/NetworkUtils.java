package edu.perphy.enger.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by perphy on 2016/4/4 0004.
 * 检查网络连接状态
 */
public class NetworkUtils {

    /**
     * 检查移动网络是否已连接
     */
    public static boolean isMobileEnabled(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 检查WIFI网络是否已连接
     */
    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
