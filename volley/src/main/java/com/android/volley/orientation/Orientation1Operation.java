package com.android.volley.orientation;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/10/21.
 */

public class Orientation1Operation implements OrientationOperation {

    Orientation1Operation(){}

    @Override
    public Bitmap handleOrientationOperate(Bitmap originalBitmap) {
        return originalBitmap;
    }
}
