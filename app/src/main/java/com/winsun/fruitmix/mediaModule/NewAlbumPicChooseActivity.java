package com.winsun.fruitmix.mediaModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/5/9.
 */
public class NewAlbumPicChooseActivity extends Activity {

    @BindView(R.id.back)
    ImageView ivBack;
    @BindView(R.id.ok)
    TextView tfOK;

    @BindView(R.id.main_framelayout)
    FrameLayout mMainFrameLayout;

    private NewPhotoList mNewPhotoList;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_activity_album_pic_choose);

        ButterKnife.bind(this);

        mContext = this;

        mNewPhotoList = new NewPhotoList(this);

        List<String> alreadySelectedImageUUIDArrayList = getIntent().getStringArrayListExtra(Util.KEY_ALREADY_SELECTED_IMAGE_UUID_ARRAYLIST);

        mMainFrameLayout.addView(mNewPhotoList.getView());
        mNewPhotoList.setSelectMode(true);
        mNewPhotoList.setAlreadySelectedImageUUIDArrayList(alreadySelectedImageUUIDArrayList);
        mNewPhotoList.refreshView();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tfOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> selectImageUUIDs = getSelectedImageUUIDs();
                if (selectImageUUIDs.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.select_nothing), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = getIntent();

                String[] selectedImageUUIDList = selectImageUUIDs.toArray(new String[selectImageUUIDs.size()]);

                if (intent.getBooleanExtra(Util.EDIT_PHOTO, false)) {

                    getIntent().putExtra(Util.KEY_NEW_SELECTED_IMAGE_UUID_ARRAY,selectedImageUUIDList);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    intent = new Intent();
                    intent.setClass(NewAlbumPicChooseActivity.this, CreateAlbumActivity.class);
                    intent.putExtra(Util.KEY_NEW_SELECTED_IMAGE_UUID_ARRAY, selectedImageUUIDList);
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


    public List<String> getSelectedImageUUIDs() {
        return mNewPhotoList.getSelectedImageUUIDs();
    }

}
