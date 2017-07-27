package com.winsun.fruitmix;

import com.winsun.fruitmix.util.FileUtil;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/2/14.
 */

public class FileUtilTest {



    @Ignore
    public void formatFileSizeTest() {

        String size = FileUtil.formatFileSize(32 * 1000 * 1024);

        Assert.fail(size);
    }

    @Ignore
    public void percentTest() {

        float percent = 400 * 100 / 7000;

        String percentText = (int) percent + "%";

        Assert.fail(percentText);
    }

    @Test
    public void getFileTypeResIDTest() {

        String fileName = "a.pdf";

        int resID = FileUtil.getFileTypeResID(fileName);

        assertEquals(R.drawable.pdf, resID);

        fileName = "a.xls";

        resID = FileUtil.getFileTypeResID(fileName);

        assertEquals(R.drawable.excel, resID);

        fileName = "a.doc";

        resID = FileUtil.getFileTypeResID(fileName);

        assertEquals(R.drawable.word, resID);

        fileName = "a.ppt";

        resID = FileUtil.getFileTypeResID(fileName);

        assertEquals(R.drawable.power_point, resID);

        fileName = "a.txt";

        resID = FileUtil.getFileTypeResID(fileName);

        assertEquals(R.drawable.txt, resID);
    }

}
