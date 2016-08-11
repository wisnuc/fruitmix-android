package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.services.LocalShareService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/4/28.
 */
public class CreateAlbumActivity extends AppCompatActivity {

    public static final String TAG = CreateAlbumActivity.class.getSimpleName();

    TextInputLayout mTitleLayout;
    TextInputEditText tfTitle, tfDesc;
    CheckBox ckPublic;
    CheckBox ckSetMaintainer;
    TextView btOK;
    ImageView ivBack;
    TextView mLayoutTitle;

    String selectedUIDStr;

    private Context mContext;

    private ProgressDialog mDialog;

    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        selectedUIDStr = getIntent().getStringExtra("selectedUIDStr");
        Log.d("winsun", selectedUIDStr);
        setContentView(R.layout.activity_create_album);

        tfTitle = (TextInputEditText) findViewById(R.id.title_edit);
        mTitleLayout = (TextInputLayout) findViewById(R.id.title_textlayout);
        tfDesc = (TextInputEditText) findViewById(R.id.desc);
        ckPublic = (CheckBox) findViewById(R.id.sPublic);
        btOK = (TextView) findViewById(R.id.ok);
        ivBack = (ImageView) findViewById(R.id.back);
        ckSetMaintainer = (CheckBox) findViewById(R.id.set_maintainer);
        mLayoutTitle = (TextView) findViewById(R.id.layout_title);
        mLayoutTitle.setText(getString(R.string.create_album_text));

        mTitle = String.format(getString(R.string.title_hint), new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(new Date(System.currentTimeMillis())));
        mTitleLayout.setHint(mTitle);

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Log.d("winsun", tfTitle.getText().toString()+" "+tfDesc.getText().toString()+" "+ckPrivate.isChecked());
                /*
                Map<String, String> item;
                item=new HashMap<String, String>();
                item.put("type", "normal");
                item.put("title", tfTitle.getText().toString());
                item.put("desc", tfDesc.getText().toString());
                item.put("permission", ckPublic.isChecked()?"public":"");
                item.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                item.put("uuid", UUID.randomUUID().toString());
                item.put("images", selectedUIDStr);
                LocalCache.AlbumsMap.put(item.get("uuid"), item);
                if(1==1) {
                    setResult(200);
                    finish();
                    return;
                }
                */

                final boolean sPublic, sSetMaintainer;
                final String title, desc;

                sPublic = ckPublic.isChecked();
                sSetMaintainer = ckSetMaintainer.isChecked();

                if (tfTitle.getText().toString().equals("")) {
                    title = mTitleLayout.getHint().toString();
                } else {
                    title = tfTitle.getText().toString();
                }

                desc = tfDesc.getText().toString();

                new AsyncTask<Object, Object, Boolean>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        mDialog = ProgressDialog.show(mContext, getString(R.string.loading_title), getString(R.string.loading_message), true, false);
                    }

                    @Override
                    protected Boolean doInBackground(Object... params) {
                        String data, viewers, maintainers;
                        String[] selectedUIDArr;
                        int i;

                        selectedUIDArr = selectedUIDStr.split(",");
                        data = "";
                        for (i = 0; i < selectedUIDArr.length; i++) {
                            data += ",{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"" + selectedUIDArr[i] + "\\\"}";
                        }

                        viewers = "";
                        if (sPublic) {
                            for (String key : LocalCache.UsersMap.keySet()) {
                                viewers += ",\\\"" + key + "\\\"";
                            }
                        } else viewers = ",";
                        if (viewers.length() == 0) {
                            viewers += ",";
                        }

                        Log.i("winsun ,viewers:", viewers);

                        if (sSetMaintainer) {
                            maintainers = viewers;
                        } else maintainers = ",\\\"" + FNAS.userUUID + "\\\"";

                        Log.i("winsun ,miantianers:", maintainers);

                        createAlbumInLocalAlbumDatabase(sPublic, sSetMaintainer, title, desc, selectedUIDStr);
                        FNAS.loadLocalShare();

                        return true;

//                        data = "{\"album\":true, \"archived\":false,\"maintainers\":\"[" + maintainers.substring(1) + "]\",\"viewers\":\"[" + viewers.substring(1) + "]\",\"tags\":[{\"albumname\":\"" + title + "\",\"desc\":\"" + desc + "\"}],\"contents\":\"[" + data.substring(1) + "]\"}";
//                        Log.d("winsun", data);
//                        try {
//                            FNAS.PostRemoteCall("/mediashare", data);
//                            FNAS.LoadDocuments();
//                            return true;
//                        } catch (Exception e) {
//                            return false;
//                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean sSuccess) {

                        mDialog.dismiss();
                        if (Util.getNetworkState(mContext)) {
                            LocalShareService.startActionLocalShareTask(mContext);
                        }

                        if (sSuccess) {
                            setResult(200);
                            finish();
                        } else {

                            Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                            setResult(200);
                            finish();
                        }
                    }

                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void createAlbumInLocalAlbumDatabase(boolean isPublic, boolean otherMaintianer, String title, String desc, String digest) {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        StringBuilder builder = new StringBuilder();

        Share share = new Share();
        share.setUuid(Util.createLocalUUid());
        share.setDigest(digest);
        share.setTitle(title);
        share.setDesc(desc);

        Log.i(TAG, "create album digest:" + digest);

        if (isPublic) {
            for (String user : LocalCache.UsersMap.keySet()) {
                builder.append(user);
                builder.append(",");
            }
        }
        String viewer = builder.toString();
        Log.i(TAG, "create album viewer:" + viewer);
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
        share.setAlbum(true);
        dbUtils.insertLocalShare(share);

    }

}
