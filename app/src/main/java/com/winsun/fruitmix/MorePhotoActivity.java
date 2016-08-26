package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class MorePhotoActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = MorePhotoActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.more_photo_gridview)
    RecyclerView mMorePhotoRecyclerView;

    private int mSpanCount = 3;
    private GridLayoutManager mManager;
    private Context mContext;
    private MorePhotoAdapter mAdapter;

    private String mImages;
    private List<Map<String, Object>> mPhotoList;

    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_photo);

        ButterKnife.bind(this);

        mImageLoader = new ImageLoader(RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue(), ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mBack.setOnClickListener(this);

        mContext = this;

        mImages = getIntent().getStringExtra("images");

        mManager = new GridLayoutManager(mContext, mSpanCount);
        mMorePhotoRecyclerView.setLayoutManager(mManager);
        mMorePhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MorePhotoAdapter();
        mMorePhotoRecyclerView.setAdapter(mAdapter);

        mPhotoList = new ArrayList<>();
        fillPhotoList(mImages);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void fillPhotoList(String selectedUIDStr) {

        mPhotoList.clear();

        if (!selectedUIDStr.equals("")) {
            String[] stArr = selectedUIDStr.split(",");
            Map<String, Object> picItem;
            ConcurrentMap<String, String> picItemRaw;
            for (String str : stArr) {
                picItem = new HashMap<>();
                picItemRaw = LocalCache.MediasMap.get(str);
                if (picItemRaw != null) {
                    picItem.put("cacheType", "nas");
                    picItem.put("resID", "" + R.drawable.default_img);
                    picItem.put("resHash", picItemRaw.get("uuid"));
                    picItem.put("thumb", picItemRaw.get("thumb"));
                    picItem.put("width", picItemRaw.get("width"));
                    picItem.put("height", picItemRaw.get("height"));
                    picItem.put("uuid", picItemRaw.get("uuid"));
                    picItem.put("mtime", picItemRaw.get("lastModified"));
                    picItem.put("selected", "0");
                    picItem.put("locked", "1");
                    mPhotoList.add(picItem);
                } else {
                    picItemRaw = LocalCache.LocalImagesMap2.get(str);
                    if (picItemRaw != null) {
                        picItem.put("cacheType", "local");
                        picItem.put("resID", "" + R.drawable.default_img);
                        picItem.put("thumb", picItemRaw.get("thumb"));
                        picItem.put("resHash", picItemRaw.get("uuid"));
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
            default:
        }
    }


    class MorePhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo_item)
        NetworkImageView mPhotoItem;
        @BindView(R.id.more_photo_item_layout)
        LinearLayout mMorelPhotoItemLayout;

        private Map<String, Object> mMap;
        private int width, height;

        public MorePhotoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(final int position) {
            mMap = mPhotoList.get(position);

            if (mMap.get("cacheType").equals("local")) {  // local bitmap path
//                LocalCache.LoadLocalBitmapThumb((String) mMap.get("thumb"), width, height, mPhotoItem);

                String url = String.valueOf(mMap.get("thumb"));

                mImageLoader.setShouldCache(false);
                mPhotoItem.setTag(url);
                mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoItem.setImageUrl(url, mImageLoader);

            } else if (mMap.get("cacheType").equals("nas")) {
//                LocalCache.LoadRemoteBitmapThumb((String) (mMap.get("resHash")), width, height, mPhotoItem);

                width = Integer.parseInt((String) mMap.get("width"));
                height = Integer.parseInt((String) mMap.get("height"));

                int[] result = Util.formatPhotoWidthHeight(width, height);

                String url = String.format(getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + mMap.get("resHash"), result[0], result[1]);
//                String url = FNAS.Gateway + "/media/" + mMap.get("resHash") + "?type=thumb&width=" + result[0] + "&height=" + result[1];

                mImageLoader.setShouldCache(true);
                mPhotoItem.setTag(url);
                mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
                mPhotoItem.setImageUrl(url, mImageLoader);
            }

            mMorelPhotoItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocalCache.TransActivityContainer.put("imgSliderList", mPhotoList);
                    Intent intent = new Intent();
                    intent.putExtra("pos", getAdapterPosition());
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    intent.setClass(mContext, PhotoSliderActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    class MorePhotoAdapter extends RecyclerView.Adapter<MorePhotoViewHolder> {
        @Override
        public int getItemCount() {
            return mPhotoList == null ? 0 : mPhotoList.size();
        }

        @Override
        public MorePhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.more_photo_item, parent, false);

            return new MorePhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MorePhotoViewHolder holder, int position) {

            holder.refreshView(position);
        }

    }
}
