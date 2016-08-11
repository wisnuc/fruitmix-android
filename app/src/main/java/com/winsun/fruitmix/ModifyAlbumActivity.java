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

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/4/28.
 */
public class ModifyAlbumActivity extends AppCompatActivity {

    TextInputLayout mTitleLayout;
    TextInputEditText tfTitle, tfDesc;
    CheckBox ckPublic;
    CheckBox ckSetMaintainer;
    TextView btOK;
    ImageView ivBack;

    String selectedUIDStr;

    private String mUuid;
    private Map<String, String> mAblumMap;

    private Context mContext;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_album);

        mContext = this;

        mUuid = getIntent().getStringExtra(Util.MEDIASHARE_UUID);
        mAblumMap = LocalCache.DocumentsMap.get(mUuid);
        selectedUIDStr = mAblumMap.get("images");
        Log.d("winsun", selectedUIDStr);

        tfTitle = (TextInputEditText) findViewById(R.id.title_edit);
        mTitleLayout = (TextInputLayout) findViewById(R.id.title_textlayout);
        mTitleLayout.setHint(mAblumMap.get("title"));
        tfDesc = (TextInputEditText) findViewById(R.id.desc);
        tfDesc.setText(mAblumMap.get("desc"));
        ckPublic = (CheckBox) findViewById(R.id.sPublic);
        if (mAblumMap.get("private").equals("1")) {
            ckPublic.setChecked(false);
        } else {
            ckPublic.setChecked(true);
        }
        ckSetMaintainer = (CheckBox) findViewById(R.id.set_maintainer);
        if (mAblumMap.get("maintained").equals("false")) {
            ckSetMaintainer.setChecked(false);
        } else {
            ckSetMaintainer.setChecked(true);
        }
        btOK = (TextView) findViewById(R.id.ok);
        ivBack = (ImageView) findViewById(R.id.back);

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                title = tfTitle.getText().toString();
                desc = tfDesc.getText().toString();


                new AsyncTask<Object, Object, Boolean>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        mDialog = ProgressDialog.show(mContext,getString(R.string.loading_title),getString(R.string.loading_message),true,false);
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

                        if (sSetMaintainer) {
                            maintainers = viewers;
                        } else {
                            maintainers = ",";
                        }

                        if (Util.getNetworkState(mContext)) {
                            //modify by liang.wu
//                            data = "{\"album\":true, \"archived\":false,\"maintainers\":\"[\\\"" + FNAS.userUUID + "\\\"]\",\"viewers\":\"[" + viewers.substring(1) + "]\",\"tags\":[{\"albumname\":\"" + title + "\",\"desc\":\"" + desc + "\"}],\"contents\":\"[" + data.substring(1) + "]\"}";

                            data = "{\"commands\": \"[{\\\"op\\\":\\\"replace\\\", \\\"path\\\":\\\"" + mUuid + "\\\", \\\"value\\\":{\\\"archived\\\":\\\"false\\\",\\\"album\\\":\\\"true\\\", \\\"maintainers\\\":[\\\"" + FNAS.userUUID + "\\\"], \\\"tags\\\":[{\\\"albumname\\\":\\\"" + title + "\\\", \\\"desc\\\":\\\"" + desc + "\\\"}], \\\"viewers\\\":[" + viewers.substring(1) + "], \\\"maintainers\\\":[" + maintainers.substring(1) + "]}}]\"}";

                            Log.d("winsun", data);
                            try {
                                FNAS.PatchRemoteCall("/mediashare", data);
                                FNAS.LoadDocuments();
                                return true;
                            } catch (Exception e) {
                                return false;
                            }
                        } else {

                            DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                            Share share = dbUtils.getLocalShareByUuid(mUuid);

                            share.setTitle(title);
                            share.setDesc(desc);

                            StringBuilder builder = new StringBuilder();
                            if(sPublic){
                                for (String user:LocalCache.UsersMap.keySet()){
                                    builder.append(user);
                                    builder.append(",");
                                }
                            }
                            String viewer = builder.toString();
                            Log.i("create album viewer:",viewer);
                            share.setViewer(viewer);

                            String maintainer;
                            if(sSetMaintainer){
                                maintainer = viewer;
                            }else {
                                builder.setLength(0);
                                builder.append(FNAS.userUUID);
                                builder.append(",");

                                maintainer = builder.toString();
                            }
                            share.setMaintainer(maintainer);

                            dbUtils.updateLocalShare(share,share.getUuid());

                            FNAS.loadLocalShare();

                            return true;
                        }


                    }

                    @Override
                    protected void onPostExecute(Boolean sSuccess) {

                        mDialog.dismiss();

                        if (sSuccess) {
                            getIntent().putExtra(Util.UPDATED_ALBUM_TITLE,title);
                            setResult(RESULT_OK,getIntent());
                            finish();
                        } else {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }

                }.execute();
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

}
