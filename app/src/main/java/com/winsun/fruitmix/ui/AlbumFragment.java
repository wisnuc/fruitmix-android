package com.winsun.fruitmix.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.AlbumFragmentContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.presenter.AlbumFragmentPresenterImpl;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/19.
 */
public class AlbumFragment implements AlbumFragmentContract.AlbumFragmentView {

    public static final String TAG = AlbumFragment.class.getSimpleName();

    private Activity containerActivity;
    private View view;

    @BindView(R.id.add_album)
    FloatingActionButton ivAdd;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;
    @BindView(R.id.mainList)
    ListView mainListView;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.album_balloon)
    ImageView mAlbumBalloon;

    private SwipeLayout lastSwipeLayout;

    private AlbumListAdapter mAdapter;

    private AlbumFragmentContract.AlbumFragmentPresenter mPresenter;

    private ProgressDialog mDialog;

    public AlbumFragment(Activity activity_) {

        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity).inflate(R.layout.album_list, null);

        ButterKnife.bind(this, view);

        noContentImageView.setImageResource(R.drawable.no_photo);

        mAdapter = new AlbumListAdapter();
        mainListView.setAdapter(mAdapter);

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(containerActivity, NewPhotoSelectActivity.class);
                containerActivity.startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
            }
        });

        mPresenter = new AlbumFragmentPresenterImpl(Injection.injectDataRepository(containerActivity));
        mPresenter.attachView(this);

        mPresenter.refreshData();
    }

    public AlbumFragmentContract.AlbumFragmentPresenter getPresenter() {
        return mPresenter;
    }

    public View getView() {
        return view;
    }

    @Override
    public void setAlbumBalloonVisibility(int visibility) {
        mAlbumBalloon.setVisibility(visibility);
    }

    @Override
    public void setAlbumBalloonOnClickListener(View.OnClickListener listener) {
        mAlbumBalloon.setOnClickListener(listener);
    }

    @Override
    public void showAlbums(List<MediaShare> mediaShares) {
        mAdapter.setAlbumList(mediaShares);
        mAdapter.notifyDataSetChanged();

        mainListView.smoothScrollToPosition(0);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
    }

    @Override
    public void setAddAlbumBtnVisibility(int visibility) {
        ivAdd.setVisibility(visibility);
    }

    @Override
    public void showOperationResultToast(OperationResult result) {
        Toast.makeText(containerActivity, result.getResultMessage(containerActivity), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoOperatePermission() {
        Toast.makeText(containerActivity, containerActivity.getString(R.string.no_operate_media_share_permission), Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean isNetworkAlive() {
        return Util.getNetworkState(containerActivity);
    }

    @Override
    public void showNoNetwork() {
        Toast.makeText(containerActivity, containerActivity.getString(R.string.no_network), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void showLoadingUI() {
        mLoadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissLoadingUI() {
        mLoadingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoContentUI() {
        mNoContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNoContentUI() {
        mNoContentLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContentUI() {
        mainListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        mainListView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showDialog() {
        mDialog = ProgressDialog.show(containerActivity, containerActivity.getString(R.string.operating_title), null, true, false);
    }

    @Override
    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    public void hideSoftInput() {

    }

    private class AlbumListAdapter extends BaseSwipeAdapter {

        private List<MediaShare> mAlbumList;

        AlbumListAdapter() {
            mAlbumList = new ArrayList<>();
        }

        void setAlbumList(List<MediaShare> albumList) {
            mAlbumList.clear();
            mAlbumList.addAll(albumList);
        }

        /**
         * return the {@link SwipeLayout} resource id, int the view item.
         *
         * @param position
         * @return
         */
        @Override
        public int getSwipeLayoutResourceId(int position) {
            return R.id.swipe_layout;
        }

        /**
         * generate a new view item.
         * Never bind SwipeListener or fill values here, every item has a chance to fill value or bind
         * listeners in fillValues.
         * to fill it in {@code fillValues} method.
         *
         * @param position
         * @param parent
         * @return
         */
        @Override
        public View generateView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(containerActivity).inflate(R.layout.album_list_item, parent, false);

            AlbumListViewHolder viewHolder = new AlbumListViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        /**
         * fill values or bind listeners to the view.
         *
         * @param position
         * @param convertView
         */
        @Override
        public void fillValues(int position, View convertView) {

            View view;
            AlbumListViewHolder viewHolder;

            view = convertView;
            viewHolder = (AlbumListViewHolder) view.getTag();

            MediaShare currentItem = (MediaShare) getItem(position);
            viewHolder.refreshView(currentItem);

        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            if (mAlbumList == null) return 0;
            return mAlbumList.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return mAlbumList.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }
    }


    class AlbumListViewHolder {

        @BindView(R.id.mainBar)
        RelativeLayout mainBar;
        @BindView(R.id.mainPic)
        NetworkImageView ivMainPic;
        @BindView(R.id.lock)
        ImageView ivLock;
        @BindView(R.id.title)
        TextView lbTitle;
        @BindView(R.id.desc)
        TextView lbDesc;
        @BindView(R.id.date)
        TextView lbDate;
        @BindView(R.id.owner)
        TextView lbOwner;
        @BindView(R.id.delete)
        TextView lbDelete;
        @BindView(R.id.share)
        TextView lbShare;
        @BindView(R.id.swipe_layout)
        SwipeLayout swipeLayout;

        Media coverImg;
        MediaShare currentItem;

        AlbumListViewHolder(View view) {

            ButterKnife.bind(this, view);
        }

        void refreshView(MediaShare mediaShare) {

            currentItem = mediaShare;
            restoreSwipeLayoutState();

            coverImg = mPresenter.loadMedia(currentItem.getCoverImageUUID());

            mPresenter.loadMediaToView(containerActivity, coverImg, ivMainPic);

            if (currentItem.getViewersListSize() == 0) {
                ivLock.setVisibility(View.GONE);
                lbShare.setText(containerActivity.getString(R.string.share_text));
            } else {
                ivLock.setVisibility(View.VISIBLE);
                lbShare.setText(containerActivity.getString(R.string.private_text));
            }

            String title = currentItem.getTitle();

            String photoCount = String.valueOf(currentItem.getMediaShareContents().size());

            if (title.length() > 8) {
                title = title.substring(0, 8);

                title += containerActivity.getString(R.string.android_ellipsize);
            }

            title = String.format(containerActivity.getString(R.string.android_share_album_title), title, photoCount);

            lbTitle.setText(title);

            String desc = currentItem.getDesc();

            if (desc == null || desc.length() == 0) {
                lbDesc.setVisibility(View.GONE);
            } else {
                lbDesc.setVisibility(View.VISIBLE);
                lbDesc.setText(currentItem.getDesc());
            }

            lbDate.setText(currentItem.getDate().substring(0, 10));

            User user = mPresenter.loadUser(currentItem.getCreatorUUID());
            if (user != null) {
                lbOwner.setText(user.getUserName());
            }

            lbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    restoreSwipeLayoutState();

                    MediaShare cloneMediaShare = currentItem.cloneMyself();

                    mPresenter.modifyMediaShare(cloneMediaShare);

                }
            });

            lbDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    new AlertDialog.Builder(containerActivity).setMessage(containerActivity.getString(R.string.confirm_delete))
                            .setPositiveButton(containerActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    restoreSwipeLayoutState();

                                    mPresenter.deleteMediaShare(currentItem);

                                }
                            }).setNegativeButton(containerActivity.getString(R.string.cancel), null).create().show();


                }
            });

            mainBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(containerActivity, AlbumContentActivity.class);
                    intent.putExtra(Util.KEY_MEDIA_SHARE_UUID, currentItem.getUuid());
                    containerActivity.startActivityForResult(intent, Util.KEY_ALBUM_CONTENT_REQUEST_CODE);
                }
            });

            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    super.onStartOpen(layout);

                    if (lastSwipeLayout != null) {
                        lastSwipeLayout.close();
                    }
                    lastSwipeLayout = swipeLayout;
                }
            });

        }

        private void restoreSwipeLayoutState() {
            //restore mainbar state
//            mainBar.setTranslationX(0.0f);
            swipeLayout.close();
        }

    }

}

