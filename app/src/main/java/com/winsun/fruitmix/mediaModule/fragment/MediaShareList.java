package com.winsun.fruitmix.mediaModule.fragment;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/19.
 */
public class MediaShareList implements Page, IShowHideFragmentListener {

    public static final String TAG = MediaShareList.class.getSimpleName();

    private Activity containerActivity;
    private View view;

    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;
    @BindView(R.id.share_list_framelayout)
    FrameLayout mShareListFrameLayout;

    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.no_content_textview)
    TextView noContentTextView;

    private List<IPhotoListListener> mPhotoListListeners;

    public MediaShareList(Activity activity_) {
        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(R.layout.share_list2, null);

        ButterKnife.bind(this, view);

        noContentImageView.setImageResource(R.drawable.no_photo);

        noContentTextView.setText(containerActivity.getString(R.string.no_media_shares));

        mPhotoListListeners = new ArrayList<>();

    }

    public void addPhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.add(listListener);
    }

    public void removePhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.remove(listListener);
    }


    @Override
    public void show() {
        //MobclickAgent.onPageStart("MediaShareFragment");
    }

    @Override
    public void hide() {
        //MobclickAgent.onPageEnd("MediaShareFragment");
    }

    public View getView() {
        return view;
    }

    @Override
    public void refreshView() {

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

    }

    @Override
    public void onDestroy() {

    }


}

