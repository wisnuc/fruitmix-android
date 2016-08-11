package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/5/9.
 */
public class AlbumPicChooseActivity extends Activity {

    ImageView ivBack;
    TextView tfOK;

    public ListView mainListView;
    List<Map<String, Object>> dateList;
    List<Map<String, Object>> imgList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_pic_choose);

        ivBack = (ImageView) findViewById(R.id.back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tfOK = (TextView) findViewById(R.id.ok);
        tfOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getIntent();

                if (intent.getBooleanExtra(Util.EDIT_PHOTO, false)) {

                    getIntent().putExtra("selectedUIDStr", getSelectedUIDString());
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    intent = new Intent();
                    intent.setClass(AlbumPicChooseActivity.this, CreateAlbumActivity.class);
                    intent.putExtra("selectedUIDStr", getSelectedUIDString());
                    startActivityForResult(intent, 100);
                }

            }
        });

        reloadList();

        mainListView = (ListView) findViewById(R.id.mainList);
        mainListView.setAdapter(new MainListViewAdapter());

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == 200) {
            setResult(200);
            finish();
        }
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

        Log.d("winsun selectedArr", selectedArr + "");
        return selectedArr;
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

        for (String key : LocalCache.MediasMap.keySet()) {
            itemRaw = LocalCache.MediasMap.get(key);
            Log.d("winsun", "" + itemRaw);
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
        List<Map<String, Object>> dateList;
    }


    class MainListViewAdapter extends BaseAdapter {

        AlbumPicChooseActivity container;
        int nCols = 3;

        public MainListViewAdapter() {
            container = AlbumPicChooseActivity.this;
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
            Map<String, Object> currentSubItem;
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
                view = LayoutInflater.from(container).inflate(R.layout.photo_list_cell2, parent, false);
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
                if(title.equals("1916-01-01")){
                    lbTitle.setText(getString(R.string.unknown_time_text));
                }else {
                    lbTitle.setText((String) currentItem.get("title"));
                }

                ivSelectAll.setVisibility(View.VISIBLE);
                if (currentItem.get("selected").equals("1"))
                    ivSelectAll.setImageResource(R.drawable.select);
                else ivSelectAll.setImageResource(R.drawable.unselected);

                llHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Map<String, Object>> imageList;
                        int i;
                        imageList = (List<Map<String, Object>>) currentItem.get("images");
                        if (currentItem.get("selected").equals("1")) {
                            for (i = 0; i < imageList.size(); i++)
                                imageList.get(i).put("selected", "0");
                        } else {
                            for (i = 0; i < imageList.size(); i++)
                                imageList.get(i).put("selected", "1");
                        }
                        calcSelectAll();
                        ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();
                    }
                });
            } else {
                llHeader.setVisibility(View.GONE);
                llContent.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams param = llContent.getLayoutParams();
                param.height = LocalCache.ScreenWidth/3;
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

                        if (currentSubItems.get(k).get("cacheType").equals("local")) {  // local bitmap path
                            LocalCache.LoadLocalBitmapThumb((String) currentSubItems.get(k).get("thumb"), w, h, ivMainPics.get(k));
                        } else if (currentSubItems.get(k).get("cacheType").equals("nas")) {
                            LocalCache.LoadRemoteBitmapThumb((String) (currentSubItems.get(k).get("resHash")), w, h, ivMainPics.get(k));
                        }
                        ivMainPics.get(k).setVisibility(View.VISIBLE);

                        ivMainPics.get(k).setLongClickable(true);
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

                            if (currentSubItems.get(0).get("selected").equals("1"))
                                currentSubItems.get(0).put("selected", "0");
                            else currentSubItems.get(0).put("selected", "1");
                            calcSelectAll();
                            ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();
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

                            if (currentSubItems.get(1).get("selected").equals("1"))
                                currentSubItems.get(1).put("selected", "0");
                            else currentSubItems.get(1).put("selected", "1");
                            calcSelectAll();
                            ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();

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

                            if (currentSubItems.get(2).get("selected").equals("1"))
                                currentSubItems.get(2).put("selected", "0");
                            else currentSubItems.get(2).put("selected", "1");
                            calcSelectAll();
                            ((BaseAdapter) container.mainListView.getAdapter()).notifyDataSetChanged();

                        }
                    });
                }
            }


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

        AlbumPicChooseActivity containerActivity;
        Map<String, Object> dateItem;

        public ImageGridViewAdapter(AlbumPicChooseActivity containerActivity_, Map<String, Object> dateItem_) {
            containerActivity = containerActivity_;
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
                view = LayoutInflater.from(containerActivity).inflate(R.layout.photo_list_cell_cell, parent, false);
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

                    if (currentItem.get("selected").equals("1")) currentItem.put("selected", "0");
                    else currentItem.put("selected", "1");
                    calcSelectAll();
                    ((BaseAdapter) containerActivity.mainListView.getAdapter()).notifyDataSetChanged();


                }
            });

            if (currentItem.get("locked").equals("1")) ivLock.setVisibility(View.VISIBLE);
            else ivLock.setVisibility(View.GONE);
            if (currentItem.get("selected").equals("1")) ivSelect.setVisibility(View.VISIBLE);
            else ivSelect.setVisibility(View.GONE);

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
}
