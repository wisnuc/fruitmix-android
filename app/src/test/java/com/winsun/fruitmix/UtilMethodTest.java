package com.winsun.fruitmix;

import android.util.Log;

import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.LocalCache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by Administrator on 2016/10/19.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class UtilMethodTest {

    public static final String TAG = UtilMethodTest.class.getSimpleName();

    @Test
    public void createStringOperateValuesInMediaShareTest(){
        String op = "add";

        MediaShare mediashare = new MediaShare();

        String requestData = mediashare.createStringOperateViewersInMediaShare(op);

        Log.i(TAG, "createStringOperateValuesInMediaShareTest: requestData" + requestData);

    }

    public void createStringReplaceTitleTextAboutMediaShareTest(){

        MediaShare mediashare = new MediaShare();

        String requestData = mediashare.createStringReplaceTitleTextAboutMediaShare();

        Log.i(TAG, "createStringReplaceTitleTextAboutMediaShareTest: requestData" + requestData);
    }

}
