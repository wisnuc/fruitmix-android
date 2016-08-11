package com.winsun.fruitmix.component;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.LocalCache;

import java.util.Map;

/**
 * Created by Administrator on 2016/4/27.
 */
public class BigLittleImageView extends ImageView {

    Map<String, Object> data;
    int width, height;
    public Bitmap bigPic = null;

    public Handler handler = null;

    public static BigLittleImageView HotView = null;
    public static BigLittleImageView HotView2 = null;

    public BigLittleImageView(Context context) {
        super(context);
    }

    public BigLittleImageView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
    }

    public BigLittleImageView(Context context, int w, int h) {
        super(context);
        this.setPadding(0, 0, 0, 0);
    }

    public void setData(Map<String, Object> data_, int width_, int height_) {
        data = data_;
        width = width_;
        height = height_;
        //Log.d("winsun", "small1 "+width+" "+height);
    }

    public void loadSmallPic() {
        //Log.d("winsun", "small"+width+" "+height);

        if (data.get("cacheType").equals("local")) {  // local bitmap path
            String path = (String) data.get("thumb");
            this.setTag(path);
            LocalCache.LoadLocalBitmapThumb(path, width, height, this);
        } else if (data.get("cacheType").equals("nas")) {
            String path = (String) data.get("resHash");
            this.setTag(path);
            LocalCache.LoadRemoteBitmapThumb(path, width, height, this);
        }
    }

    public void loadBigPic() {
        /*if(HotView!=null) {
            HotView.bigPic.recycle();
            Log.d("winsun", "Recycled "+HotView.bigPic.isRecycled());
            HotView.bigPic=null;
            //HotView.loadSmallPic();
            //HotView.setImageDrawable(null);
            HotView.setImageResource(R.drawable.yesshou);
            //HotView.invalidate();
            HotView=null;
            System.gc();
        }*/
        //loadSmallPic();
        if (data.get("cacheType").equals("local")) {
            LocalCache.LoadLocalBitmap((String) data.get("thumb"), this);
        } else if (data.get("cacheType").equals("nas")) {
            LocalCache.LoadRemoteBitmap((String) (data.get("resHash")), this);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bmp) {
        //bigPic=bmp;
/*
        if(HotView!=null) {
            HotView.bigPic.recycle();
            HotView.bigPic=null;
            HotView.setImageResource(R.drawable.yesshou);
            Log.d("winsun", "Recycled");
        }

*/
        super.setImageBitmap(bmp);
        /*
        bigPic=bmp;
        HotView=this;
        */
    }

    @Override
    public void finalize() throws Throwable {
        if (bigPic != null) {
            bigPic.recycle();
            bigPic = null;
            HotView = null;
        }
        super.finalize();
    }

    public void recycleBigPic(){
        if (bigPic != null) {
            bigPic.recycle();
            bigPic = null;
            HotView = null;
        }
    }

}
