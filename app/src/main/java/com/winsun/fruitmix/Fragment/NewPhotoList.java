package com.winsun.fruitmix.Fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.winsun.fruitmix.CreateAlbumActivity;
import com.winsun.fruitmix.CustomApplication;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.ScrollbarPanelListView;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.model.Photo;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.services.LocalShareService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/7/28.
 */
public class NewPhotoList implements NavPagerActivity.Page {

    public static final String TAG = NewPhotoList.class.getSimpleName();

    Activity containerActivity;
    private View view;

    @BindView(R.id.photo_listview)
    ScrollbarPanelListView mListView;

    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;

    private TextView mCurrentTimeTv;

    private int mSpanCount = 3;

    private PhotoListAdapter mPhotoListAdapter;

    private List<String> mPhotoGroupList;

    private Map<String, List<Photo>> mPhotoMap;

    private Map<Integer, String> mPhotoTitleLineNumberMap;// key is photo title line number,value is photo title;
    private Map<Integer, List<Photo>> mPhotoContentLineNumberMap; // key is photo content line number,value is photo list;
    private int mPhotoLineTotalCount;

    private ProgressDialog mDialog;

    private int mScreenWidth;

    private boolean mIsFling = false;
    private NewPhotoListScrollListener mScrollListener;

    private boolean mSelectMode = false;

    private List<IPhotoListListener> mPhotoListListenerList;

    private int mSelectCount;

    private boolean mHasFlung = false;
    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    private int mFirstVisiableItem;
    private int mVisiableItemCount;

    public NewPhotoList(Activity activity) {
        containerActivity = activity;

        view = View.inflate(containerActivity, R.layout.new_photo_layout, null);
        ButterKnife.bind(this, view);

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "JWT " + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mPhotoGroupList = new ArrayList<>();
        mPhotoMap = new HashMap<>();

        mPhotoTitleLineNumberMap = new HashMap<>();
        mPhotoContentLineNumberMap = new HashMap<>();

        mPhotoListListenerList = new ArrayList<>();

        calcScreenWidth();

        mPhotoListAdapter = new PhotoListAdapter();
        mScrollListener = new NewPhotoListScrollListener();
        mListView.setOnScrollListener(mScrollListener);
        mListView.setAdapter(mPhotoListAdapter);

        mListView.setOnPositionChangedListener(new ScrollbarPanelListView.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(ScrollbarPanelListView listView, int position, View scrollBarPanel) {
                mCurrentTimeTv = (TextView) scrollBarPanel;

                String title;

                if (mPhotoTitleLineNumberMap.containsKey(position))
                    title = mPhotoTitleLineNumberMap.get(position);
                else {
                    title = mPhotoContentLineNumberMap.get(position).get(0).getTitle();
                }

                if (title.contains("1916-01-01")) {
                    mCurrentTimeTv.setText(containerActivity.getString(R.string.unknown_time_text));
                } else {
                    String[] titleSplit = title.split("-");
                    String time = titleSplit[0] + "年" + titleSplit[1] + "月";
                    mCurrentTimeTv.setText(time);
                }
            }
        });

        refreshView();
    }

    public void addPhotoListListener(IPhotoListListener listListener) {
        mPhotoListListenerList.add(listListener);
    }

    public void removePhotoListListener(IPhotoListListener listListener) {
        mPhotoListListenerList.remove(listListener);
    }

    public void setSelectMode(boolean selectMode) {
        mSelectMode = selectMode;

        mPhotoListAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshView() {

        if (!mSelectMode) {
            LocalCache.LoadLocalData();

            mLoadingLayout.setVisibility(View.VISIBLE);

            reloadData();

            mLoadingLayout.setVisibility(View.INVISIBLE);
            if (mPhotoGroupList.size() == 0) {
                mNoContentLayout.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.INVISIBLE);

                for (IPhotoListListener listener : mPhotoListListenerList)
                    listener.onNoPhotoItem(true);

            } else {
                mNoContentLayout.setVisibility(View.INVISIBLE);
                mListView.setVisibility(View.VISIBLE);
                ((BaseAdapter) (mListView.getAdapter())).notifyDataSetChanged();

                for (IPhotoListListener listener : mPhotoListListenerList)
                    listener.onNoPhotoItem(false);
            }

            clearSelectedPhoto();

        }

    }

    private void calcScreenWidth() {
        DisplayMetrics metric = new DisplayMetrics();
        containerActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        mScreenWidth = metric.widthPixels;
    }

    @Override
    public View getView() {
        return view;
    }

    private void reloadData() {

        String date;
        List<Photo> photoList;

        mPhotoGroupList.clear();
        mPhotoMap.clear();

        //load local images
        for (Map<String, String> map : LocalCache.LocalImagesMap.values()) {

            if (map.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) && map.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("true")) {
                continue;
            }

            date = map.get("mtime").substring(0, 10);
            if (mPhotoMap.containsKey(date)) {
                photoList = mPhotoMap.get(date);
            } else {
                mPhotoGroupList.add(date);
                photoList = new ArrayList<>();
                mPhotoMap.put(date, photoList);
            }

            Photo photo = new Photo();
            photo.setUuid(map.get("uuid"));
            photo.setWidth(map.get("width"));
            photo.setHeight(map.get("height"));
            photo.setCached(true);
            photo.setTime(map.get("mtime"));
            photo.setTitle(date);
            photo.setThumb(map.get("thumb"));
            photoList.add(photo);
        }

        //load remote images
        //TO DO: concurrentexception
        for (Map<String, String> map : LocalCache.MediasMap.values()) {

            date = map.get("mtime").substring(0, 10);
            if (mPhotoMap.containsKey(date)) {
                photoList = mPhotoMap.get(date);
            } else {
                mPhotoGroupList.add(date);
                photoList = new ArrayList<>();
                mPhotoMap.put(date, photoList);
            }

            Photo photo = new Photo();
            photo.setUuid(map.get("uuid"));
            photo.setWidth(map.get("width"));
            photo.setHeight(map.get("height"));
            photo.setCached(false);
            photo.setTime(map.get("mtime").replace("T", " ").replace("Z", " "));
            photo.setThumb("");
            photo.setTitle(date);
            photoList.add(photo);
        }

        Collections.sort(mPhotoGroupList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return -lhs.compareTo(rhs);
            }
        });

        calcPhotoLineNumber();

    }

    private void calcPhotoLineNumber() {

        int titlePosition = 0;
        int photoListSize;
        int photoListLineSize;

        for (String title : mPhotoGroupList) {
            mPhotoTitleLineNumberMap.put(titlePosition, title);

            List<Photo> photoList = mPhotoMap.get(title);
            photoListSize = photoList.size();
            photoListLineSize = (photoListSize / mSpanCount) + (photoListSize % mSpanCount == 0 ? 0 : 1);

//            Log.i(TAG, "titlePosition:" + titlePosition + " photoListSize:" + photoListSize + " photoListLineSize:" + photoListLineSize);

            for (int i = 0; i < photoListLineSize; i++) {

                if (mSpanCount + mSpanCount * i > photoListSize) {
                    mPhotoContentLineNumberMap.put(titlePosition + i + 1, photoList.subList(mSpanCount * i, photoListSize));
                } else {
                    mPhotoContentLineNumberMap.put(titlePosition + i + 1, photoList.subList(mSpanCount * i, mSpanCount + mSpanCount * i));
                }

            }
            titlePosition += photoListLineSize + 1;

        }

        mPhotoLineTotalCount = titlePosition - 1;
    }

    @NonNull
    public String getSelectedUIDString() {

        StringBuilder builder = new StringBuilder();
        for (List<Photo> photoList : mPhotoMap.values()) {
            for (Photo photo : photoList) {
                if (photo.isSelected()) {
                    builder.append(",");
                    builder.append(photo.getUuid());
                }
            }
        }

        String selectedUID = "";
        if (builder.length() >= 1) {
            selectedUID = builder.substring(1);
        }

        Log.i(TAG, "selectUID" + selectedUID);
        return selectedUID;
    }

    private void clearSelectedPhoto() {
        for (List<Photo> photoList : mPhotoMap.values()) {
            for (Photo photo : photoList) {
                photo.setSelected(false);
            }
        }
    }

    private void calcSelectedPhoto() {

        mSelectCount = 0;

        for (List<Photo> photoList : mPhotoMap.values()) {
            for (Photo photo : photoList) {
                if (photo.isSelected())
                    mSelectCount++;
            }
        }

    }

    /**
     * 将dp转化为px
     */
    private int dip2px(float dip) {
        float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, containerActivity.getResources().getDisplayMetrics());
        return (int) (v + 0.5f);
    }

    public void createAlbum() {
        Intent intent = new Intent();
        intent.setClass(containerActivity, CreateAlbumActivity.class);
        intent.putExtra("selectedUIDStr", getSelectedUIDString());
        containerActivity.startActivityForResult(intent, 100);
    }

    public void createShare() {
        new AsyncTask<Object, Object, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mDialog = ProgressDialog.show(containerActivity, containerActivity.getString(R.string.operating_title), containerActivity.getString(R.string.loading_message), true, false);
            }

            @Override
            protected Boolean doInBackground(Object... params) {
                String data, viewers, selectUUID;
                String[] selectedUIDArr;
                int i;

                selectUUID = getSelectedUIDString();
                selectedUIDArr = selectUUID.split(",");
                data = "";
                for (i = 0; i < selectedUIDArr.length; i++) {
                    data += ",{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"" + selectedUIDArr[i] + "\\\"}";
                }

                viewers = "";
                for (String key : LocalCache.UsersMap.keySet()) {
                    viewers += ",\\\"" + key + "\\\"";
                }
                if (viewers.length() == 0) {
                    viewers += ",";
                }
                Log.i("winsun viewer:", viewers);

                createAlbumInLocalAlbumDatabase(true, false, "", "", selectUUID);
                FNAS.loadLocalShare();

                return true;

/*                data = "{\"album\":false, \"archived\":false,\"maintainers\":\"[\\\"" + FNAS.userUUID + "\\\"]\",\"viewers\":\"[" + viewers.substring(1) + "]\",\"tags\":[{}],\"contents\":\"[" + data.substring(1) + "]\"}";
                Log.d("winsun", data);
                try {
                    FNAS.PostRemoteCall("/mediashare", data);
                    FNAS.LoadDocuments();
                    return true;
                } catch (Exception e) {
                    return false;
                }*/
            }

            @Override
            protected void onPostExecute(Boolean sSuccess) {

                mDialog.dismiss();

                if (Util.getNetworkState(containerActivity)) {
                    LocalShareService.startActionLocalShareTask(containerActivity);
                }
                if (sSuccess) {
                    if (containerActivity instanceof NavPagerActivity) {
                        ((NavPagerActivity) containerActivity).onActivityResult(0, 201, null);
                    }

                } else {
                    Snackbar.make(mListView, containerActivity.getString(R.string.operation_fail), Snackbar.LENGTH_SHORT).show();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void createAlbumInLocalAlbumDatabase(boolean isPublic, boolean otherMaintianer, String title, String desc, String digest) {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        StringBuilder builder = new StringBuilder();

        Share share = new Share();
        share.setUuid(Util.createLocalUUid());
        share.setDigest(digest);
        share.setTitle(title);
        share.setDesc(desc);

        if (isPublic) {
            for (String user : LocalCache.UsersMap.keySet()) {
                builder.append(user);
                builder.append(",");
            }
        }
        String viewer = builder.toString();
        Log.i(TAG, "create share viewer:" + viewer);
        Log.i(TAG, "create share digest:" + digest);

        share.setViewer(viewer);

        String maintainer;
        if (otherMaintianer) {
            maintainer = viewer;
        } else {
            builder.setLength(0);
            builder.append(FNAS.userUUID);
            builder.append(",");

            maintainer = builder.toString();
        }
        share.setMaintainer(maintainer);

        share.setCreator(FNAS.userUUID);
        share.setmTime(String.valueOf(System.currentTimeMillis()));
        share.setAlbum(false);
        dbUtils.insertLocalShare(share);

    }

    @Override
    public void onDidAppear() {

        refreshView();

    }

    private class PhotoListAdapter extends BaseAdapter {

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public Object getItem(int position) {
            if (mPhotoTitleLineNumberMap.containsKey(position))
                return mPhotoTitleLineNumberMap.get(position);
            else
                return mPhotoContentLineNumberMap.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mPhotoLineTotalCount;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {

            if (mPhotoTitleLineNumberMap.containsKey(position))
                return 0;
            else
                return 1;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

//            Log.i(TAG, "getView position:" + position);

            if (convertView == null) {

                if (getItemViewType(position) == 0) {
                    PhotoGroupHolder groupHolder;
                    convertView = LayoutInflater.from(containerActivity).inflate(R.layout.new_photo_title_item, parent, false);
                    groupHolder = new PhotoGroupHolder(convertView);
                    convertView.setTag(groupHolder);

                    groupHolder.refreshView(position);
                } else {
                    PhotoChildHolder childHolder;
                    convertView = LayoutInflater.from(containerActivity).inflate(R.layout.new_photo_content_item, parent, false);
                    childHolder = new PhotoChildHolder(convertView);
                    convertView.setTag(childHolder);

                    childHolder.refreshView(position);
                }


            } else {

                if (getItemViewType(position) == 0) {
                    PhotoGroupHolder groupHolder;
                    groupHolder = (PhotoGroupHolder) convertView.getTag();

                    groupHolder.refreshView(position);
                } else {
                    PhotoChildHolder childHolder;
                    childHolder = (PhotoChildHolder) convertView.getTag();

                    childHolder.refreshView(position);
                }

            }


            return convertView;
        }

    }

    class PhotoGroupHolder {

        @BindView(R.id.photo_group_tv)
        TextView mPhotoTitle;

        @BindView(R.id.photo_title_select_img)
        ImageView mPhotoTitleSelectImg;

        @BindView(R.id.photo_title_layout)
        LinearLayout mPhotoTitleLayout;

        public PhotoGroupHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(int groupPosition) {

            final String date = mPhotoTitleLineNumberMap.get(groupPosition);
            if (date.equals("1916-01-01")) {
                mPhotoTitle.setText(containerActivity.getString(R.string.unknown_time_text));
            } else {
                mPhotoTitle.setText(date);
            }

            if (mSelectMode) {
                mPhotoTitleSelectImg.setVisibility(View.VISIBLE);

                List<Photo> photoList = mPhotoMap.get(date);
                int selectNum = 0;
                for (Photo photo : photoList) {
                    if (photo.isSelected())
                        selectNum++;
                }
                if (selectNum == photoList.size())
                    mPhotoTitleSelectImg.setSelected(true);
                else
                    mPhotoTitleSelectImg.setSelected(false);

            } else {
                mPhotoTitleSelectImg.setVisibility(View.GONE);
            }

            mPhotoTitleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {

                        boolean selected = mPhotoTitleSelectImg.isSelected();
                        mPhotoTitleSelectImg.setSelected(!selected);
                        List<Photo> photoList = mPhotoMap.get(date);
                        for (Photo photo : photoList)
                            photo.setSelected(!selected);

                        mPhotoListAdapter.notifyDataSetChanged();

                        calcSelectedPhoto();

                        for (IPhotoListListener listListener : mPhotoListListenerList) {
                            listListener.onPhotoItemLongClick(mSelectCount);
                        }
                    }
                }
            });

        }

    }

    class PhotoChildHolder {

        @BindView(R.id.photo_gridlayout)
        GridLayout mPhotoGridLayout;

        public PhotoChildHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(int position) {
            List<Photo> mPhotoList = mPhotoContentLineNumberMap.get(position);

            int size = mPhotoList.size();
            if (size < mSpanCount) {
                for (int i = size; i < mSpanCount; i++) {
                    View view = mPhotoGridLayout.getChildAt(i);
                    if (view != null) {
                        mPhotoGridLayout.removeView(view);
                    }
                }
            }

            mPhotoGridLayout.setColumnCount(mPhotoList.size());

            for (int i = 0; i < mPhotoList.size(); i++) {

                final Photo photo = mPhotoList.get(i);

                PhotoHolder photoHolder;

                if (mPhotoGridLayout.getChildAt(i) == null) {
                    view = View.inflate(containerActivity, R.layout.new_photo_gridlayout_item, null);

                    mPhotoGridLayout.addView(view);

                    photoHolder = new PhotoHolder(view);
                    view.setTag(photoHolder);

                    photoHolder.refreshView(photo, position);

                } else {

                    view = mPhotoGridLayout.getChildAt(i);

                    photoHolder = (PhotoHolder) view.getTag();

/*                    if (mHasFlung && !mIsFling) {
                        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
                            @Override
                            public boolean apply(Request<?> request) {

                                int position = Integer.parseInt(String.valueOf(request.getTag()));
                                if (position < mFirstVisiableItem || position > mFirstVisiableItem + mVisiableItemCount) {
                                    return true;
                                } else {
                                    return false;
                                }

                            }
                        });
                    }*/

                    photoHolder.refreshView(photo, position);

                }

            }

        }

    }

    class PhotoHolder {

        @BindView(R.id.photo_iv)
        NetworkImageView mPhotoIv;

        @BindView(R.id.photo_item_layout)
        RelativeLayout mImageLayout;

        @BindView(R.id.photo_select_img)
        ImageView mPhotoSelectedIv;

        public PhotoHolder(View view) {

            ButterKnife.bind(this, view);
        }

        public void refreshView(final Photo photo, int position) {

            mImageLoader.setTag(position);

            if (photo.isCached() && !mIsFling) {
//                    LocalCache.LoadLocalBitmapThumb(photo.getUuid(), width, height, mPhotoIv);

                String url = photo.getThumb();

                mImageLoader.setShouldCache(false);
                mPhotoIv.setTag(url);
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(url, mImageLoader);

            } else if (!photo.isCached() && !mIsFling) {

//                    LocalCache.LoadRemoteBitmapThumb(photo.getUuid(), width, height, mPhotoIv);

                int width = Integer.parseInt(photo.getWidth());
                int height = Integer.parseInt(photo.getHeight());

                int[] result = Util.formatPhotoWidthHeight(width,height);

                String url = FNAS.Gateway + "/media/" + photo.getUuid() + "?type=thumb&width=" + result[0] + "&height=" + result[1];

                mImageLoader.setShouldCache(true);
                mPhotoIv.setTag(url);
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(url, mImageLoader);
            } else if (mIsFling) {
                mPhotoIv.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoIv.setImageUrl(null, mImageLoader);
            }

            GridLayout.LayoutParams params = (GridLayout.LayoutParams) view.getLayoutParams();
            params.width = mScreenWidth / mSpanCount;
            params.height = mScreenWidth / mSpanCount;
            params.setMargins(0, 0, dip2px(5), dip2px(5));
            view.setLayoutParams(params);

            if (mSelectMode) {
                boolean selected = photo.isSelected();
                RelativeLayout.LayoutParams photoParams = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
                if (selected) {
                    int margin = dip2px(20);
                    photoParams.setMargins(margin, margin, margin, margin);
                    mPhotoIv.setLayoutParams(photoParams);
                    mPhotoSelectedIv.setVisibility(View.VISIBLE);
                } else {
                    photoParams.setMargins(0, 0, 0, 0);
                    mPhotoIv.setLayoutParams(photoParams);
                    mPhotoSelectedIv.setVisibility(View.INVISIBLE);
                }
            } else {

                photo.setSelected(false);

                RelativeLayout.LayoutParams photoParams = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
                photoParams.setMargins(0, 0, 0, 0);
                mPhotoIv.setLayoutParams(photoParams);
                mPhotoSelectedIv.setVisibility(View.INVISIBLE);
            }

            mPhotoIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode) {
                        boolean selected = photo.isSelected();
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
                        if (selected) {
                            int margin = dip2px(20);
                            params.setMargins(margin, margin, margin, margin);
                            mPhotoIv.setLayoutParams(params);
                            mPhotoSelectedIv.setVisibility(View.VISIBLE);
                        } else {
                            params.setMargins(0, 0, 0, 0);
                            mPhotoIv.setLayoutParams(params);
                            mPhotoSelectedIv.setVisibility(View.INVISIBLE);
                        }

                        photo.setSelected(!selected);
                        mPhotoListAdapter.notifyDataSetChanged();

                        calcSelectedPhoto();

                        for (IPhotoListListener listListener : mPhotoListListenerList) {
                            listListener.onPhotoItemClick(mSelectCount);
                        }

                    } else {

                        int position = 0;

                        List<Photo> photoList = mPhotoMap.get(photo.getTitle());
                        List<Map<String, Object>> imgList = new ArrayList<>(photoList.size());
                        Map<String, Object> map;
                        for (int i = 0; i < photoList.size(); i++) {
                            Photo photo1 = photoList.get(i);
                            map = new HashMap<>();
                            map.put("cacheType", photo1.isCached() ? "local" : "nas");
                            map.put("resID", R.drawable.default_img);
                            map.put("resHash", photo1.getUuid());
                            map.put("thumb", photo1.getThumb());
                            map.put("width", photo1.getWidth());
                            map.put("height", photo1.getHeight());
                            map.put("uuid", photo1.getUuid());
                            map.put("mtime", photo1.getTime());
                            map.put("selected", photo1.isSelected() ? "1" : "0");
                            map.put("locked", "1");
                            imgList.add(map);

                            if (photo.getUuid().equals(photo1.getUuid())) {
                                position = i;
                            }
                        }

                        LocalCache.TransActivityContainer.put("imgSliderList", imgList);
                        Intent intent = new Intent();

//                        Log.i(TAG, "photo pos:" + position);
                        intent.putExtra("pos", position);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, false);
                        intent.setClass(containerActivity, PhotoSliderActivity.class);
                        containerActivity.startActivity(intent);

                    }

                    Log.i(TAG, "image digest:" + photo.getUuid());
                }
            });

            mPhotoIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    photo.setSelected(true);
                    mPhotoListAdapter.notifyDataSetChanged();

                    calcSelectedPhoto();

                    for (IPhotoListListener listListener : mPhotoListListenerList) {
                        listListener.onPhotoItemLongClick(mSelectCount);
                    }

                    return true;
                }
            });

        }
    }

    private class NewPhotoListScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mFirstVisiableItem = firstVisibleItem;
            mVisiableItemCount = visibleItemCount;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

            if (scrollState == SCROLL_STATE_FLING) {

                mHasFlung = true;
                mIsFling = true;

            } else if (scrollState == SCROLL_STATE_IDLE) {

                mIsFling = false;

                mPhotoListAdapter.notifyDataSetChanged();
            }
        }
    }
}
