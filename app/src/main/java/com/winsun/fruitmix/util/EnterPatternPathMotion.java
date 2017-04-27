package com.winsun.fruitmix.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.transition.PatternPathMotion;
import android.util.AttributeSet;

/**
 * Created by Administrator on 2017/4/26.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class EnterPatternPathMotion extends PatternPathMotion {

    public EnterPatternPathMotion(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static boolean mLeftMotion = true;

    public static void setMotion(boolean leftMotion) {

        mLeftMotion = leftMotion;

    }

    @Override
    public Path getPath(float startX, float startY, float endX, float endY) {

        Path path = new Path();

        if (mLeftMotion) {

            path.moveTo(0, 0);
            path.quadTo(20, 80, 100, 100);

        } else {

            path.moveTo(0, 0);
            path.quadTo(80, 20, 100, 100);

        }

        setPatternPath(path);

        return super.getPath(startX, startY, endX, endY);
    }
}
