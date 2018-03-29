package com.winsun.fruitmix.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/3/28.
 */

public class ToastUtil {

    private static Toast toast;

    public static void showToast(Context context, String message) {

        if (toast == null)
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        else
            toast.setText(message);

        toast.show();
    }

}
