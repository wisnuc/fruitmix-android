package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.Fragment.NewPhotoList;
import com.winsun.fruitmix.Fragment.NewPhotoList2;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/5/9.
 */
public class NewAlbumPicChooseActivity extends Activity {

    ImageView ivBack;
    TextView tfOK;

    @BindView(R.id.main_framelayout)
    FrameLayout mMainFrameLayout;

    private NewPhotoList2 mNewPhotoList;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_activity_album_pic_choose);

        ButterKnife.bind(this);

        mContext = this;

        mNewPhotoList = new NewPhotoList2(this);

        mMainFrameLayout.addView(mNewPhotoList.getView());
        mNewPhotoList.setSelectMode(true);
        mNewPhotoList.refreshView();

        ivBack = (ImageView) findViewById(R.id.back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tfOK = (TextView) findViewById(R.id.ok);
        tfOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectUIDString = getSelectedUIDString();
                if (selectUIDString.equals("")) {
                    Toast.makeText(mContext, getString(R.string.select_nothing), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = getIntent();

                if (intent.getBooleanExtra(Util.EDIT_PHOTO, false)) {

                    getIntent().putExtra("selectedUIDStr", selectUIDString);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    intent = new Intent();
                    intent.setClass(NewAlbumPicChooseActivity.this, CreateAlbumActivity.class);
                    intent.putExtra("selectedUIDStr", selectUIDString);
                    startActivityForResult(intent, Util.KEY_CREATE_ALBUM_REQUEST_CODE);
                }

            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Util.KEY_CREATE_ALBUM_REQUEST_CODE) {
            setResult(resultCode);
            finish();
        }
    }


    public String getSelectedUIDString() {
        return mNewPhotoList.getSelectedUIDString();
    }

}
