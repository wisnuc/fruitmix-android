package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.component.BigLittleImageView;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.OfflineTask;
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

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/4/28.
 */
public class AlbumPicContentActivity extends AppCompatActivity {

    public static final String TAG = AlbumPicContentActivity.class.getSimpleName();

    GridView mainGridView;
    ImageView ivBack;
    TextView mTitleTextView;

    Toolbar mToolBar;

    List<Map<String, Object>> picList;

    private MenuItem mPrivatePublicMenu;

    private String mUuid;
    private String mTitle;
    private String mDesc;
    private String imagesStr;
    private boolean mMaintained;
    private boolean mPrivate;
    private boolean mIsLocked;

    private Context mContext;

    private boolean mShowMenu;

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    private boolean mShowCommentBtn = false;

    private ProgressDialog mDialog;

    private boolean isOperated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = this;

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        imagesStr = getIntent().getStringExtra("images");
        mUuid = getIntent().getStringExtra("uuid");
        mTitle = getIntent().getStringExtra("title");
        mDesc = getIntent().getStringExtra("desc");
        mMaintained = getIntent().getBooleanExtra("maintained", false);
        mIsLocked = getIntent().getBooleanExtra("local", false);
        mShowMenu = getIntent().getBooleanExtra(Util.NEED_SHOW_MENU, true);

        mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

        if (getIntent().getStringExtra("private").equals("1")) {
            mPrivate = true;
        } else {
            mPrivate = false;
        }

        setContentView(R.layout.activity_album_pic_content);

        ivBack = (ImageView) findViewById(R.id.back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mainGridView = (GridView) findViewById(R.id.mainGrid);
        mainGridView.setAdapter(new PicGridViewAdapter(this));

        mTitleTextView = (TextView) findViewById(R.id.title);
        mTitleTextView.setText(mTitle);

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        picList = new ArrayList<Map<String, Object>>();
        fillPicList(imagesStr);
        ((BaseAdapter) mainGridView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (isOperated)
            setResult(200);

        super.onBackPressed();
    }

    private void fillPicList(String imagesStr) {

        Map<String, Object> picItem;
        ConcurrentMap<String, String> picItemRaw;
        String[] stArr;

        picList.clear();

        if (!imagesStr.equals("")) {
            stArr = imagesStr.split(",");

            Log.i(TAG, "imageStr[0]:" + stArr[0]);

            for (int i = 0; i < stArr.length; i++) {
                picItem = new HashMap<String, Object>();
                picItemRaw = LocalCache.MediasMap.get(stArr[i]);

                Log.i(TAG, "media has it or not:" + (picItemRaw != null ? "true" : "false"));

                if (picItemRaw != null) {
                    picItem.put("cacheType", "nas");
                    picItem.put("resID", "" + R.drawable.default_img);
                    picItem.put("resHash", picItemRaw.get("uuid"));
                    picItem.put("width", picItemRaw.get("width"));
                    picItem.put("height", picItemRaw.get("height"));
                    picItem.put("uuid", picItemRaw.get("uuid"));
                    picItem.put("mtime", picItemRaw.get("mtime"));
                    picItem.put("selected", "0");
                    picItem.put("locked", "1");
                    picList.add(picItem);
                } else {
                    picItemRaw = LocalCache.LocalImagesMap2.get(stArr[i]);

                    Log.i(TAG, "localimagesMap2 has it or not:" + (picItemRaw != null ? "true" : "false"));

                    if (picItemRaw != null) {
                        picItem.put("cacheType", "local");
                        picItem.put("resID", "" + R.drawable.default_img);
                        picItem.put("thumb", picItemRaw.get("thumb"));
                        picItem.put("width", picItemRaw.get("width"));
                        picItem.put("height", picItemRaw.get("height"));
                        picItem.put("uuid", picItemRaw.get("uuid"));
                        picItem.put("mtime", picItemRaw.get("mtime"));
                        picItem.put("selected", "0");
                        picItem.put("locked", "1");
                        picList.add(picItem);
                    }
                }
            }
        }
    }

    public void showSlider(int position) {
        LocalCache.TransActivityContainer.put("imgSliderList", picList);
        Intent intent = new Intent();
        intent.putExtra("pos", position);
        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, mShowCommentBtn);
        intent.setClass(this, PhotoSliderActivity.class);
        startActivity(intent);
    }

    class PicGridViewAdapter extends BaseAdapter {

        AlbumPicContentActivity activity;

        public PicGridViewAdapter(AlbumPicContentActivity activity_) {
            activity = activity_;
        }

        @Override
        public int getCount() {
            if (activity.picList == null) return 0;
            return activity.picList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            TextView lbTitle;
            GridView gvGrid;
            final Map<String, Object> currentItem;
            final RelativeLayout mainBar;
            NetworkImageView ivMain;
            ImageView ivLock;

            if (convertView == null)
                view = LayoutInflater.from(activity).inflate(R.layout.photo_list_cell_cell, parent, false);
            else view = convertView;

            currentItem = (Map<String, Object>) this.getItem(position);

            ivMain = (NetworkImageView) view.findViewById(R.id.mainPic);
            ivLock = (ImageView) view.findViewById(R.id.lock);

            if (currentItem.get("cacheType").equals("local")) {  // local bitmap path
//                LocalCache.LoadLocalBitmapThumb((String) currentItem.get("thumb"), w, h, ivMain);

                String url = String.valueOf(currentItem.get("thumb"));

                mImageLoader.setShouldCache(false);
                ivMain.setTag(url);
                ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
                ivMain.setImageUrl(url, mImageLoader);

            } else if (currentItem.get("cacheType").equals("nas")) {
//                LocalCache.LoadRemoteBitmapThumb((String) (currentItem.get("resHash")), w, h, ivMain);

                int width = Integer.parseInt((String) currentItem.get("width"));
                int height = Integer.parseInt((String) currentItem.get("height"));

                int[] result = Util.formatPhotoWidthHeight(width, height);

                String url = String.format(getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + currentItem.get("resHash"), result[0], result[1]);

                mImageLoader.setShouldCache(true);
                ivMain.setTag(url);
                ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
                ivMain.setImageUrl(url, mImageLoader);
            }

            ivMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.showSlider(position);
                }
            });

            return view;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public Object getItem(int position) {
            return activity.picList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (mShowMenu) {

            getMenuInflater().inflate(R.menu.album_menu, menu);

            mPrivatePublicMenu = menu.findItem(R.id.set_private_public);

            if (mPrivate) {
                mPrivatePublicMenu.setTitle(getString(R.string.set_public));
            } else {
                mPrivatePublicMenu.setTitle(getString(R.string.set_private));
            }
        }

        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i("islock", mIsLocked + "");

        if (Util.getNetworkState(mContext)) {
            if (mIsLocked) {
                Toast.makeText(mContext, getString(R.string.share_uploading), Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            if (!mIsLocked) {
                Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        if (!mMaintained) {
            Toast.makeText(mContext, getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

            return true;
        }

        Intent intent;
        switch (item.getItemId()) {
            case R.id.setting_album:
                intent = new Intent(this, ModifyAlbumActivity.class);
                intent.putExtra(Util.MEDIASHARE_UUID, mUuid);
                startActivityForResult(intent, Util.KEY_MODIFY_ALBUM_REQUEST_CODE);
                break;
            case R.id.edit_photo:
                intent = new Intent(this, EditPhotoActivity.class);
                intent.putExtra("images", imagesStr);
                intent.putExtra(Util.MEDIASHARE_UUID, mUuid);
                startActivityForResult(intent, Util.KEY_EDIT_PHOTO_REQUEST_CODE);
                break;
            case R.id.set_private_public:
                setPublicPrivate();
                break;
            case R.id.delete_album:
                deleteCurrentAblum();
                break;
            default:
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_EDIT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            fillPicList(data.getStringExtra(Util.NEW_ALBUM_CONTENT));
            ((BaseAdapter) mainGridView.getAdapter()).notifyDataSetChanged();
        } else if (requestCode == Util.KEY_MODIFY_ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            mTitle = data.getStringExtra(Util.UPDATED_ALBUM_TITLE);
            mTitleTextView.setText(mTitle);
        }

        isOperated = true;
    }

    private void deleteCurrentAblum() {
        new AsyncTask<Object, Object, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mDialog = ProgressDialog.show(mContext, getString(R.string.loading_title), getString(R.string.loading_message), true, false);
            }

            @Override
            protected Boolean doInBackground(Object... params) {
                String data;

                if (Util.getNetworkState(mContext)) {

                    data = "{\"commands\": \"[{\\\"op\\\":\\\"replace\\\", \\\"path\\\":\\\"" + mUuid + "\\\", \\\"value\\\":{\\\"archived\\\":\\\"true\\\",\\\"album\\\":\\\"true\\\", \\\"maintainers\\\":[\\\"" + FNAS.userUUID + "\\\"], \\\"tags\\\":[{\\\"albumname\\\":\\\"" + mTitle + "\\\", \\\"desc\\\":\\\"" + mDesc + "\\\"}], \\\"viewers\\\":[]}}]\"}";
                    try {
                        FNAS.PatchRemoteCall(Util.MEDIASHARE_PARAMETER, data);
                        FNAS.LoadDocuments();
                        return true;
                    } catch (Exception e) {

                        e.printStackTrace();

/*
                    // insert offline work add by liang.wu
                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                    dbUtils.openWritableDB();
                    OfflineTask offlineTask = new OfflineTask();
                    offlineTask.setHttpType(OfflineTask.HttpType.PATCH);
                    offlineTask.setOperationType(OfflineTask.OperationType.DELETE);
                    offlineTask.setRequest("/mediashare");
                    offlineTask.setData(data);
                    offlineTask.setOperationCount(0);
                    dbUtils.insertTask(offlineTask);

                    dbUtils.close();
                    //end add
*/

                        return false;
                    }

                } else {


                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                    dbUtils.deleteLocalShareByUUid(mUuid);

                    FNAS.delShareInDocumentsMapById(mUuid);

                    return true;
                }
            }

            @Override
            protected void onPostExecute(Boolean sSuccess) {

/*                //delete local album add by liang.wu
                if (!sSuccess) {
                    deleteAlbumInLocalMap(mUuid);
                }
                //end add by liang.wu*/

                mDialog.dismiss();

                if (sSuccess) {
                    setResult(200);
                } else {
                    Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
                }

                finish();

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }

    private void deleteAlbumInLocalMap(String uuid) {

        if (LocalCache.DocumentsMap.containsKey(uuid)) {
            Map<String, String> map = LocalCache.DocumentsMap.get(uuid);
            map.put("del", "1");
        }

    }

    private void setPublicPrivate() {
        new AsyncTask<Object, Object, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mDialog = ProgressDialog.show(mContext, getString(R.string.loading_title), getString(R.string.loading_message), true, false);
            }

            @Override
            protected Boolean doInBackground(Object... params) {
                String data;

                if (Util.getNetworkState(mContext)) {
                    data = "";
                    if (mPrivate) {
                        for (String key : LocalCache.UsersMap.keySet()) {
                            data += ",\\\"" + key + "\\\"";
                        }
                    } else data = ",";

                    data = "{\"commands\": \"[{\\\"op\\\":\\\"replace\\\", \\\"path\\\":\\\"" + mUuid + "\\\", \\\"value\\\":{\\\"archived\\\":\\\"false\\\",\\\"album\\\":\\\"true\\\", \\\"maintainers\\\":[\\\"" + FNAS.userUUID + "\\\"], \\\"tags\\\":[{\\\"albumname\\\":\\\"" + mTitle + "\\\", \\\"desc\\\":\\\"" + mDesc + "\\\"}], \\\"viewers\\\":[" + data.substring(1) + "]}}]\"}";
                    try {
                        FNAS.PatchRemoteCall(Util.MEDIASHARE_PARAMETER, data);
                        FNAS.LoadDocuments();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                } else {

                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                    Share share = dbUtils.getLocalShareByUuid(mUuid);
                    StringBuilder builder = new StringBuilder();
                    if (mPrivate) {
                        for (String user : LocalCache.UsersMap.keySet()) {
                            builder.append(user);
                            builder.append(",");
                        }
                    }
                    String viewer = builder.toString();
                    Log.i("create album share:", share.toString());
                    share.setViewer(viewer);
                    dbUtils.updateLocalShare(share, share.getUuid());

                    FNAS.loadLocalShare();
                    return true;
                }

            }

            @Override
            protected void onPostExecute(Boolean sSuccess) {

                mDialog.dismiss();

                if (sSuccess) {
                    Toast.makeText(mContext, getString(R.string.setting_succeed), Toast.LENGTH_SHORT).show();

                    mPrivate = !mPrivate;
                    if (mPrivate) {
                        mPrivatePublicMenu.setTitle(getString(R.string.set_public));
                    } else {
                        mPrivatePublicMenu.setTitle(getString(R.string.set_private));
                    }

                    isOperated = true;

                } else {
                    Toast.makeText(mContext, getString(R.string.setting_fail), Toast.LENGTH_SHORT).show();

                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
