package com.winsun.fruitmix.mediaModule.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.NewAlbumPicChooseActivity;
import com.winsun.fruitmix.interfaces.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/19.
 */
public class AlbumList implements Page, IShowHideFragmentListener {

    public static final String TAG = AlbumList.class.getSimpleName();

    private Activity containerActivity;
    private View view;

    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.no_content_textview)
    TextView noContentTextView;

    private List<IPhotoListListener> mPhotoListListeners;

    public AlbumList(Activity activity_) {

        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity).inflate(R.layout.album_list, null);

        ButterKnife.bind(this, view);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerActivity.startActivity(new Intent(containerActivity, NewAlbumPicChooseActivity.class));
            }
        });

        noContentImageView.setImageResource(R.drawable.no_photo);

        noContentTextView.setText(containerActivity.getString(R.string.no_albums));

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
        //MobclickAgent.onPageStart("AlbumFragment");


    }

    @Override
    public void hide() {
        //MobclickAgent.onPageEnd("AlbumFragment");


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

