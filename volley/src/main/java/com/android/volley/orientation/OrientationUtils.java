package com.android.volley.orientation;

import android.graphics.Matrix;

/**
 * Created by Administrator on 2016/10/21.
 */

public class OrientationUtils {
    public static Matrix rotateBitmap(Matrix matrix, int degrees, float px, float py) {

        matrix.setRotate(degrees, px, py);

        return matrix;
    }

    public static Matrix convertHorizontalBitmap(Matrix matrix) {

        matrix.postScale(-1, 1);

        return matrix;
    }

    public static Matrix convertVerticalBitmap(Matrix matrix) {

        matrix.postScale(1, -1);

        return matrix;
    }
}
