package com.winsun.fruitmix.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.AlbumPicContentActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.NewAlbumPicChooseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2016/4/19.
 */
public class AlbumList implements NavPagerActivity.Page {

    public static final String TAG = AlbumList.class.getSimpleName();

    NavPagerActivity containerActivity;
    View view;
    ImageView ivAdd;
    LinearLayout mLoadingLayout;
    LinearLayout mNoContentLayout;

    private ListView mainListView;
    List<Map<String, Object>> albumList;

    private long mDownTime = 0;
    private double mDiffTimeMilliSecond = 200;

    ProgressDialog mDialog;

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    public AlbumList(NavPagerActivity activity_) {

        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(
                R.layout.album_list, null);

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mainListView = (ListView) view.findViewById(R.id.mainList);
        mainListView.setAdapter(new AlbumListViewAdapter(this));

        mLoadingLayout = (LinearLayout) view.findViewById(R.id.loading_layout);
        mNoContentLayout = (LinearLayout) view.findViewById(R.id.no_content_layout);

        ivAdd = (ImageView) view.findViewById(R.id.add_album);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(containerActivity, NewAlbumPicChooseActivity.class);
                containerActivity.startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
            }
        });

    }

    public void reloadList() {
        List<Map<String, Object>> albumList1;
        Map<String, Object> albumItem;
        String[] stArr;
        String coverImg;

        albumList1 = new ArrayList<>();

        for (ConcurrentMap<String, String> albumRaw : LocalCache.SharesMap.values()) {
            albumItem = new HashMap<String, Object>();
            if (albumRaw.get("type").equals("album") && albumRaw.get("del").equals("0")) {
                albumItem.put("type", albumRaw.get("type"));
                albumItem.put("title", albumRaw.get("title"));
                albumItem.put("creatorNick", LocalCache.UsersMap.get(albumRaw.get("creator")).get("name"));

                StringBuilder images = new StringBuilder("");
                for (String image : albumRaw.get("images").split(",")) {

                    if (LocalCache.MediasMap.containsKey(image) || LocalCache.LocalImagesMapKeyIsUUID.containsKey(image)) {
                        images.append(image);
                        images.append(",");
                    } else {
                        images.append("");
                    }
                }
                albumItem.put("images", images.toString());

                albumItem.put("mtime", albumRaw.get("mtime"));
                albumItem.put("desc", albumRaw.get("desc"));
                albumItem.put("date", albumRaw.get("date"));
                albumItem.put("uuid", albumRaw.get("uuid"));
                if (albumRaw.get("maintained").equals("false")) {
                    albumItem.put("maintained", false);
                } else {
                    albumItem.put("maintained", true);
                }
                if (albumItem.get("images").equals("")) {
                    albumItem.put("imageCount", 0);
                } else {
                    albumItem.put("imageCount", ((String) albumItem.get("images")).split(",").length);
                }

                albumItem.put("private", albumRaw.get("private"));

                // cover
                if (((String) albumItem.get("images")).contains(","))
                    coverImg = ((String) albumItem.get("images")).substring(0, ((String) albumItem.get("images")).indexOf(","));
                else
                    coverImg = (String) albumItem.get("images");
                albumItem.put("coverImg", coverImg);

                if (albumRaw.containsKey("local")) {
                    albumItem.put("local", "true");
                }

                albumList1.add(albumItem);
                Log.d("winsun", "ms " + albumItem);

            }
        }

        Collections.sort(albumList1, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                Map<String, Object> map1, map2;
                map1 = (Map<String, Object>) lhs;
                map2 = (Map<String, Object>) rhs;
                long mtime1 = Long.parseLong((String) map1.get("mtime"));
                long mtime2 = Long.parseLong((String) map2.get("mtime"));
                if (mtime1 < mtime2)
                    return 1;
                else if (mtime1 > mtime2)
                    return -1;
                else return 0;
            }
        });

        albumList = albumList1;

    }

    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);

        reloadList();

        mLoadingLayout.setVisibility(View.INVISIBLE);
        ivAdd.setVisibility(View.VISIBLE);
        if (albumList.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mainListView.setVisibility(View.INVISIBLE);
        } else {
            mNoContentLayout.setVisibility(View.INVISIBLE);
            mainListView.setVisibility(View.VISIBLE);
            ((BaseAdapter) (mainListView.getAdapter())).notifyDataSetChanged();
            mainListView.setSelection(0);
        }

    }

    public void onDidAppear() {

        refreshView();
    }

    private void deleteAlbumInLocalMap(String uuid) {

        if (LocalCache.SharesMap.containsKey(uuid)) {
            Map<String, String> map = LocalCache.SharesMap.get(uuid);
            map.put("del", "1");
        }

    }

    public View getView() {
        return view;
    }

    class AlbumListViewAdapter extends BaseAdapter {

        AlbumList container;
        RelativeLayout lastMainbar;

        public static final int LOCAL_ITEM_WITH_NETWORK = 0;
        public static final int SUCCEED = 1;
        public static final int REMOTE_ITEM_WITHOUT_NETWORK = 2;
        public static final int EXCEPTION = 3;

        public AlbumListViewAdapter(AlbumList container_) {
            container = container_;
        }

        @Override
        public int getCount() {
            if (container.albumList == null) return 0;
            return container.albumList.size();
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View view;
            GridView gvGrid;
            final Map<String, Object> currentItem;
            final RelativeLayout mainBar;
            NetworkImageView ivMainPic;
            ImageView ivRecommand, ivCreate, ivLock;
            TextView lbHot, lbTitle, lbDesc, lbDate, lbOwner, lbPhotoCount;
            TextView lbDelete, lbShare;
            ConcurrentMap<String, String> coverImg;
            final boolean sLocal;
            int w, h;


            if (convertView == null)
                view = LayoutInflater.from(container.containerActivity).inflate(R.layout.album_list_cell, parent, false);
            else view = convertView;

            currentItem = (Map<String, Object>) this.getItem(position);

            mainBar = (RelativeLayout) view.findViewById(R.id.mainBar);
            ivMainPic = (NetworkImageView) view.findViewById(R.id.mainPic);
            ivRecommand = (ImageView) view.findViewById(R.id.recommand);
            ivCreate = (ImageView) view.findViewById(R.id.create);
            ivLock = (ImageView) view.findViewById(R.id.lock);
            lbHot = (TextView) view.findViewById(R.id.hot);
            lbTitle = (TextView) view.findViewById(R.id.title);
            lbPhotoCount = (TextView) view.findViewById(R.id.photo_count_tv);
            lbDesc = (TextView) view.findViewById(R.id.desc);
            lbDelete = (TextView) view.findViewById(R.id.delete);
            lbShare = (TextView) view.findViewById(R.id.share);
            lbDate = (TextView) view.findViewById(R.id.date);
            lbOwner = (TextView) view.findViewById(R.id.owner);

            //restore mainbar state
            mainBar.setTranslationX(0.0f);

            //check image
            coverImg = LocalCache.MediasMap.get(String.valueOf(currentItem.get("coverImg")));
            if (coverImg != null) sLocal = false;
            else {
                coverImg = LocalCache.LocalImagesMapKeyIsUUID.get(String.valueOf(currentItem.get("coverImg")));
                sLocal = true;
            }

            if (coverImg != null) {

                if (sLocal) {  // local bitmap path

                    String url = String.valueOf(coverImg.get("thumb"));

                    mImageLoader.setShouldCache(false);
                    ivMainPic.setTag(url);
                    ivMainPic.setDefaultImageResId(R.drawable.placeholder_photo);
                    ivMainPic.setImageUrl(url, mImageLoader);

                } else {

                    w = Integer.parseInt(coverImg.get("width"));
                    h = Integer.parseInt(coverImg.get("height"));

                    int[] result = Util.formatPhotoWidthHeight(w, h);

                    String url = String.format(containerActivity.getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + coverImg.get("uuid"), result[0], result[1]);

                    mImageLoader.setShouldCache(true);
                    ivMainPic.setTag(url);
                    ivMainPic.setDefaultImageResId(R.drawable.placeholder_photo);
                    ivMainPic.setImageUrl(url, mImageLoader);
                }
            } else {
                ivMainPic.setDefaultImageResId(R.drawable.placeholder_photo);
                ivMainPic.setImageUrl(null, mImageLoader);
            }

            if (currentItem.get("type").equals("recommand")) {
                ivRecommand.setVisibility(View.VISIBLE);
                lbHot.setVisibility(View.VISIBLE);
                ivCreate.setVisibility(View.VISIBLE);
                lbDate.setVisibility(View.GONE);
                lbOwner.setVisibility(View.GONE);
            } else {
                ivRecommand.setVisibility(View.GONE);
                lbHot.setVisibility(View.GONE);
                ivCreate.setVisibility(View.GONE);
                lbDate.setVisibility(View.VISIBLE);
                lbOwner.setVisibility(View.VISIBLE);
            }

            if (currentItem.get("private").equals("true")) {
                ivLock.setVisibility(View.GONE);
                lbShare.setText(containerActivity.getString(R.string.public_text));
            } else {
                ivLock.setVisibility(View.VISIBLE);
                lbShare.setText(containerActivity.getString(R.string.private_text));
            }

            lbTitle.setText(String.valueOf(currentItem.get("title")));
            lbPhotoCount.setText(String.format(containerActivity.getString(R.string.photo_count), currentItem.get("imageCount")));
            lbDesc.setText((String) currentItem.get("desc"));
            lbDate.setText(((String) currentItem.get("date")).substring(0, 10));

            lbOwner.setText(String.valueOf(currentItem.get("creatorNick")));

            lbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    if (!(boolean) currentItem.get("maintained")) {
                        Toast.makeText(containerActivity, containerActivity.getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    new AsyncTask<Object, Object, Integer>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();

                            mDialog = ProgressDialog.show(containerActivity, containerActivity.getString(R.string.operating_title), containerActivity.getString(R.string.loading_message), true, false);

                        }

                        @Override
                        protected Integer doInBackground(Object... params) {
                            String data;

                            if (Util.getNetworkState(containerActivity)) {
                                if (currentItem.containsKey("local")) {
                                    return LOCAL_ITEM_WITH_NETWORK;
                                }

                                data = "";
                                if (currentItem.get("private").equals("true")) {
                                    for (String key : LocalCache.UsersMap.keySet()) {
                                        data += ",\\\"" + key + "\\\"";
                                    }
                                } else data = ",";

                                data = "{\"commands\": \"[{\\\"op\\\":\\\"replace\\\", \\\"path\\\":\\\"" + currentItem.get("uuid") + "\\\", \\\"value\\\":{\\\"archived\\\":\\\"false\\\",\\\"album\\\":\\\"true\\\", \\\"maintainers\\\":[\\\"" + FNAS.userUUID + "\\\"], \\\"tags\\\":[{\\\"albumname\\\":\\\"" + currentItem.get("title") + "\\\", \\\"desc\\\":\\\"" + currentItem.get("desc") + "\\\"}], \\\"viewers\\\":[" + data.substring(1) + "]}}]\"}";
                                try {
                                    FNAS.PatchRemoteCall(Util.MEDIASHARE_PARAMETER, data);
                                    FNAS.retrieveShareMap();
                                    return SUCCEED;
                                } catch (Exception e) {

                                    e.printStackTrace();

                                    return EXCEPTION;
                                }

                            } else {
                                if (!currentItem.containsKey("local")) {
                                    return REMOTE_ITEM_WITHOUT_NETWORK;
                                }


                                DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                                Share share = dbUtils.getLocalShareByUuid(String.valueOf(currentItem.get("uuid")));
                                StringBuilder builder = new StringBuilder();
                                if (currentItem.get("private").equals("true")) {
                                    for (String user : LocalCache.UsersMap.keySet()) {
                                        builder.append(user);
                                        builder.append(",");
                                    }
                                }
                                String viewer = builder.toString();
                                Log.i("create album viewer:", viewer);
                                share.setViewer(viewer);
                                dbUtils.updateLocalShare(share, share.getUuid());

                                FNAS.loadLocalShare();

                                return SUCCEED;
                            }
                        }

                        @Override
                        protected void onPostExecute(Integer sSuccess) {

                            mDialog.dismiss();

                            if (sSuccess == LOCAL_ITEM_WITH_NETWORK) {
                                Toast.makeText(containerActivity, containerActivity.getString(R.string.share_uploading), Toast.LENGTH_SHORT).show();

                            } else if (sSuccess == SUCCEED) {

                                Toast.makeText(containerActivity, containerActivity.getString(R.string.operation_success), Toast.LENGTH_SHORT).show();

                                reloadList();
                                ((BaseAdapter) (mainListView.getAdapter())).notifyDataSetChanged();
                                mainListView.setSelection(0);
                            } else if (sSuccess == REMOTE_ITEM_WITHOUT_NETWORK) {
                                Toast.makeText(containerActivity, containerActivity.getString(R.string.no_network), Toast.LENGTH_SHORT).show();

                            } else if (sSuccess == EXCEPTION) {
                                Toast.makeText(containerActivity, containerActivity.getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                            }

                            mainBar.setTranslationX(0.0f);
                        }

                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            });

            lbDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    if (!(boolean) currentItem.get("maintained")) {
                        Toast.makeText(containerActivity, containerActivity.getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    new AsyncTask<Object, Object, Integer>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();

                            mDialog = ProgressDialog.show(containerActivity, containerActivity.getString(R.string.operating_title), containerActivity.getString(R.string.loading_message), true, false);
                        }

                        @Override
                        protected Integer doInBackground(Object... params) {
                            String data;

                            if (Util.getNetworkState(containerActivity)) {
                                if (currentItem.containsKey("local")) {
                                    return LOCAL_ITEM_WITH_NETWORK;
                                }

                                data = "{\"commands\": \"[{\\\"op\\\":\\\"replace\\\", \\\"path\\\":\\\"" + currentItem.get("uuid") + "\\\", \\\"value\\\":{\\\"archived\\\":\\\"true\\\",\\\"album\\\":\\\"true\\\", \\\"maintainers\\\":[\\\"" + FNAS.userUUID + "\\\"], \\\"tags\\\":[{\\\"albumname\\\":\\\"" + currentItem.get("title") + "\\\", \\\"desc\\\":\\\"" + currentItem.get("desc") + "\\\"}], \\\"viewers\\\":[]}}]\"}";
                                try {
                                    FNAS.PatchRemoteCall(Util.MEDIASHARE_PARAMETER, data);
                                    FNAS.retrieveShareMap();
                                    return SUCCEED;
                                } catch (Exception e) {

                                    e.printStackTrace();

                                    return EXCEPTION;
                                }

                            } else {
                                if (!currentItem.containsKey("local")) {
                                    return REMOTE_ITEM_WITHOUT_NETWORK;
                                }

                                DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                                long value = dbUtils.deleteLocalShareByUUid(String.valueOf(currentItem.get("uuid")));

                                Log.i(TAG, value + "");

                                FNAS.delShareInDocumentsMapById(String.valueOf(currentItem.get("uuid")));

                                return SUCCEED;
                            }

                        }

                        @Override
                        protected void onPostExecute(Integer sSuccess) {

                            mDialog.dismiss();

                            if (sSuccess == LOCAL_ITEM_WITH_NETWORK) {

                                Toast.makeText(containerActivity, containerActivity.getString(R.string.share_uploading), Toast.LENGTH_SHORT).show();

                            } else if (sSuccess == SUCCEED) {
                                //Snackbar.make(mainListView, containerActivity.getString(R.string.operation_fail), Snackbar.LENGTH_SHORT).show();

                                Toast.makeText(containerActivity, containerActivity.getString(R.string.operation_success), Toast.LENGTH_SHORT).show();

                                reloadList();
                                ((BaseAdapter) (mainListView.getAdapter())).notifyDataSetChanged();
                                mainListView.setSelection(0);

                            } else if (sSuccess == REMOTE_ITEM_WITHOUT_NETWORK) {
                                Toast.makeText(containerActivity, containerActivity.getString(R.string.no_network), Toast.LENGTH_SHORT).show();

                            } else if (sSuccess == EXCEPTION) {
                                Toast.makeText(containerActivity, containerActivity.getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                            }
                            mainBar.setTranslationX(0.0f);
                        }

                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            });

            mainBar.setOnTouchListener(new View.OnTouchListener() {

                float x, y, lastX, lastY, vY, vX;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int margin;
                    margin = Util.Dp2Px(100);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            if (lastMainbar != null) lastMainbar.setTranslationX(0.0f);
                            lastMainbar = mainBar;
                            x = event.getRawX() - mainBar.getTranslationX();
                            y = event.getRawY();
                            lastX = x;
                            lastY = y;

                            mDownTime = System.currentTimeMillis();

                            Log.d(TAG, "down");
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d(TAG, "up X:" + (lastX - x) + " Y:" + (lastY - y));

                            if (System.currentTimeMillis() - mDownTime < mDiffTimeMilliSecond && (lastX - x) * (lastX - x) + (lastY - y) * (lastY - y) < 100.0) {
                                Intent intent = new Intent();
                                intent.setClass(containerActivity, AlbumPicContentActivity.class);
                                intent.putExtra("images", (String) currentItem.get("images"));
                                intent.putExtra("uuid", (String) currentItem.get("uuid"));
                                intent.putExtra("title", (String) currentItem.get("title"));
                                intent.putExtra("desc", (String) currentItem.get("desc"));
                                intent.putExtra("maintained", (boolean) currentItem.get("maintained"));
                                intent.putExtra("private", (String) currentItem.get("private"));
                                intent.putExtra("local", currentItem.containsKey("local"));
                                containerActivity.startActivityForResult(intent, Util.KEY_ALBUM_CONTENT_REQUEST_CODE);

                            }
                        case MotionEvent.ACTION_CANCEL:
                            Log.d(TAG, "cancel " + (lastX - x) + " " + (lastY - y));
                            if (lastX - x > -margin + 0.5 && lastX - x < margin - 0.5) {
                                if (vX > 30.0) mainBar.setTranslationX(margin);
                                else if (vX < -30.0) mainBar.setTranslationX(-margin);
                                else mainBar.setTranslationX(0.0f);
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            vX = event.getRawX() - lastX;
                            vY = event.getRawX() - lastY;
                            lastX = event.getRawX();
                            lastY = event.getRawY();
                            if (lastX - x > margin) lastX = x + margin;
                            else if (lastX - x < -margin) lastX = x - margin;
                            mainBar.setTranslationX(lastX - x);
                            break;
                    }
                    return true;
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
            return container.albumList.get(position);
        }
    }

}

