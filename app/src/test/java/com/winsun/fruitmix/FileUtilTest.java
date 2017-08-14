package com.winsun.fruitmix;

import com.winsun.fruitmix.util.FileUtil;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    @Ignore
    public void sortFile() {

        Comparator<String> fileNameComparator = new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {

                try {
                    lhs = new String(lhs.getBytes("GBK"), "Unicode");

                    rhs = new String(rhs.getBytes("GBK"), "Unicode");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return lhs.compareTo(rhs);

            }
        };

        List<String> fileNames = new ArrayList<>();

        fileNames.add("Alice");
        fileNames.add("bob");

        fileNames.add("测试");
        fileNames.add("刘华");
        fileNames.add("吴亮");
        fileNames.add("JackYang");

        Collections.sort(fileNames, fileNameComparator);

        assertEquals("Alice", fileNames.get(0));

        assertEquals(Collections.EMPTY_LIST, fileNames);

    }


}
