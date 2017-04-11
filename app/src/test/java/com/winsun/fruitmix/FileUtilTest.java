package com.winsun.fruitmix;

import com.winsun.fruitmix.util.FileUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Administrator on 2017/2/14.
 */

public class FileUtilTest {

    @Test
    public void formatFileSizeTest(){

        String size = FileUtil.formatFileSize(32 * 1000 * 1024);

        Assert.fail(size);
    }

    @Test
    public void f(){

        float percent = 400 * 100 / 7000;

        String percentText = (int) percent + "%";

        Assert.fail(percentText);
    }

}
