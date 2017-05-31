package com.winsun.fruitmix.anim;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.transition.ArcMotion;
import android.transition.PatternPathMotion;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by Administrator on 2017/4/26.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class EnterPatternPathMotion extends PatternPathMotion {

    public static final String TAG = EnterPatternPathMotion.class.getSimpleName();

    public EnterPatternPathMotion(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static boolean mLeftMotion = true;

    private static boolean mRightMotion = true;

    public static void setMotion(boolean leftMotion, boolean rightMotion) {

        mLeftMotion = leftMotion;

        mRightMotion = rightMotion;

    }

    @Override
    public Path getPath(float startX, float startY, float endX, float endY) {

        Path path = new Path();

        path.moveTo(0, 0);

        Log.d(TAG, "getPath: leftMotion: " + mLeftMotion + " rightMotion: " + mRightMotion);

        if (mLeftMotion) {

            path.quadTo(0, 50, 100, 100);

        } else if (mRightMotion) {

            path.quadTo(50, 0, 100, 100);

        } else {

            path.lineTo(100, 100);

        }

        setPatternPath(path);

        return super.getPath(startX, startY, endX, endY);
    }
}
