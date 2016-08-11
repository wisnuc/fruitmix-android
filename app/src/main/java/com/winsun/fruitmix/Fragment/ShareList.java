package com.winsun.fruitmix.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
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

import com.winsun.fruitmix.AlbumPicContentActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.ShareCommentActivity;
import com.winsun.fruitmix.SplashScreenActivity;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/19.
 */
public class ShareList implements NavPagerActivity.Page {

    public static final String TAG = ShareList.class.getSimpleName();

    NavPagerActivity containerActivity;
    View view;
    LinearLayout mLoadingLayout;
    LinearLayout mNoContentLayout;

    public ListView mainListView;
    List<Map<String, Object>> shareList;

    Map<String, List<Comment>> mCommentMap;
    ShareListViewAdapter mAdapter;

    int mLoadCommentCount = 0;
    int mLoadCommentTotal = 0;

    private DBUtils dbUtils;

    public ShareList(NavPagerActivity activity_) {
        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(
                R.layout.share_list2, null);

        mainListView = (ListView) view.findViewById(R.id.mainList);
        mAdapter = new ShareListViewAdapter(this);
        mainListView.setAdapter(mAdapter);

        mLoadingLayout = (LinearLayout) view.findViewById(R.id.loading_layout);
        mNoContentLayout = (LinearLayout) view.findViewById(R.id.no_content_layout);

        mCommentMap = new HashMap<>();

        while (true) {
            try {
                Thread.sleep(2000);
                refreshView();
                break;
            } catch (Exception e) {
                try {
                    e.printStackTrace();
                    Thread.sleep(1000);
                } catch (Exception e1) {
                }
            }
        }
        //loadRemoteData();
    }

    public void reloadList() {
        List<Map<String, Object>> shareList1;
        Map<String, Object> albumItem;
        Map<String, String> albumRaw;
        String[] stArr;
        String coverImg;
        Map<String, Map<String, String>> albumsMap;

        shareList1 = new ArrayList<Map<String, Object>>();

        for (String key : LocalCache.DocumentsMap.keySet()) {
            albumRaw = LocalCache.DocumentsMap.get(key);
            if (albumRaw.get("del").equals("1")) continue;


            String creator = albumRaw.get("creator");

//            Log.i("ShareList",creator+"");
//            Log.i("ShareList",LocalCache.UsersMap.get(creator).get("name")+"");

            if (!LocalCache.UsersMap.containsKey(creator)) {
                continue;
            }

            albumItem = new HashMap<String, Object>();

            Map<String, String> map = LocalCache.UsersMap.get(creator);
            String avatar = map.get("avatar");
            if (avatar.equals("defaultAvatar.jpg")) {
                albumItem.put("avatar", map.get("avatar_default"));
                albumItem.put("avatar_default_color", map.get("avatar_default_color"));
            } else {
                albumItem.put("avatar", avatar);
            }

            albumItem.put("type", albumRaw.get("type"));
            albumItem.put("uuid", albumRaw.get("uuid"));
            albumItem.put("date", albumRaw.get("date"));
            albumItem.put("mtime", albumRaw.get("mtime"));
            albumItem.put("creator", albumRaw.get("creator"));
            StringBuilder images = new StringBuilder("");
            for (String image : albumRaw.get("images").split(",")) {

                if (LocalCache.MediasMap.containsKey(image) || LocalCache.LocalImagesMap2.containsKey(image)) {
                    images.append(image);
                    images.append(",");
                } else {
                    images.append("");
                }
            }
            albumItem.put("images", images.toString());

            if (albumRaw.get("maintained").equals("false")) {
                albumItem.put("maintained", false);
            } else {
                albumItem.put("maintained", true);
            }
            albumItem.put("private", albumRaw.get("private"));

            albumItem.put("creatorNick", LocalCache.UsersMap.get(albumRaw.get("creator")).get("name"));
            if (albumRaw.get("type").equals("album")) {
                albumItem.put("title", albumRaw.get("title"));
                if (albumItem.get("images").equals("")) {
                    albumItem.put("imageCount", 0);
                } else {
                    albumItem.put("imageCount", ((String) albumItem.get("images")).split(",").length);
                }

                if (((String) albumItem.get("images")).contains(","))
                    albumItem.put("coverImg", ((String) albumItem.get("images")).substring(0, ((String) albumItem.get("images")).indexOf(",")));
                else
                    albumItem.put("coverImg", albumItem.get("images"));

            }

            shareList1.add(albumItem);
        }

        Collections.sort(shareList1, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                Map<String, Object> map1, map2;
                map1 = (Map<String, Object>) lhs;
                map2 = (Map<String, Object>) rhs;

                long mtime1 = Long.parseLong(String.valueOf(map2.get("mtime")));
                long mtime2 = Long.parseLong(String.valueOf(map1.get("mtime")));

                if (mtime1 - mtime2 > 0) {
                    return 1;
                } else if (mtime1 - mtime2 < 0) {
                    return -1;
                } else
                    return 0;

            }
        });

        shareList = shareList1;

        Log.d(TAG, "shareList " + shareList);

    }


    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);

        reloadList();

        mLoadingLayout.setVisibility(View.INVISIBLE);
        if (shareList.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mainListView.setVisibility(View.INVISIBLE);
        } else {
            mNoContentLayout.setVisibility(View.INVISIBLE);
            mainListView.setVisibility(View.VISIBLE);
            ((BaseAdapter) (mainListView.getAdapter())).notifyDataSetChanged();

            loadLocalCommentList();
            //load comment
            if (Util.getNetworkState(containerActivity)) {

                for (Map<String, Object> map : shareList) {
                    if (!map.get("type").equals("album")) {
                        String[] images = map.get("images").toString().split(",");
                        if (images.length == 1) {
                            mLoadCommentTotal++;

                            Map<String, String> imageMap = LocalCache.MediasMap.get(images[0]);

                            if (imageMap != null && imageMap.containsKey("uuid")) {
                                loadCommentList(LocalCache.MediasMap.get(images[0]).get("uuid"));
                            }

                        }
                    }
                }

            }

        }
    }

    public void loadRemoteData() {

        new AsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                String str, date;
                JSONArray json;
                int i;
                JSONObject itemRaw;
                Map<String, String> item;

                try {

                    str = FNAS.RemoteCall("/document");


                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean sSuccess) {
                //if(sSuccess) ((BaseAdapter)(mainListView.getAdapter())).notifyDataSetChanged();
            }

        }.execute();
    }

    public void onDidAppear() {

        refreshView();

    }

    public View getView() {
        return view;
    }


    class ShareListViewAdapter extends BaseAdapter {

        ShareList container;

        public ShareListViewAdapter(ShareList container_) {
            container = container_;
        }

        @Override
        public int getCount() {
            if (container.shareList == null) return 0;
            return container.shareList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            GridView gvGrid;
            final Map<String, Object> currentItem;
            Map<String, String> coverImg, itemImg;
            TextView lbNick, lbTime, lbAlbumTitle;
            ImageView lbAlbumShare;
            ImageView ivCover;
            ImageView ivItems[];
            View rlAlbum, llPic1, llPic2, llPic3;
            boolean sLocal;
            int w, h, i;
            String images[];

            //add by liang.wu
            LinearLayout mShareCountLayout;
            RelativeLayout mShareCommentLayout;
            TextView mShareCountTextView;
            TextView mShareCommentTextView;
            TextView mShareCommentCountTextView;
            TextView mAvator;

            String nickName;

            if (convertView == null)
                view = LayoutInflater.from(container.containerActivity).inflate(R.layout.share_list_cell, parent, false);
            else view = convertView;

            currentItem = (Map<String, Object>) this.getItem(position);

            nickName = currentItem.get("creatorNick").toString();

            lbNick = (TextView) view.findViewById(R.id.nick);
            lbTime = (TextView) view.findViewById(R.id.time);
            ivCover = (ImageView) view.findViewById(R.id.cover_img);
            lbAlbumTitle = (TextView) view.findViewById(R.id.album_title);
            lbAlbumShare = (ImageView) view.findViewById(R.id.album_share);

            //add by liang.wu
            mShareCountLayout = (LinearLayout) view.findViewById(R.id.share_count_layout);
            mShareCommentLayout = (RelativeLayout) view.findViewById(R.id.share_comment_layout);
            mAvator = (TextView) view.findViewById(R.id.avatar);
            mShareCommentCountTextView = (TextView) view.findViewById(R.id.share_comment_count_textview);

            lbNick.setText(nickName);
            lbTime.setText(Util.formatTime(containerActivity, Long.parseLong(String.valueOf(currentItem.get("mtime")))));

            //add by liang.wu
            mAvator.setText(String.valueOf(currentItem.get("avatar")));
            int color = Integer.valueOf(String.valueOf(currentItem.get("avatar_default_color")));
            switch (color) {
                case 0:
                    mAvator.setBackgroundResource(R.drawable.user_portrait_bg_blue);
                    break;
                case 1:
                    mAvator.setBackgroundResource(R.drawable.user_portrait_bg_green);
                    break;
                case 2:
                    mAvator.setBackgroundResource(R.drawable.user_portrait_bg_yellow);
                    break;
            }

            rlAlbum = (View) view.findViewById(R.id.album_row);
            llPic1 = (View) view.findViewById(R.id.pic_row1);
            llPic2 = (View) view.findViewById(R.id.pic_row2);
            llPic3 = (View) view.findViewById(R.id.pic_row3);

            ivItems = new ImageView[9];
            ivItems[0] = (ImageView) view.findViewById(R.id.mainPic0);
            ivItems[1] = (ImageView) view.findViewById(R.id.mainPic1);
            ivItems[2] = (ImageView) view.findViewById(R.id.mainPic2);
            ivItems[3] = (ImageView) view.findViewById(R.id.mainPic3);
            ivItems[4] = (ImageView) view.findViewById(R.id.mainPic4);
            ivItems[5] = (ImageView) view.findViewById(R.id.mainPic5);
            ivItems[6] = (ImageView) view.findViewById(R.id.mainPic6);
            ivItems[7] = (ImageView) view.findViewById(R.id.mainPic7);
            ivItems[8] = (ImageView) view.findViewById(R.id.mainPic8);

            if (currentItem.get("type").equals("album")) {
                rlAlbum.setVisibility(View.VISIBLE);
                llPic1.setVisibility(View.GONE);
                llPic2.setVisibility(View.GONE);
                llPic3.setVisibility(View.GONE);

                mShareCommentLayout.setVisibility(View.GONE);
                mShareCountLayout.setVisibility(View.GONE);

                lbAlbumShare.setVisibility(View.VISIBLE);
                lbAlbumTitle.setVisibility(View.VISIBLE);

                Log.i(TAG, currentItem.get("images").toString());
                lbAlbumTitle.setText(String.format(containerActivity.getString(R.string.share_album_title), currentItem.get("title"), currentItem.get("imageCount")));

                coverImg = LocalCache.MediasMap.get(currentItem.get("coverImg"));
                if (coverImg != null) sLocal = false;
                else {
                    coverImg = LocalCache.LocalImagesMap2.get(currentItem.get("coverImg"));
                    sLocal = true;
                }
                if (coverImg != null) {
                    w = Integer.parseInt((String) coverImg.get("width"));
                    h = Integer.parseInt((String) coverImg.get("height"));
                    if (w >= h) {
                        w = w * 200 / h;
                        h = 200;
                    } else {
                        h = h * 200 / w;
                        w = 200;
                    }
                    if (sLocal) {
                        LocalCache.LoadLocalBitmapThumb(coverImg.get("thumb"), w, h, ivCover);
                    } else {
                        LocalCache.LoadRemoteBitmapThumb((String) (coverImg.get("uuid")), w, h, ivCover);
                    }
                } else {
                    ivCover.setImageResource(R.drawable.placeholder_photo);
                }

                ivCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(containerActivity, AlbumPicContentActivity.class);
                        intent.putExtra("images", (String) currentItem.get("images"));
                        intent.putExtra("uuid", (String) currentItem.get("uuid"));
                        intent.putExtra("title", (String) currentItem.get("title"));
                        intent.putExtra("desc", (String) currentItem.get("desc"));
                        intent.putExtra("maintained", (boolean) currentItem.get("maintained"));
                        intent.putExtra("private", (String) currentItem.get("private"));
                        intent.putExtra(Util.NEED_SHOW_MENU, false);
                        containerActivity.startActivity(intent);
                    }
                });

            } else {

                lbAlbumShare.setVisibility(View.GONE);
                lbAlbumTitle.setVisibility(View.GONE);

                // modify by liang.wu
                images = currentItem.get("images").toString().split(",");

                if (images.length == 1) {
                    rlAlbum.setVisibility(View.VISIBLE);
                    llPic1.setVisibility(View.GONE);
                    llPic2.setVisibility(View.GONE);
                    llPic3.setVisibility(View.GONE);

                    mShareCommentLayout.setVisibility(View.VISIBLE);
                    mShareCountLayout.setVisibility(View.GONE);

                    Log.i(TAG, "images[0]:" + images[0]);
                    itemImg = LocalCache.MediasMap.get(images[0]);
                    if (itemImg != null) sLocal = false;
                    else {
                        itemImg = LocalCache.LocalImagesMap2.get(images[0]);
                        sLocal = true;
                    }

                    if (itemImg != null) {
                        w = Integer.parseInt((String) itemImg.get("width"));
                        h = Integer.parseInt((String) itemImg.get("height"));
                        if (w >= h) {
                            w = w * 200 / h;
                            h = 200;
                        } else {
                            h = h * 200 / w;
                            w = 200;
                        }
                        if (sLocal) {
                            LocalCache.LoadLocalBitmapThumb(itemImg.get("thumb"), w, h, ivCover);
                        } else {
                            LocalCache.LoadRemoteBitmapThumb((String) (itemImg.get("uuid")), w, h, ivCover);
                        }

                        mShareCommentTextView = (TextView) view.findViewById(R.id.share_comment_textview);

                        String uuid = itemImg.get("uuid");
                        if (mCommentMap.containsKey(uuid)) {
                            List<Comment> commentList = mCommentMap.get(uuid);
                            if (commentList.size() != 0) {
                                mShareCommentTextView.setText(String.format(containerActivity.getString(R.string.share_comment_text), nickName, commentList.get(0).getText()));
                            }
                            mShareCommentCountTextView.setText(String.valueOf(commentList.size()));

                        }
                    } else {
                        ivCover.setImageResource(R.drawable.placeholder_photo);
                    }

                    ivCover.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LocalCache.TransActivityContainer.put("imgSliderList", getImgList((String) currentItem.get("images")));
                            Intent intent = new Intent();
                            intent.putExtra("pos", 0);
                            intent.setClass(containerActivity, PhotoSliderActivity.class);
                            containerActivity.startActivity(intent);
                        }
                    });

                } else {

                    if (images.length <= 3) {

                        rlAlbum.setVisibility(View.GONE);
                        llPic1.setVisibility(View.VISIBLE);
                        llPic2.setVisibility(View.GONE);
                        llPic3.setVisibility(View.GONE);

                    } else if (images.length <= 6) {
                        rlAlbum.setVisibility(View.GONE);
                        llPic1.setVisibility(View.VISIBLE);
                        llPic2.setVisibility(View.VISIBLE);
                        llPic3.setVisibility(View.GONE);

                    } else {
                        rlAlbum.setVisibility(View.GONE);
                        llPic1.setVisibility(View.VISIBLE);
                        llPic2.setVisibility(View.VISIBLE);
                        llPic3.setVisibility(View.VISIBLE);

                    }

                    mShareCommentLayout.setVisibility(View.GONE);
                    mShareCountLayout.setVisibility(View.VISIBLE);

                    mShareCountTextView = (TextView) view.findViewById(R.id.share_count_textview);

                    String shareCountText = String.format(containerActivity.getString(R.string.share_comment_count), images.length);
                    int start = shareCountText.indexOf(String.valueOf(images.length));
                    int end = start + String.valueOf(images.length).length();
                    SpannableStringBuilder builder = new SpannableStringBuilder(shareCountText);
                    ForegroundColorSpan span = new ForegroundColorSpan(containerActivity.getResources().getColor(R.color.light_black));
                    ForegroundColorSpan beforeSpan = new ForegroundColorSpan(containerActivity.getResources().getColor(R.color.light_gray));
                    ForegroundColorSpan afterSpan = new ForegroundColorSpan(containerActivity.getResources().getColor(R.color.light_gray));
                    builder.setSpan(beforeSpan, 0, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    builder.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    builder.setSpan(afterSpan, end, shareCountText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mShareCountTextView.setText(builder);

                    for (i = 0; i < 9; i++) {
                        if (i >= images.length) {
                            ivItems[i].setVisibility(View.INVISIBLE);
                            continue;
                        }
                        ivItems[i].setVisibility(View.VISIBLE);
                        itemImg = LocalCache.MediasMap.get(images[i]);
                        if (itemImg != null) sLocal = false;
                        else {
                            itemImg = LocalCache.LocalImagesMap2.get(images[i]);
                            sLocal = true;
                        }
                        //Log.d("winsun", "shareX "+position+" "+i+" "+itemImg);
                        if (itemImg != null) {
                            w = Integer.parseInt((String) itemImg.get("width"));
                            h = Integer.parseInt((String) itemImg.get("height"));
                            if (w >= h) {
                                w = w * 200 / h;
                                h = 200;
                            } else {
                                h = h * 200 / w;
                                w = 200;
                            }
                            if (sLocal) {
                                LocalCache.LoadLocalBitmapThumb(itemImg.get("thumb"), w, h, ivItems[i]);
                            } else {
                                LocalCache.LoadRemoteBitmapThumb((String) (itemImg.get("uuid")), w, h, ivItems[i]);
                            }
                        } else {
                            ivItems[i].setImageResource(R.drawable.placeholder_photo);
                        }

                        final int mItemPosition = i;

                        ivItems[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("winsun", currentItem + "");
                                if (currentItem.get("type").equals("album")) {
                                    Intent intent = new Intent();
                                    intent.setClass(containerActivity, AlbumPicContentActivity.class);
                                    intent.putExtra("images", (String) currentItem.get("images"));
                                    intent.putExtra("uuid", (String) currentItem.get("uuid"));
                                    intent.putExtra("title", (String) currentItem.get("title"));
                                    intent.putExtra("desc", (String) currentItem.get("desc"));
                                    intent.putExtra("maintained", (boolean) currentItem.get("maintained"));
                                    intent.putExtra("private", (String) currentItem.get("private"));
                                    containerActivity.startActivity(intent);
                                } else {
                                    Log.d("winsun", getImgList("images") + "");
                                    LocalCache.TransActivityContainer.put("imgSliderList", getImgList((String) currentItem.get("images")));
                                    Intent intent = new Intent();
                                    intent.putExtra("pos", mItemPosition);
                                    intent.setClass(containerActivity, PhotoSliderActivity.class);
                                    containerActivity.startActivity(intent);

                                }
                            }
                        });
                    }
                }

/*                if (images.length <= 3) llPic2.setVisibility(View.GONE);
                else llPic2.setVisibility(View.VISIBLE);
                if (images.length <= 6) llPic3.setVisibility(View.GONE);
                else llPic3.setVisibility(View.VISIBLE);*/
                Log.d("winsun", "shareX " + currentItem);

            }


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
            return container.shareList.get(position);
        }

        public List<Map<String, Object>> getImgList(String imagesStr) {
            List<Map<String, Object>> picList;
            Map<String, Object> picItem;
            Map<String, String> picItemRaw;
            String[] stArr;

            picList = new ArrayList<Map<String, Object>>();
            if (!imagesStr.equals("")) {
                stArr = imagesStr.split(",");
                Log.i(TAG, "stArr[0]" + stArr[0]);
                for (int i = 0; i < stArr.length; i++) {
                    picItem = new HashMap<String, Object>();
                    picItemRaw = LocalCache.LocalImagesMap2.get(stArr[i]);
                    if (picItemRaw != null) {
                        picItem.put("cacheType", "local");
                        picItem.put("resID", "" + R.drawable.default_img);
                        picItem.put("resHash", picItemRaw.get("resHash"));
                        picItem.put("thumb", picItemRaw.get("thumb"));
                        picItem.put("width", picItemRaw.get("width"));
                        picItem.put("height", picItemRaw.get("height"));
                        picItem.put("uuid", picItemRaw.get("uuid"));
                        picItem.put("mtime", picItemRaw.get("mtime"));
                        picItem.put("selected", "0");
                        picItem.put("locked", "1");
                        picList.add(picItem);
                    } else {
                        picItemRaw = LocalCache.MediasMap.get(stArr[i]);
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
                        }
                    }
                }
            }

            return picList;
        }
    }

    private void loadLocalCommentList() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                dbUtils = DBUtils.SINGLE_INSTANCE;
                mCommentMap = dbUtils.getAllRemoteImageComment();

                for (String imageUUid : mCommentMap.keySet()) {

                    List<Comment> list = mCommentMap.get(imageUUid);

                    List<Comment> localList = dbUtils.getLocalImageCommentByUUid(imageUUid);

                    list.addAll(localList);

                    for (Comment comment : list) {
                        Log.i(TAG, "remote comment:" + comment.toString());
                    }
                }

                Map<String, List<Comment>> localMap = dbUtils.getAllLocalImageComment();
                for (String uuid : localMap.keySet()) {

                    List<Comment> localList = localMap.get(uuid);

                    if (!mCommentMap.containsKey(uuid)) {
                        mCommentMap.put(uuid, localList);
                    }

                    for (Comment comment : localList) {
                        Log.i(TAG, "local comment:" + comment.toString());
                    }

                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {

                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void loadCommentList(String imageUuid) {

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {

                String str;
                JSONArray json;
                List<Comment> commentList;

                try {

                    commentList = mCommentMap.get(params[0]);
                    if (commentList == null) {
                        commentList = new ArrayList<>();
                    }

                    str = FNAS.RemoteCall("/media/" + params[0] + "?type=comments");
                    json = new JSONArray(str);

                    dbUtils = DBUtils.SINGLE_INSTANCE;

                    for (int i = 0; i < json.length(); i++) {
                        Comment comment = new Comment();
                        comment.setCreator(json.getJSONObject(i).getString("creator"));
                        comment.setTime(json.getJSONObject(i).getString("datatime"));
                        comment.setFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(json.getJSONObject(i).getString("datatime")))));
                        comment.setShareId(json.getJSONObject(i).getString("shareid"));
                        comment.setText(json.getJSONObject(i).getString("text"));
                        commentList.add(comment);

                        dbUtils.insertRemoteComment(comment, params[0]);
                    }

                    mCommentMap.put(params[0], commentList);

                    Log.d("winsun mCommentMap:", mCommentMap + "");
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {

                mLoadCommentCount++;
                if (mLoadCommentCount == mLoadCommentTotal) {
                    mAdapter.notifyDataSetChanged();
                }

            }
        }.execute(imageUuid);

    }
}

