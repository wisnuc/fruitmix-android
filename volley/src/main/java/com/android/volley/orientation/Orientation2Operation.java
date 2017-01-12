package com.android.volley.orientation;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Administrator on 2016/10/21.
 */

public class Orientation2Operation implements OrientationOperation {

    Orientation2Operation() {
    }

    @Override
    public Bitmap handleOrientationOperate(Bitmap originalBitmap) {

        Matrix matrix = new Matrix();
        matrix = OrientationUtils.convertVerticalBitmap(matrix);
        return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

    }
}
