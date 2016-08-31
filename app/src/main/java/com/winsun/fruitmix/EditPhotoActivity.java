package com.winsun.fruitmix;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

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
    ImageView mAddPhoto;

    private int mSpanCount = 3;
    private GridLayoutManager mManager;
    private Context mContext;
    private EditPhotoAdapter mAdapter;

    private String mImages;
    private List<Map<String, Object>> mPhotoList;

    private List<String> mPhotoUuidListOriginal;

    private String mMediaShareUUid;

    private ImageLoader mImageLoader;

    private ProgressDialog mDialog;

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

        mImages = getIntent().getStringExtra("images");
        mMediaShareUUid = getIntent().getStringExtra(Util.MEDIASHARE_UUID);

        mManager = new GridLayoutManager(mContext, mSpanCount);
        mEditPhotoRecyclerView.setLayoutManager(mManager);
        mEditPhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new EditPhotoAdapter();
        mEditPhotoRecyclerView.setAdapter(mAdapter);

        mPhotoList = new ArrayList<>();
        fillPhotoList(mImages);
        mAdapter.notifyDataSetChanged();

        mPhotoUuidListOriginal = new ArrayList<>(mPhotoList.size());
        for (Map<String, Object> map : mPhotoList) {
            mPhotoUuidListOriginal.add((String) map.get("uuid"));
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void fillPhotoList(String selectedImageUUIDStr) {

        if (!selectedImageUUIDStr.equals("")) {
            String[] stArr = selectedImageUUIDStr.split(",");
            Map<String, Object> picItem;
            ConcurrentMap<String, String> picItemRaw;
            for (String aStArr : stArr) {
                picItem = new HashMap<>();
                picItemRaw = LocalCache.MediasMap.get(aStArr);
                if (picItemRaw != null) {
                    picItem.put("cacheType", "nas");
                    picItem.put("resID", "" + R.drawable.default_img);
                    picItem.put("resHash", picItemRaw.get("uuid"));
                    picItem.put("width", picItemRaw.get("width"));
                    picItem.put("height", picItemRaw.get("height"));
                    picItem.put("uuid", picItemRaw.get("uuid"));
                    picItem.put("mtime", picItemRaw.get("lastModified"));
                    picItem.put("selected", "0");
                    picItem.put("locked", "1");
                    mPhotoList.add(picItem);
                } else {
                    picItemRaw = LocalCache.LocalImagesMapKeyIsUUID.get(aStArr);
                    if (picItemRaw != null) {
                        picItem.put("cacheType", "local");
                        picItem.put("resID", "" + R.drawable.default_img);
                        picItem.put("thumb", picItemRaw.get("thumb"));
                        picItem.put("width", picItemRaw.get("width"));
                        picItem.put("height", picItemRaw.get("height"));
                        picItem.put("uuid", picItemRaw.get("uuid"));
                        picItem.put("mtime", picItemRaw.get("lastModified"));
                        picItem.put("selected", "0");
                        picItem.put("locked", "1");
                        mPhotoList.add(picItem);
                    }
                }
            }
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

                final StringBuilder stringBuilder = new StringBuilder("{\"commands\": \"[");

                final List<String> photoUuidList = new ArrayList<>(mPhotoList.size());
                for (Map<String, Object> map : mPhotoList) {
                    photoUuidList.add((String) map.get("uuid"));
                }

                for (String string : mPhotoUuidListOriginal) {
                    if (!photoUuidList.contains(string)) {
                        stringBuilder.append("{\\\"op\\\":\\\"remove\\\",\\\"path\\\":\\\"").append(mMediaShareUUid).append("\\\",\\\"value\\\":{\\\"digest\\\":\\\"").append(string).append("\\\"}},");
                    }
                }

                for (String string : photoUuidList) {
                    if (!mPhotoUuidListOriginal.contains(string)) {
                        stringBuilder.append("{\\\"op\\\":\\\"add\\\",\\\"path\\\":\\\"").append(mMediaShareUUid).append("\\\",\\\"value\\\":{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"").append(string).append("\\\"}},");
                    }
                }

                if (stringBuilder.lastIndexOf(",") != -1) {
                    stringBuilder.replace(stringBuilder.lastIndexOf(","), stringBuilder.length(), "]\"}");


                    new AsyncTask<Object, Object, Boolean>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();

//                            Snackbar.make(mAddPhoto, getString(R.string.patch_now), Snackbar.LENGTH_LONG).show();
//                            Toast.makeText(mContext, getString(R.string.patch_now), Toast.LENGTH_SHORT).show();

                            mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);
                        }

                        @Override
                        protected Boolean doInBackground(Object... params) {

                            if (Util.getNetworkState(mContext)) {
                                try {

                                    boolean uploadFileResult = true;
                                    int uploadSucceedCount = 0;

                                    for (String string : photoUuidList) {
                                        if (!mPhotoUuidListOriginal.contains(string)) {
                                            if (!FNAS.isPhotoInMediaMap(string)) {

                                                if (LocalCache.LocalImagesMapKeyIsUUID.containsKey(string)) {
                                                    String thumb = LocalCache.LocalImagesMapKeyIsUUID.get(string).get("thumb");

                                                    ConcurrentMap<String, String> map = LocalCache.LocalImagesMapKeyIsThumb.get(thumb);

                                                    Log.i(TAG, "thumb:" + thumb + "hash:" + string);
                                                    if (!map.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) || map.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("false")) {
                                                        uploadFileResult = FNAS.UploadFile(map.get("thumb"));
                                                        Log.i(TAG, "digest:" + string + "uploadFileResult:" + uploadFileResult);
                                                        if (!uploadFileResult){
                                                            break;
                                                        }else {
                                                            uploadSucceedCount++;
                                                        }

                                                    }
                                                }

                                            }
                                        }
                                    }

                                    if (uploadSucceedCount > 0) {
                                        LocalCache.SetGlobalHashMap(Util.LOCAL_IMAGE_MAP_NAME, LocalCache.LocalImagesMapKeyIsThumb);
                                        Intent intent = new Intent(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED);
                                        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                                        broadcastManager.sendBroadcast(intent);
                                    }

                                    if (!uploadFileResult)
                                        return false;

                                    FNAS.PatchRemoteCall(Util.MEDIASHARE_PARAMETER, stringBuilder.toString());
                                    FNAS.retrieveShareMap();
                                    return true;
                                } catch (Exception e) {
                                    return false;
                                }
                            } else {

                                DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                                Share share = dbUtils.getLocalShareByUuid(mMediaShareUUid);
                                StringBuilder builder = new StringBuilder();
                                for (String string : photoUuidList) {
                                    builder.append(string);
                                    builder.append(",");
                                }
                                String digest = builder.toString();
                                Log.i("winsun edit digest:", digest);

                                share.setDigest(digest);
                                dbUtils.updateLocalShare(share, mMediaShareUUid);
                                FNAS.loadLocalShare();
                                return true;
                            }

                        }

                        @Override
                        protected void onPostExecute(Boolean sSuccess) {

                            mDialog.dismiss();

                            if (sSuccess) {
//                                Snackbar.make(mAddPhoto, "Patch Success", Snackbar.LENGTH_LONG).show();
                                Toast.makeText(mContext, getString(R.string.operation_success), Toast.LENGTH_SHORT).show();

                                if(!Util.getNetworkState(mContext)){
                                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                                    broadcastManager.sendBroadcast(new Intent(Util.LOCAL_SHARE_CHANGED));
                                }

                                stringBuilder.setLength(0);
                                for (Map<String, Object> map : mPhotoList) {
                                    stringBuilder.append(map.get("uuid"));
                                    stringBuilder.append(",");
                                }

                                int position = stringBuilder.lastIndexOf(",");
                                if (position != -1) {
                                    stringBuilder.replace(position, position + 1, "");
                                }
                                getIntent().putExtra(Util.NEW_ALBUM_CONTENT, stringBuilder.toString());
                                setResult(RESULT_OK, getIntent());
                                finish();

                            } else {
//                                Snackbar.make(mAddPhoto, "Patch Fail", Snackbar.LENGTH_LONG).show();
                                Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                                setResult(RESULT_CANCELED, getIntent());
                                finish();
                            }

                        }

                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }


                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_CHOOSE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {

            String selectedImageUUIDStr = data.getStringExtra("mSelectedImageUUIDStr");
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

        private Map<String, Object> mMap;
        private int width, height;

        public EditPhotoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(final int position) {
            mMap = mPhotoList.get(position);

            if (mMap.get("cacheType").equals("local")) {

                String url = String.valueOf(mMap.get("thumb"));

                mImageLoader.setShouldCache(false);
                mPhotoItem.setTag(url);
                mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoItem.setImageUrl(url, mImageLoader);

            } else if (mMap.get("cacheType").equals("nas")) {

                width = Integer.parseInt((String) mMap.get("width"));
                height = Integer.parseInt((String) mMap.get("height"));

                int[] result = Util.formatPhotoWidthHeight(width, height);

                String url = String.format(getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + mMap.get("resHash"), result[0], result[1]);

                mImageLoader.setShouldCache(true);
                mPhotoItem.setTag(url);
                mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoItem.setImageUrl(url, mImageLoader);
            }

            mDelPhotoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
