package com.winsun.fruitmix.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.AlbumContentContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.presenter.AlbumContentPresentImpl;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/28.
 */
public class AlbumContentActivity extends BaseActivity implements AlbumContentContract.AlbumContentView {

    public static final String TAG = AlbumContentActivity.class.getSimpleName();

    @BindView(R.id.album_content_recycler_view)
    RecyclerView albumContentRecyclerView;
    @BindView(R.id.back)
    ImageView ivBack;
    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;

    private PicGridViewAdapter mPicGridViewAdapter;

    private MenuItem mPrivatePublicMenu;

    private Context mContext;

    private AlbumContentContract.AlbumContentPresenter mPresenter;

    private boolean mShowMenu;

    private boolean mShowCommentBtn = false;

    public static final int SPAN_COUNT = 2;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            mPresenter.onMapSharedElements(names, sharedElements);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = this;

        setExitSharedElementCallback(sharedElementCallback);

        String mediaShareUUID = getIntent().getStringExtra(Util.KEY_MEDIA_SHARE_UUID);

        mShowMenu = getIntent().getBooleanExtra(Util.NEED_SHOW_MENU, true);
        mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

        setContentView(R.layout.activity_album_pic_content);

        ButterKnife.bind(this);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleBackEvent();
            }
        });

        mPicGridViewAdapter = new PicGridViewAdapter();

        albumContentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        albumContentRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(SPAN_COUNT,StaggeredGridLayoutManager.VERTICAL));
        albumContentRecyclerView.setAdapter(mPicGridViewAdapter);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mPresenter = new AlbumContentPresentImpl(Injection.injectDataRepository(mContext), mediaShareUUID);
        mPresenter.attachView(this);
        mPresenter.setMediaShareTitle();

        mPresenter.loadMediaInMediaShare();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        mPresenter.detachView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        mPresenter.handleBackEvent();
    }


    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        mPresenter.onActivityReenter(resultCode, data);

    }

    @Override
    public void showLoadingUI() {
        super.showLoadingUI();

        loadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissLoadingUI() {
        super.dismissLoadingUI();

        loadingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoContentUI() {
        super.showNoContentUI();

        noContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNoContentUI() {
        super.dismissNoContentUI();

        noContentLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContentUI() {
        super.showContentUI();

        albumContentRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        super.dismissContentUI();

        albumContentRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showPhotoSlider(int position, View sharedElement, String sharedElementName) {

        OriginalMediaActivity.setMediaList(mPresenter.getMedias());

        Intent intent = new Intent();
        intent.putExtra(Util.INITIAL_PHOTO_POSITION, position);
        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, mShowCommentBtn);
        intent.setClass(this, OriginalMediaActivity.class);

        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedElement, sharedElementName);
        startActivity(intent, optionsCompat.toBundle());
    }

    @Override
    public void setTitle(String title) {
        mTitleTextView.setText(title);
    }

    @Override
    public void showAlbumContent(List<Media> medias) {

        mPicGridViewAdapter.setMedias(medias);
        mPicGridViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void showNoOperationPermission() {
        Toast.makeText(mContext, getString(R.string.no_operate_media_share_permission), Toast.LENGTH_SHORT).show();
    }

    @Override
    public View findViewWithTag(String tag) {
        return albumContentRecyclerView.findViewWithTag(tag);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        albumContentRecyclerView.smoothScrollToPosition(position);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showUploadingToast() {
        Toast.makeText(mContext, getString(R.string.local_media_share_uploading), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOperationResultToast(OperationResult result) {
        Toast.makeText(mContext, result.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPrivatePublicMenuItemTitle(int titleResID) {
        mPrivatePublicMenu.setTitle(getString(titleResID));
    }

    class PicGridViewAdapter extends RecyclerView.Adapter<AlbumContentViewHolder> {

        private List<Media> medias;

        PicGridViewAdapter() {
            medias = new ArrayList<>();
        }

        @Override
        public AlbumContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.photo_list_cell, parent, false);

            return new AlbumContentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AlbumContentViewHolder holder, int position) {
            holder.refreshView(position, medias.get(position));
        }

        public void setMedias(List<Media> medias) {
            this.medias.clear();
            this.medias.addAll(medias);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public int getItemCount() {
            if (medias == null) return 0;
            return medias.size();
        }

    }


    class AlbumContentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.mainPic)
        NetworkImageView networkImageView;

        public AlbumContentViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void refreshView(final int position, final Media media) {

            mPresenter.loadMediaToView(mContext, media, networkImageView);

            networkImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String sharedElementName = media.getKey();
                    ViewCompat.setTransitionName(networkImageView, sharedElementName);
                    showPhotoSlider(position, networkImageView, media.getKey());
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (mShowMenu) {

            getMenuInflater().inflate(R.menu.album_menu, menu);

            mPrivatePublicMenu = menu.findItem(R.id.set_private_public);

            mPresenter.showMenuItemPrivateOrPublic();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        mPresenter.preTreatItemOnOptionsItemSelected();

        Intent intent;
        switch (item.getItemId()) {
            case R.id.setting_album:
                intent = new Intent(this, ModifyAlbumActivity.class);
                intent.putExtra(Util.MEDIASHARE_UUID, mPresenter.getMediaShareUUID());
                startActivityForResult(intent, Util.KEY_MODIFY_ALBUM_REQUEST_CODE);
                break;
            case R.id.edit_photo:
                intent = new Intent(this, EditPhotoActivity.class);
                intent.putExtra(Util.KEY_MEDIA_SHARE_UUID, mPresenter.getMediaShareUUID());
                startActivityForResult(intent, Util.KEY_EDIT_PHOTO_REQUEST_CODE);
                break;
            case R.id.set_private_public:
                mPresenter.toggleAlbumPublicState();
                break;
            case R.id.delete_album:
                deleteCurrentAlbum();
                break;
            default:
        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mPresenter.handleOnActivityResult(requestCode, resultCode, data);

    }

    private void deleteCurrentAlbum() {

        new AlertDialog.Builder(mContext).setMessage(getString(R.string.confirm_delete))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mPresenter.deleteCurrentAlbum();

                    }
                }).setNegativeButton(getString(R.string.cancel), null).create().show();

    }


}
