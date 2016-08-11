package com.winsun.fruitmix.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Administrator on 2016/4/26.
 */
public class StatedImageView extends ImageView {
    public String src=null;

    public StatedImageView (Context context){
        super(context);
    }

    public StatedImageView(Context context,AttributeSet paramAttributeSet){
        super(context,paramAttributeSet);
    }

    public StatedImageView(Context context,int w,int h) {
        super(context);
        this.setPadding(0, 0, 0, 0);
    }
}
