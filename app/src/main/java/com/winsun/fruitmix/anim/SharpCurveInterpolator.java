package com.winsun.fruitmix.anim;

import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;

/**
 * Created by Administrator on 2017/5/31.
 */

public class SharpCurveInterpolator {

    public static Interpolator getSharpCurveInterpolator(){
        return PathInterpolatorCompat.create(0.4f, 0, 0.6f, 1);
    }

}
