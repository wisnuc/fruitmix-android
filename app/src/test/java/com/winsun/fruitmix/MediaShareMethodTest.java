package com.winsun.fruitmix;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.mock.MockApplication;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

/**
 * Created by Administrator on 2016/10/19.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23,application = MockApplication.class)
public class MediaShareMethodTest {

    @Test
    public void createStringOperateValuesInMediaShareTest(){
        String op = "add";

        MediaShare mediashare = new MediaShare();
        mediashare.addViewer("5da92303-33a1-4f79-8d8f-a7b6becde6c3");
        mediashare.addViewer("e5f23cb9-1852-475d-937d-162d2554e22c");
        mediashare.addViewer("1f4faecf-1bb5-4ff1-ab41-bd44a0cd0809");
        mediashare.addViewer("3908afee-0818-4a3e-b327-76c2578ecb80");
        mediashare.addViewer("831b5cc9-6a14-4a4f-b1b6-666c5b282783");
        mediashare.addViewer("9d4873e2-c0b7-4541-b535-87d5fd637f70");


        String requestData = mediashare.createStringOperateViewersInMediaShare(op);

        Assert.assertEquals(requestData,"{\"op\":\"add\",\"path\":\"viewers\",\"value\":[" +
                "\"5da92303-33a1-4f79-8d8f-a7b6becde6c3\"," +
                "\"e5f23cb9-1852-475d-937d-162d2554e22c\"," +
                "\"1f4faecf-1bb5-4ff1-ab41-bd44a0cd0809\"," +
                "\"3908afee-0818-4a3e-b327-76c2578ecb80\"," +
                "\"831b5cc9-6a14-4a4f-b1b6-666c5b282783\"," +
                "\"9d4873e2-c0b7-4541-b535-87d5fd637f70\"" +
                "]}");

    }

    @Test
    public void createStringReplaceTitleTextAboutMediaShareTest(){

        MediaShare mediashare = new MediaShare();

        mediashare.setTitle("title");
        mediashare.setDesc("desc");

        String requestData = mediashare.createStringReplaceTitleTextAboutMediaShare();

        Assert.assertEquals(requestData,"{\"op\":\"replace\",\"path\":\"album\",\"value\":{\"title\":\"title\",\"text\":\"desc\"}}");
    }

    @Test
    public void getDifferentMediaShareContentInThisMediaShareTest(){

        MediaShare mediaShare1 = new MediaShare();
        MediaShare mediaShare2 = new MediaShare();

        MediaShareContent mediaShareContent = new MediaShareContent();
        mediaShareContent.setKey("65fac2a5c61906c851727076cc25d2da54d0a908ec492b8307be595f83bb7705");
        mediaShare1.addMediaShareContent(mediaShareContent);

        mediaShareContent = new MediaShareContent();
        mediaShareContent.setKey("ceeb92546f72b949f629995edeadf64ef5a4cf28aa3db451f3d82ed233e3ea16");
        mediaShare1.addMediaShareContent(mediaShareContent);

        mediaShareContent = new MediaShareContent();
        mediaShareContent.setKey("ceeb92546f72b949f629995edeadf64ef5a4cf28aa3db451f3d82ed233e3ea16");
        mediaShare2.addMediaShareContent(mediaShareContent);

        mediaShareContent = new MediaShareContent();
        mediaShareContent.setKey("4a12da6e30c4281518ad4551bce953f45a653862e4d1b8849ca73bfc52d19fb9");
        mediaShare2.addMediaShareContent(mediaShareContent);

        List<MediaShareContent> diff1 = mediaShare1.getDifferentMediaShareContentInCurrentMediaShare(mediaShare2);
        mediaShareContent = diff1.get(0);
        Assert.assertEquals(mediaShareContent.getKey(),"65fac2a5c61906c851727076cc25d2da54d0a908ec492b8307be595f83bb7705");

        List<MediaShareContent> diff2 = mediaShare2.getDifferentMediaShareContentInCurrentMediaShare(mediaShare1);
        mediaShareContent = diff2.get(0);
        Assert.assertEquals(mediaShareContent.getKey(),"4a12da6e30c4281518ad4551bce953f45a653862e4d1b8849ca73bfc52d19fb9");

    }

}
