package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.winsun.fruitmix.component.BigLittleImageView;
import com.winsun.fruitmix.util.LocalCache;

import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/4/28.
 */
public class ImageZoomActivity extends Activity implements View.OnTouchListener {

    private static final String TAG = "PhotoViewer";
    public static final int RESULT_CODE_NOFOUND = 200;

    public boolean sQuit;

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    DisplayMetrics dm;
    BigLittleImageView imgView;
    Bitmap bitmap;

    /** 最小缩放比例*/
    float minScaleR = 1.0f;
    /** 最大缩放比例*/
    static final float MAX_SCALE = 10f;

    /** 初始状态*/
    static final int NONE = 0;
    /** 拖动*/
    static final int DRAG = 1;
    /** 缩放*/
    static final int ZOOM = 2;

    /** 当前模式*/
    int mode = NONE;

    PointF prev = new PointF();
    PointF mid = new PointF();
    float dist = 1f;

    Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final Map<String, Object> imgItem;
        super.onCreate(savedInstanceState);

        // 获取图片资源

        //bitmap = (Bitmap)LocalCache.TransActivityContainer.get("bigPic");//BitmapFactory.decodeResource(getResources(), R.drawable.yesshou);
        //LocalCache.TransActivityContainer.remove("bigPic");

        imgItem=(Map<String, Object>)LocalCache.TransActivityContainer.get("bigPic");

        setContentView(R.layout.activity_image_zoom);
        imgView = (BigLittleImageView) findViewById(R.id.imageView1);// 获取控件
        imgView.handler=new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                bitmap = imgView.bigPic;


                imgView.setOnTouchListener(ImageZoomActivity.this);// 设置触屏监听
                dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);// 获取分辨率
                minZoom();
                center();
                sQuit=true;
                imgView.setImageMatrix(matrix);

                if(getIntent().hasExtra("zoomIn")) {
                    matrix.postScale(2.0f, 2.0f, 200.0f, 200.0f);
                    imgView.setImageMatrix(matrix);
                }

            }
        };



        if(imgItem.get("cacheType").equals("local")) {
            LocalCache.LoadLocalBitmap(imgItem.get("thumb").toString(), imgView);
        }
        else {
            LocalCache.LoadRemoteBitmap(imgItem.get("resHash").toString(), imgView);
        }



        //imgView.setData(imgItem, 500, 500);

            /*
            if(pos==position) {
                iv.loadBigPic();
                lbDate.setText((String) activity.imgList.get(position).get("mtime"));
            }
            else iv.loadSmallPic();*/
        //imgView.loadBigPic();

/*
        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object...params) {

                try {
                    bitmap = BitmapFactory.decodeFile(imgItem.get("thumb").toString());
                    Log.d("winsun", "bmp "+imgItem.get("thumb").toString()+" "+bitmap);
                    return bitmap;
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                try {
                    imgView.setImageBitmap(bmp);
                } catch (NullPointerException e) { e.printStackTrace(); }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

*/
        //imgView.setImageBitmap(bitmap);// 填充控件

    }

    public void SureOnClick(View v)
    {

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * 触屏监听
     */
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 主点按下
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                prev.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            // 副点按下
            case MotionEvent.ACTION_POINTER_DOWN:
                dist = spacing(event);
                // 如果连续两点距离大于10，则判定为多点模式
                if (spacing(event) > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                //savedMatrix.set(matrix);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - prev.x, event.getY()
                            - prev.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float tScale = newDist / dist;
                        matrix.postScale(tScale, tScale, mid.x, mid.y);
                    }
                }
                break;
        }
        imgView.setImageMatrix(matrix);
        CheckView();
        return true;
    }

    /**
     * 限制最大最小缩放比例，自动居中
     */
    private void CheckView() {
        float p[] = new float[9];
        matrix.getValues(p);
        if (mode == ZOOM) {
            if (p[0] < minScaleR) {
                //Log.d("", "当前缩放级别:"+p[0]+",最小缩放级别:"+minScaleR);
                //if(sQuit) finish(); // quit if it is over mininized
                matrix.setScale(minScaleR, minScaleR);
                sQuit=true;
            }
            else sQuit=false;
            if (p[0] > MAX_SCALE) {
                //Log.d("", "当前缩放级别:"+p[0]+",最大缩放级别:"+MAX_SCALE);
                matrix.set(savedMatrix);
            }
        }
        else if (mode == NONE ) {
            if(sQuit) finish(); // quit if it is over mininized
        }
        center();
    }

    /**
     * 最小缩放比例，最大为100%
     */
    private void minZoom() {
        try {
            minScaleR = Math.min(
                    (float) dm.widthPixels / (float) bitmap.getWidth(),
                    (float) dm.heightPixels / (float) bitmap.getHeight());
            if (minScaleR < 1.0) {
                matrix.postScale(minScaleR, minScaleR);
            }
        } catch (java.lang.NullPointerException e) { e.printStackTrace();}
    }

    private void center() {
        center(true, true);
    }

    /**
     * 横向、纵向居中
     */
    protected void center(boolean horizontal, boolean vertical) {

        Matrix m = new Matrix();
        m.set(matrix);
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        m.mapRect(rect);

        float height = rect.height();
        float width = rect.width();

        float deltaX = 0, deltaY = 0;

        if (vertical) {
            // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
            int screenHeight = dm.heightPixels;
            if (height < screenHeight) {
                deltaY = (screenHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < screenHeight) {
                deltaY = imgView.getHeight() - rect.bottom;
            }
        }

        if (horizontal) {
            int screenWidth = dm.widthPixels;
            if (width < screenWidth) {
                deltaX = (screenWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < screenWidth) {
                deltaX = screenWidth - rect.right;
            }
        }
        matrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * 两点的中点
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }



}


