package com.android.volley.orientation;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Administrator on 2016/10/21.
 */

public class Orientation4Operation implements OrientationOperation {

    Orientation4Operation(){}

    @Override
    public Bitmap handleOrientationOperate(Bitmap originalBitmap) {

        Matrix matrix = new Matrix();

        matrix = OrientationUtils.convertVerticalBitmap(matrix);

        matrix = OrientationUtils.rotateBitmap(matrix,180,originalBitmap.getWidth() / 2,originalBitmap.getHeight() / 2);
        return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

    }
}
