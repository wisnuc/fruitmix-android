package com.winsun.fruitmix.mediaModule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EditPhotoActivity extends Activity implements View.OnClickListener {

    public static final String TAG = EditPhotoActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.finish)
    TextView mFinish;
    @BindView(R.id.edit_photo_gridview)
    RecyclerView mEditPhotoRecyclerView;
    @BindView(R.id.add_album)
    FloatingActionButton mAddPhoto;

    private int mSpanCount = 3;
    private Context mContext;
    private EditPhotoAdapter mAdapter;

    private MediaShare mediaShare;
    private MediaShare modifiedMediaShare;
    private List<Media> mPhotoList;

    private ImageLoader mImageLoader;

    private ProgressDialog mDialog;

    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        ButterKnife.bind(this);

        mContext = this;

        initImageLoader();

        mBack.setOnClickListener(this);
        mAddPhoto.setOnClickListener(this);
        mFinish.setOnClickListener(this);

        mediaShare = getIntent().getParcelableExtra(Util.KEY_MEDIASHARE);
        modifiedMediaShare = mediaShare.cloneMyself();

        GridLayoutManager mManager = new GridLayoutManager(mContext, mSpanCount);
        mEditPhotoRecyclerView.setLayoutManager(mManager);
        mEditPhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new EditPhotoAdapter();
        mEditPhotoRecyclerView.setAdapter(mAdapter);

        mPhotoList = new ArrayList<>();
        fillPhotoList(mediaShare.getMediaDigestInMediaShareContents().toArray(new String[mediaShare.getMediaContentsListSize()]));
        mAdapter.notifyDataSetChanged();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

    }

    private void initImageLoader() {
        mImageLoader = new ImageLoader(RequestQueueInstance.getInstance(this).getRequestQueue(), ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent){

        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();

        OperationResult operationResult = operationEvent.getOperationResult();

        switch (operationResult) {
            case SUCCEED:
                Toast.makeText(mContext, getString(R.string.operation_success), Toast.LENGTH_SHORT).show();
                getIntent().putExtra(Util.KEY_MEDIASHARE, modifiedMediaShare);
                EditPhotoActivity.this.setResult(RESULT_OK, getIntent());
                finish();
                break;
            case FAIL:
                Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
                EditPhotoActivity.this.setResult(RESULT_CANCELED, getIntent());
                finish();
                break;
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void fillPhotoList(String[] selectedImageUUIDs) {

        Media picItem;
        Media picItemRaw;
        for (String aStArr : selectedImageUUIDs) {
            picItem = new Media();
            picItemRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(aStArr);
            if (picItemRaw != null) {
                picItem.setLocal(false);
            } else {
                picItemRaw = LocalCache.LocalMediaMapKeyIsUUID.get(aStArr);
                picItem.setLocal(true);
                picItem.setThumb(picItemRaw.getThumb());
            }

            picItem.setUuid(picItemRaw.getUuid());
            picItem.setWidth(picItemRaw.getWidth());
            picItem.setHeight(picItemRaw.getHeight());
            picItem.setTime(picItemRaw.getTime());
            picItem.setSelected(false);
            picItem.setSharing(picItemRaw.isSharing());
            picItem.setOrientationNumber(picItemRaw.getOrientationNumber());

            mPhotoList.add(picItem);

            Log.i(TAG, "fillPhotoList: image uuid" + aStArr);

        }

    }

    private void fillMediaShareContents(String[] selectedImageUUIDs) {
        for (String imageUUID : selectedImageUUIDs) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setDigest(imageUUID);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            modifiedMediaShare.addMediaShareContent(mediaShareContent);

            Log.i(TAG, "fillMediaShareContents: image uuid:" + imageUUID);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.add_album:
                Intent intent = new Intent(mContext, NewAlbumPicChooseActivity.class);

                String[] alreadySelectedImageUUIDArray = new String[mediaShare.getMediaContentsListSize()];
                mediaShare.getMediaDigestInMediaShareContents().toArray(alreadySelectedImageUUIDArray);
                intent.putStringArrayListExtra(Util.KEY_ALREADY_SELECTED_IMAGE_UUID_ARRAYLIST, (ArrayList<String>) mediaShare.getMediaDigestInMediaShareContents());
                intent.putExtra(Util.EDIT_PHOTO, true);
                startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
                break;
            case R.id.finish:

                int diffOriginalMediaShareContentSize = mediaShare.getDifferentMediaShareContentInCurrentMediaShare(modifiedMediaShare).size();
                int diffModifiedMediaShareContentSize = modifiedMediaShare.getDifferentMediaShareContentInCurrentMediaShare(mediaShare).size();
                if(diffOriginalMediaShareContentSize == 0 && diffModifiedMediaShareContentSize == 0){

                    finish();
                }

                mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                Intent operationIntent = new Intent(Util.OPERATION);

                if (modifiedMediaShare.getMediaContentsListSize() != 0) {
                    modifiedMediaShare.setCoverImageDigest(modifiedMediaShare.getFirstMediaDigestInMediaContentsList());
                } else {
                    modifiedMediaShare.setCoverImageDigest("");
                }

                operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.EDIT_PHOTO_IN_MEDIASHARE.name());
                operationIntent.putExtra(Util.OPERATION_ORIGINAL_MEDIASHARE_WHEN_EDIT_PHOTO, mediaShare);
                operationIntent.putExtra(Util.OPERATION_MODIFIED_MEDIASHARE_WHEN_EDIT_PHOTO, modifiedMediaShare);

                if (Util.getNetworkState(mContext)) {

                    operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());

                    localBroadcastManager.sendBroadcast(operationIntent);

                } else {

                    operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());

                    localBroadcastManager.sendBroadcast(operationIntent);
                }

                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_CHOOSE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {

            String[] selectedImageUUIDStr = data.getStringArrayExtra(Util.KEY_NEW_SELECTED_IMAGE_UUID_ARRAY);
            fillPhotoList(selectedImageUUIDStr);
            fillMediaShareContents(selectedImageUUIDStr);

            mAdapter.notifyDataSetChanged();
        }
    }

    class EditPhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo_item)
        NetworkImageView mPhotoItem;
        @BindView(R.id.del_photo)
        ImageView mDelPhoto;
        @BindView(R.id.del_photo_layout)
        FrameLayout mDelPhotoLayout;

        private Media mMap;
        private int width, height;

        EditPhotoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(final int position) {
            mMap = mPhotoList.get(position);

            String imageUrl = mMap.getImageThumbUrl(mContext);
            mImageLoader.setShouldCache(!mMap.isLocal());
            mPhotoItem.setTag(imageUrl);
            mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
            mPhotoItem.setImageUrl(imageUrl, mImageLoader);

            mDelPhotoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    modifiedMediaShare.removeMediaShareContent(getAdapterPosition());

                    Log.i(TAG, "onClick: photo uuid" + mPhotoList.get(getAdapterPosition()).getUuid());

                    mPhotoList.remove(getAdapterPosition());
                    mAdapter.notifyItemRemoved(getAdapterPosition());

                }
            });
        }

    }

    class EditPhotoAdapter extends RecyclerView.Adapter<EditPhotoViewHolder> {
        @Override
        public int getItemCount() {
            return mPhotoList == null ? 0 : mPhotoList.size();
        }

        @Override
        public EditPhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.edit_photo_item, parent, false);

            return new EditPhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(EditPhotoViewHolder holder, int position) {

            holder.refreshView(position);
        }

    }

}
