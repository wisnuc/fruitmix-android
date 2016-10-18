package com.winsun.fruitmix;

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
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShareContent;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

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
    private GridLayoutManager mManager;
    private Context mContext;
    private EditPhotoAdapter mAdapter;

    private MediaShare mediaShare;
    private MediaShare modifiedMediaShare;
    private List<Media> mPhotoList;

    private ImageLoader mImageLoader;

    private ProgressDialog mDialog;

    private LocalBroadcastManager localBroadcastManager;
    private CustomReceiver customReceiver;
    private IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        ButterKnife.bind(this);

        mImageLoader = new ImageLoader(RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue(), ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mBack.setOnClickListener(this);
        mAddPhoto.setOnClickListener(this);
        mFinish.setOnClickListener(this);

        mContext = this;

        mediaShare = getIntent().getParcelableExtra(Util.KEY_MEDIASHARE);
        modifiedMediaShare = mediaShare.cloneMyself();

        mManager = new GridLayoutManager(mContext, mSpanCount);
        mEditPhotoRecyclerView.setLayoutManager(mManager);
        mEditPhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new EditPhotoAdapter();
        mEditPhotoRecyclerView.setAdapter(mAdapter);

        mPhotoList = new ArrayList<>();
        fillPhotoList(mediaShare.getMediaShareContents().toArray(new String[mPhotoList.size()]));
        mAdapter.notifyDataSetChanged();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        customReceiver = new CustomReceiver();
        filter = new IntentFilter(Util.PHOTO_IN_LOCAL_MEDIASHARE_MODIFIED);
        filter.addAction(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED);

    }

    @Override
    protected void onResume() {
        super.onResume();

        localBroadcastManager.registerReceiver(customReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        localBroadcastManager.unregisterReceiver(customReceiver);
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

            mPhotoList.add(picItem);

            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setDigest(picItem.getUuid());
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            modifiedMediaShare.addMediaShareContent(mediaShareContent);
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
                intent.putExtra(Util.EDIT_PHOTO, true);
                startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
                break;
            case R.id.finish:

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

            String[] selectedImageUUIDStr = data.getStringArrayExtra(Util.KEY_SELECTED_IMAGE_UUID_ARRAY);
            fillPhotoList(selectedImageUUIDStr);
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

        public EditPhotoViewHolder(View view) {
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

                    mPhotoList.remove(getAdapterPosition());
                    mAdapter.notifyItemRemoved(getAdapterPosition());

                    modifiedMediaShare.removeMediaShareContent(getAdapterPosition());
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

    private class CustomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            String result = intent.getStringExtra(Util.OPERATION_RESULT_NAME);

            OperationResult operationResult = OperationResult.valueOf(result);

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
    }
}
