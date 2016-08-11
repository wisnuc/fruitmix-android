package com.winsun.fruitmix.Fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winsun.fruitmix.CreateAlbumActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.ScrollbarPanelListView;
import com.winsun.fruitmix.db.DBUtils;
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

/**
 * Created by Administrator on 2016/4/19.
 */
public class PhotoList implements NavPagerActivity.Page {

    public static final String TAG = PhotoList.class.getSimpleName();

    NavPagerActivity containerActivity;
    View view;

    public ScrollbarPanelListView mainListView;
    private TextView mCurrentTimeTv;

    List<Map<String, Object>> dateList;
    List<Map<String, Object>> imgList;

    boolean sInRefresh = true;

    boolean sMenuUnfolding = false;

    private MainListViewAdapter mAdapter;

    private int mStartLoadIndex = 0;
    private int mStopLoadIndex = 0;
    private boolean mHaveFling = false;
    private CustomScrollListener mScrollListener;

    private ProgressDialog mDialog;

    private Animator mAnimator;

    public PhotoList(NavPagerActivity activity_) {
        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(
                R.layout.photo_list2, null);

        //Log.d("wisnun", ""+LocalCache.PhotoList("Camera"));
        //loadLocalData();
        //loadRemoteData();
        while (true) {
            try {
                Thread.sleep(2000);
                reloadList();
                break;
            } catch (Exception e) {
                try {
                    e.printStackTrace();
                    Thread.sleep(1000);
                } catch (Exception e1) {
                }
            }
        }


        mAdapter = new MainListViewAdapter(this);
        mainListView = (ScrollbarPanelListView) view.findViewById(R.id.mainList);
        mainListView.setAdapter(mAdapter);
        mScrollListener = new CustomScrollListener();
        mainListView.setOnScrollListener(mScrollListener);

        mainListView.setOnPositionChangedListener(new ScrollbarPanelListView.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(ScrollbarPanelListView listView, int position, View scrollBarPanel) {
                mCurrentTimeTv = (TextView) scrollBarPanel;

                int i, j, t, n = 0;
                for (i = 0; i < dateList.size(); i++) {
                    t = 1 + 1 + ((int) dateList.get(i).get("count") - 1) / 3;
                    if (n + t > position) {
                        if (position == n) j = -1;
                        else j = (position - n - 1) * 3;
                        break;
                    } else n += t;
                }

                String title = (String) dateList.get(i).get("title");
                if (title.equals("1916-01-01")) {
                    mCurrentTimeTv.setText(containerActivity.getString(R.string.unknown_time_text));
                } else {
                    String[] titleSplit = title.split("-");
                    String time = titleSplit[0] + "年" + titleSplit[1] + "月";
                    mCurrentTimeTv.setText(time);
                }
            }
        });

        /*
        ImageView iv = (ImageView) view.findViewById(R.id.aaa);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(containerActivity, PhotoSliderActivity.class);
                containerActivity.startActivity(intent);
            }
        });
        */

        containerActivity.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sMenuUnfolding) {
                    sMenuUnfolding = false;
                    collapseFab();

                } else {
                    sMenuUnfolding = true;
                    extendFab();
                }
            }
        });

    }

    private void collapseFab() {

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.fab_restore);
        mAnimator.setTarget(containerActivity.fab);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.album_btn_restore);
        mAnimator.setTarget(containerActivity.ivBtAlbum);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.share_btn_restore);
        mAnimator.setInterpolator(new BounceInterpolator());
        mAnimator.setTarget(containerActivity.ivBtShare);
        mAnimator.start();

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                containerActivity.ivBtAlbum.setVisibility(View.GONE);
                containerActivity.ivBtShare.setVisibility(View.GONE);

                Log.i(TAG, "share getTop:" + containerActivity.ivBtShare.getTop());
                Log.i(TAG, "share getTranslationY" + containerActivity.ivBtShare.getTranslationY());
                Log.i(TAG, "share getY:" + containerActivity.ivBtShare.getY());
            }
        });

    }

    private void extendFab() {

        containerActivity.ivBtAlbum.setVisibility(View.VISIBLE);
        containerActivity.ivBtShare.setVisibility(View.VISIBLE);

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.fab_remote);
        mAnimator.setTarget(containerActivity.fab);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.album_btn_translation);
        mAnimator.setTarget(containerActivity.ivBtAlbum);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.share_btn_translation);
        mAnimator.setInterpolator(new BounceInterpolator());
        mAnimator.setTarget(containerActivity.ivBtShare);
        mAnimator.start();

    }

    public void refreshView() {
        //Log.d("winsun", "inv");

        if (!containerActivity.sInChooseMode) {
            LocalCache.LoadLocalData();
            reloadList();
        }

        clearDataList();

        calcSelectAll();
        ((BaseAdapter) mainListView.getAdapter()).notifyDataSetChanged();
    }

    public void calcSelectAll() {
        int count1, count2, i, j;
        List<Map<String, Object>> imageList;

        for (i = 0; i < dateList.size(); i++) {
            imageList = (List<Map<String, Object>>) dateList.get(i).get("images");
            count1 = 0;
            count2 = 0;
            for (j = 0; j < imageList.size(); j++) {
                count1++;
                if (imageList.get(j).get("selected").equals("1")) count2++;
            }
            if (count1 == count2) dateList.get(i).put("selected", "1");
            else dateList.get(i).put("selected", "0");
        }
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

                mDialog = ProgressDialog.show(containerActivity, containerActivity.getString(R.string.loading_title), containerActivity.getString(R.string.loading_message), true, false);
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
                    containerActivity.onActivityResult(0, 201, null);
                } else {
                    Snackbar.make(mainListView, containerActivity.getString(R.string.operation_fail), Snackbar.LENGTH_SHORT).show();
                }
            }

        }.execute();
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
        Log.i("create share viewer:", viewer);

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

    public void reloadList() {

        Map<String, Map<String, Object>> dateMap;
        Map<String, String> itemRaw;
        Map<String, Object> dateItem, imageItem;
        List<Map<String, Object>> dateList1;
        List<Map<String, Object>> imgList1;
        String date;
        int i;

        // dateMap Tree
        dateMap = new HashMap<String, Map<String, Object>>();

        for (String key : LocalCache.LocalImagesMap.keySet()) {
            itemRaw = LocalCache.LocalImagesMap.get(key);
            date = itemRaw.get("mtime").substring(0, 10);
            dateItem = dateMap.get(date);
            if (dateItem == null) {
                dateItem = new HashMap<String, Object>();
                dateItem.put("title", date);
                dateItem.put("images", new ArrayList<Map<String, Object>>());
                dateItem.put("selected", "0");
                dateMap.put(date, dateItem);
            }
            imageItem = new HashMap<String, Object>();
            imageItem.put("cacheType", "local");
            imageItem.put("resID", "" + R.drawable.default_img);
            imageItem.put("resHash", itemRaw.get("uuid"));
            imageItem.put("thumb", itemRaw.get("thumb"));
            imageItem.put("width", itemRaw.get("width"));
            imageItem.put("height", itemRaw.get("height"));
            imageItem.put("uuid", itemRaw.get("uuid"));
            imageItem.put("mtime", itemRaw.get("mtime"));
            imageItem.put("selected", "0");
            imageItem.put("locked", "1");
            ((List<Map<String, Object>>) (dateItem.get("images"))).add(imageItem);
        }

        Log.d("winsun", "AAA " + LocalCache.MediasMap);

        for (String key : LocalCache.MediasMap.keySet()) {
            itemRaw = LocalCache.MediasMap.get(key);
//            Log.d("winsun", "" + itemRaw);
            date = itemRaw.get("mtime").substring(0, 10);
            dateItem = dateMap.get(date);
            if (dateItem == null) {
                dateItem = new HashMap<String, Object>();
                dateItem.put("title", date);
                dateItem.put("images", new ArrayList<Map<String, Object>>());
                dateItem.put("selected", "0");
                dateMap.put(date, dateItem);
            }
            imageItem = new HashMap<String, Object>();
            imageItem.put("cacheType", "nas");
            imageItem.put("resID", "" + R.drawable.default_img);
            imageItem.put("resHash", itemRaw.get("uuid"));
            imageItem.put("thumb", "");
            imageItem.put("width", itemRaw.get("width"));
            imageItem.put("height", itemRaw.get("height"));
            imageItem.put("uuid", itemRaw.get("uuid"));
            imageItem.put("mtime", itemRaw.get("mtime").replace("T", " ").replace("Z", " "));
            imageItem.put("selected", "0");
            imageItem.put("locked", "1");
            ((List<Map<String, Object>>) (dateItem.get("images"))).add(imageItem);
        }

        Log.d("winsun", "dateMap " + dateMap);

        // sort
        dateList1 = new ArrayList<Map<String, Object>>();
        for (String key : dateMap.keySet()) {
            dateList1.add(dateMap.get(key));
        }
        Collections.sort(dateList1, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                Map<String, Object> map1, map2;
                map1 = (Map<String, Object>) lhs;
                map2 = (Map<String, Object>) rhs;
                return -map1.get("title").toString().compareTo(map2.get("title").toString());
            }
        });
        dateList = dateList1;

        // count
        for (i = 0; i < dateList.size(); i++) {
            dateList.get(i).put("count", ((List<Map<String, Object>>) dateList.get(i).get("images")).size());
        }

        Log.d("winsun", "dateList " + dateList);

        // imageList
        imgList1 = new ArrayList<Map<String, Object>>();
        for (i = 0; i < dateList.size(); i++) {
            imgList1.addAll((List<Map<String, Object>>) dateList.get(i).get("images"));
        }
        imgList = imgList1;

    }

    public String getSelectedUIDString() {
        ArrayList<String> selectedArr;
        int i;
        String st;

        selectedArr = getSelectedUIDArray();
        st = "";
        for (i = 0; i < selectedArr.size(); i++) st += "," + selectedArr.get(i);
        if (st.startsWith(",")) st = st.substring(1);
        return st;
    }

    public ArrayList<String> getSelectedUIDArray() {
        ArrayList<String> selectedArr;
        List<Map<String, Object>> imgList;
        int i, j;

        selectedArr = new ArrayList<String>();

        for (i = 0; i < dateList.size(); i++) {
            imgList = (List<Map<String, Object>>) dateList.get(i).get("images");
            for (j = 0; j < imgList.size(); j++) {
                if (imgList.get(j).get("selected").equals("1"))
                    selectedArr.add(imgList.get(j).get("uuid").toString());
            }
        }

        Log.d("winsun", selectedArr + "");
        return selectedArr;
    }


    public void onDidAppear() {
        containerActivity.toolbar.setTitle(containerActivity.getString(R.string.photo_text));
        containerActivity.lbRight.setVisibility(View.VISIBLE);
        containerActivity.lbRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

/*                if (!Util.getNetworkState(containerActivity)) {
                    Toast.makeText(containerActivity, containerActivity.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }*/

                containerActivity.showChooseHeader();
            }
        });
        //containerActivity.fab.setVisibility(View.VISIBLE);
        //containerActivity.toolbar.setVisibility(View.GONE);
        //containerActivity.toolbar.setNavigationIcon(R.drawable.menu);
        /*
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("winsun", "CCCC 1");
            }
        });*/
        /*
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("winsun", "CCCC 1");



            }
        });*/
    }

    public View getView() {
        return view;
    }

    private void clearDataList() {
        if (!containerActivity.sInChooseMode) {
            for (Map<String, Object> data : dateList) {
                List<Map<String, Object>> imageList = (List<Map<String, Object>>) data.get("images");
                for (Map<String, Object> image : imageList) {
                    image.put("selected", "0");
                }
            }
        }
    }

    class MainListViewAdapter extends BaseAdapter {

        PhotoList container;
        int nCols = 3;

        public MainListViewAdapter(PhotoList container_) {
            container = container_;
        }

        @Override
        public int getCount() {
            int i, n;

            if (container.dateList == null) return 0;
            n = 0;
            for (i = 0; i < container.dateList.size(); i++) {
                n += 1 + 1 + ((int) container.dateList.get(i).get("count") - 1) / 3;
            }
            return n;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            TextView lbTitle;
            GridView gvGrid;
            final Map<String, Object> currentItem;
            final Map<String, Object> currentSubItem;
            final Map<String, Object> currentSubItem1, currentSubItem2, currentSubItem3;
            final List<Map<String, Object>> currentSubItems;
            ImageGridViewAdapter adapter;
            List<ImageView> ivMainPics, ivLocks, ivSelects;
            ImageView ivSelectAll, ivMainPic, ivLock, ivSelect;
            RelativeLayout llHeader;
            LinearLayout llContent;

            ivMainPics = new ArrayList<ImageView>();
            ivLocks = new ArrayList<ImageView>();
            ivSelects = new ArrayList<ImageView>();
            currentSubItems = new ArrayList<Map<String, Object>>();

            int i, j, k, n, t, w, h;

            if (convertView == null)
                view = LayoutInflater.from(container.containerActivity).inflate(R.layout.photo_list_cell2, parent, false);
            else view = convertView;

            llHeader = (RelativeLayout) view.findViewById(R.id.header);
            llContent = (LinearLayout) view.findViewById(R.id.content);

            n = 0;
            j = 0;
            for (i = 0; i < container.dateList.size(); i++) {
                t = 1 + 1 + ((int) container.dateList.get(i).get("count") - 1) / 3;
                if (n + t > position) {
                    if (position == n) j = -1;
                    else j = (position - n - 1) * 3;
                    break;
                } else n += t;
            }

            currentItem = (Map<String, Object>) this.getItem(i);

            if (j == -1) {
                llHeader.setVisibility(View.VISIBLE);
                llContent.setVisibility(View.GONE);
                ivSelectAll = (ImageView) view.findViewById(R.id.selectAll);

                lbTitle = (TextView) view.findViewById(R.id.title);
                String title = (String) currentItem.get("title");
                if (title.equals("1916-01-01")) {
                    lbTitle.setText(containerActivity.getString(R.string.unknown_time_text));
                } else {
                    lbTitle.setText((String) currentItem.get("title"));
                }

                if (container.containerActivity.sInChooseMode) {
                    ivSelectAll.setVisibility(View.VISIBLE);
                    if (currentItem.get("selected").equals("1"))
                        ivSelectAll.setImageResource(R.drawable.select);
                    else ivSelectAll.setImageResource(R.drawable.unselected);
                } else ivSelectAll.setVisibility(View.GONE);

                llHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Map<String, Object>> imageList;
                        int i;
                        if (container.containerActivity.sInChooseMode) {
                            imageList = (List<Map<String, Object>>) currentItem.get("images");
                            if (currentItem.get("selected").equals("1")) {
                                for (i = 0; i < imageList.size(); i++)
                                    imageList.get(i).put("selected", "0");
                            } else {
                                for (i = 0; i < imageList.size(); i++)
                                    imageList.get(i).put("selected", "1");
                            }
                        }
                        calcSelectAll();
                        ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();
                    }
                });
            } else {
                llHeader.setVisibility(View.GONE);
                llContent.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams param = llContent.getLayoutParams();
                param.height = LocalCache.ScreenWidth / 3;
                llContent.setLayoutParams(param);

                for (k = 0; k < nCols; k++) {

                    if (k == 0) {
                        ivMainPics.add((ImageView) view.findViewById(R.id.mainPic0));
                        ivLocks.add((ImageView) view.findViewById(R.id.lock0));
                        ivSelects.add((ImageView) view.findViewById(R.id.select0));
                    } else if (k == 1) {
                        ivMainPics.add((ImageView) view.findViewById(R.id.mainPic1));
                        ivLocks.add((ImageView) view.findViewById(R.id.lock1));
                        ivSelects.add((ImageView) view.findViewById(R.id.select1));
                    } else if (k == 2) {
                        ivMainPics.add((ImageView) view.findViewById(R.id.mainPic2));
                        ivLocks.add((ImageView) view.findViewById(R.id.lock2));
                        ivSelects.add((ImageView) view.findViewById(R.id.select2));
                    }

                    if (((List<Map<String, Object>>) currentItem.get("images")).size() > j + k) {
                        currentSubItems.add(((List<Map<String, Object>>) currentItem.get("images")).get(j + k));

                        w = Integer.parseInt((String) currentSubItems.get(k).get("width"));
                        h = Integer.parseInt((String) currentSubItems.get(k).get("height"));
                        if (w >= h) {
                            w = w * 100 / h;
                            h = 100;
                        } else {
                            h = h * 100 / w;
                            w = 100;
                        }

                        if (currentSubItems.get(k).get("cacheType").equals("local") && !mHaveFling) {  // local bitmap path
                            LocalCache.LoadLocalBitmapThumb((String) currentSubItems.get(k).get("thumb"), w, h, ivMainPics.get(k));
                        } else if (currentSubItems.get(k).get("cacheType").equals("nas") && !mHaveFling) {
                            LocalCache.LoadRemoteBitmapThumb((String) (currentSubItems.get(k).get("resHash")), w, h, ivMainPics.get(k));
                        }
                        ivMainPics.get(k).setVisibility(View.VISIBLE);

                        ivMainPics.get(k).setLongClickable(true);
//                        ivMainPics.get(k).setOnLongClickListener(new View.OnLongClickListener() {
//                            @Override
//                            public boolean onLongClick(View v) {
//                                container.containerActivity.showChooseHeader();
//
//                                Log.i("winsun :","onLongClick");
//
//                                return true;
//                            }
//                        });

                        if (container.containerActivity.sInChooseMode) {
                            if (currentSubItems.get(k).get("locked").equals("1"))
                                ivLocks.get(k).setVisibility(View.GONE);
                            else ivLocks.get(k).setVisibility(View.VISIBLE);
                            if (currentSubItems.get(k).get("selected").equals("1")) {
                                ivSelects.get(k).setVisibility(View.VISIBLE);
                                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ivMainPics.get(k).getLayoutParams();
                                lp.setMargins(20, 20, 20, 20);
                                ivMainPics.get(k).setLayoutParams(lp);
                            } else {
                                ivSelects.get(k).setVisibility(View.GONE);
                                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ivMainPics.get(k).getLayoutParams();
                                lp.setMargins(2, 2, 2, 2);
                                ivMainPics.get(k).setLayoutParams(lp);
                            }
                        } else {
                            ivLocks.get(k).setVisibility(View.GONE);
                            ivSelects.get(k).setVisibility(View.GONE);

                            ivSelects.get(k).setVisibility(View.GONE);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ivMainPics.get(k).getLayoutParams();
                            lp.setMargins(2, 2, 2, 2);
                            ivMainPics.get(k).setLayoutParams(lp);

                        }

                    } else {
                        ivMainPics.get(k).setVisibility(View.INVISIBLE);
                        ivLocks.get(k).setVisibility(View.INVISIBLE);
                        ivSelects.get(k).setVisibility(View.INVISIBLE);
                        ivMainPics.get(k).setLongClickable(false);
                    }

                }

                if (currentSubItems.size() > 0 && currentSubItems.get(0) == null)
                    ivMainPics.get(0).setOnClickListener(null);
                else {
                    ivMainPics.get(0).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int i;

                            if (container.containerActivity.sInChooseMode) {
                                if (currentSubItems.get(0).get("selected").equals("1"))
                                    currentSubItems.get(0).put("selected", "0");
                                else currentSubItems.get(0).put("selected", "1");
                                Log.i("onClick0:", currentSubItems.get(0) + "");
                                calcSelectAll();
                                ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();
                            } else {
                                LocalCache.TransActivityContainer.put("imgSliderList", imgList);
                                Intent intent = new Intent();
                                for (i = 0; i < container.imgList.size(); i++)
                                    if (container.imgList.get(i) == currentSubItems.get(0)) break;
                                intent.putExtra("pos", i);
                                intent.setClass(containerActivity, PhotoSliderActivity.class);
                                containerActivity.startActivity(intent);
                            }

                            if (sMenuUnfolding) {
                                sMenuUnfolding = false;
                                collapseFab();

                            }
                        }
                    });

                    ivMainPics.get(0).setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            containerActivity.showChooseHeader();

                            currentSubItems.get(0).put("selected", "1");
                            calcSelectAll();
                            ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();

                            return true;
                        }
                    });
                }

                if (currentSubItems.size() > 1 && currentSubItems.get(1) == null)
                    ivMainPics.get(1).setOnClickListener(null);
                else {
                    ivMainPics.get(1).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int i;

                            if (container.containerActivity.sInChooseMode) {
                                if (currentSubItems.get(1).get("selected").equals("1"))
                                    currentSubItems.get(1).put("selected", "0");
                                else currentSubItems.get(1).put("selected", "1");
                                Log.i("onClick1:", currentSubItems.get(1) + "");

                                calcSelectAll();
                                ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();
                            } else {
                                LocalCache.TransActivityContainer.put("imgSliderList", imgList);
                                Intent intent = new Intent();
                                for (i = 0; i < container.imgList.size(); i++)
                                    if (container.imgList.get(i) == currentSubItems.get(1)) break;
                                intent.putExtra("pos", i);
                                intent.setClass(containerActivity, PhotoSliderActivity.class);
                                containerActivity.startActivity(intent);
                            }

                            if (sMenuUnfolding) {
                                sMenuUnfolding = false;
                                collapseFab();

                            }
                        }
                    });

                    ivMainPics.get(1).setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            containerActivity.showChooseHeader();

                            currentSubItems.get(1).put("selected", "1");
                            calcSelectAll();
                            ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();

                            return true;
                        }
                    });
                }

                if (currentSubItems.size() > 2 && currentSubItems.get(2) == null)
                    ivMainPics.get(2).setOnClickListener(null);
                else {
                    ivMainPics.get(2).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int i;

                            if (container.containerActivity.sInChooseMode) {
                                if (currentSubItems.get(2).get("selected").equals("1"))
                                    currentSubItems.get(2).put("selected", "0");
                                else currentSubItems.get(2).put("selected", "1");
                                Log.i("onClick2:", currentSubItems.get(2) + "");

                                calcSelectAll();
                                ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();
                            } else {
                                LocalCache.TransActivityContainer.put("imgSliderList", imgList);
                                Intent intent = new Intent();
                                for (i = 0; i < container.imgList.size(); i++)
                                    if (container.imgList.get(i) == currentSubItems.get(2)) break;
                                intent.putExtra("pos", i);
                                intent.setClass(containerActivity, PhotoSliderActivity.class);
                                containerActivity.startActivity(intent);
                            }

                            if (sMenuUnfolding) {
                                sMenuUnfolding = false;
                                collapseFab();

                            }
                        }
                    });

                    ivMainPics.get(2).setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            containerActivity.showChooseHeader();

                            currentSubItems.get(2).put("selected", "1");
                            calcSelectAll();
                            ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();

                            return true;
                        }
                    });
                }
            }


            /*
            currentItem=(Map<String, Object>)this.getItem(position);



            gvGrid=(GridView)view.findViewById(R.id.grid);
            if(currentItem.get("adapter")==null) {
                adapter=new ImageGridViewAdapter(container, currentItem);
                currentItem.put("adapter", adapter);
                gvGrid.setAdapter(adapter);
            }
            else if(gvGrid.getAdapter()!=currentItem.get("adapter")) {
                gvGrid.setAdapter((ImageGridViewAdapter)currentItem.get("adapter"));
            }
            else if(sInRefresh) {
                ((BaseAdapter)gvGrid.getAdapter()).notifyDataSetChanged();
            }


            */
            return view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return container.dateList.get(position);
        }
    }


    class ImageGridViewAdapter extends BaseAdapter {

        PhotoList container;
        Map<String, Object> dateItem;

        public ImageGridViewAdapter(PhotoList container_, Map<String, Object> dateItem_) {
            container = container_;
            dateItem = dateItem_;
        }

        @Override
        public int getCount() {
            return ((List<Map<String, Object>>) dateItem.get("images")).size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            ImageView ivLock, ivSelect, ivMain;
            final Map<String, Object> currentItem;
            int w, h;

            if (convertView == null)
                view = LayoutInflater.from(container.containerActivity).inflate(R.layout.photo_list_cell_cell, parent, false);
            else view = convertView;
            //Log.d("winsun", "inv2");
            currentItem = (Map<String, Object>) this.getItem(position);
            ivMain = (ImageView) view.findViewById(R.id.mainPic);
            ivLock = (ImageView) view.findViewById(R.id.lock);
            ivSelect = (ImageView) view.findViewById(R.id.select);

            w = Integer.parseInt((String) currentItem.get("width"));
            h = Integer.parseInt((String) currentItem.get("height"));
            if (w >= h) {
                w = w * 100 / h;
                h = 100;
            } else {
                h = h * 100 / w;
                w = 100;
            }

            if (currentItem.get("cacheType").equals("local")) {  // local bitmap path
                LocalCache.LoadLocalBitmapThumb((String) currentItem.get("thumb"), w, h, ivMain);
            } else if (currentItem.get("cacheType").equals("nas")) {
                LocalCache.LoadRemoteBitmapThumb((String) (currentItem.get("resHash")), w, h, ivMain);
            }

            ivMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i;

                    if (container.containerActivity.sInChooseMode) {
                        if (currentItem.get("selected").equals("1"))
                            currentItem.put("selected", "0");
                        else currentItem.put("selected", "1");
                        calcSelectAll();
                        ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();
                    } else {
                        LocalCache.TransActivityContainer.put("imgSliderList", imgList);
                        Intent intent = new Intent();
                        for (i = 0; i < container.imgList.size(); i++)
                            if (container.imgList.get(i) == currentItem) break;
                        intent.putExtra("pos", i);
                        intent.setClass(containerActivity, PhotoSliderActivity.class);
                        containerActivity.startActivity(intent);
                    }
                }
            });

            ivMain.setLongClickable(true);
            ivMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    container.containerActivity.showChooseHeader();
                    return true;
                }
            });


            //LocalCache.LoadLocalBitmap("temp0001", ivMain);
            //LocalCache.LoadRemoteBitmapThumb((String) (currentItem.get("resHash")), ivMain);

            return view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return ((List<Map<String, Object>>) dateItem.get("images")).get(position);
        }
    }

    //add by liang.wu
    private class CustomScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

            if (scrollState == SCROLL_STATE_FLING) {

                mHaveFling = true;

            } else if (scrollState == SCROLL_STATE_IDLE) {

                int startLoadIndex = mStartLoadIndex;
                int stopLoadIndex = mStopLoadIndex;
                int n, j, i, t, w, h;
                List<ImageView> ivMainPics;

                for (; startLoadIndex < stopLoadIndex; startLoadIndex++) {
                    n = 0;
                    j = 0;
                    for (i = 0; i < dateList.size(); i++) {
                        t = 1 + 1 + ((int) dateList.get(i).get("count") - 1) / 3;
                        if (n + t > startLoadIndex) {
                            if (startLoadIndex == n) j = -1;
                            else j = (startLoadIndex - n - 1) * 3;
                            break;
                        } else n += t;
                    }

                    Map<String, Object> currentItem = (Map<String, Object>) mAdapter.getItem(i);
                    List<Map<String, Object>> currentSubItems = new ArrayList<>();
                    ivMainPics = new ArrayList<>();

                    if (j != -1) {
                        for (int k = 0; k < 3; k++) {

                            if (k == 0) {
                                ivMainPics.add((ImageView) view.findViewById(R.id.mainPic0));
                            } else if (k == 1) {
                                ivMainPics.add((ImageView) view.findViewById(R.id.mainPic1));
                            } else if (k == 2) {
                                ivMainPics.add((ImageView) view.findViewById(R.id.mainPic2));
                            }

                            if (((List<Map<String, Object>>) currentItem.get("images")).size() > j + k) {

                                currentSubItems.add(((List<Map<String, Object>>) currentItem.get("images")).get(j + k));

                                w = Integer.parseInt((String) currentSubItems.get(k).get("width"));
                                h = Integer.parseInt((String) currentSubItems.get(k).get("height"));
                                if (w >= h) {
                                    w = w * 100 / h;
                                    h = 100;
                                } else {
                                    h = h * 100 / w;
                                    w = 100;
                                }

                                if (currentSubItems.get(k).get("cacheType").equals("local")) {  // local bitmap path
                                    LocalCache.LoadLocalBitmapThumb((String) currentSubItems.get(k).get("thumb"), w, h, ivMainPics.get(k));
                                } else if (currentSubItems.get(k).get("cacheType").equals("nas")) {
                                    LocalCache.LoadRemoteBitmapThumb((String) (currentSubItems.get(k).get("resHash")), w, h, ivMainPics.get(k));
                                }

                            }
                        }
                    }

                }
            }

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            mStartLoadIndex = firstVisibleItem;
            mStopLoadIndex = firstVisibleItem + visibleItemCount;

            if (mStopLoadIndex > totalItemCount) {
                mStopLoadIndex = totalItemCount - 1;
            }

        }
    }

}
