package com.winsun.fruitmix.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.contract.SettingContract;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/3/21.
 */

public class SettingPresenterImpl implements SettingContract.SettingPresenter {

    private SettingContract.SettingView mView;

    private DataRepository mRepository;

    private boolean mAutoUploadOrNot = false;

    private int mAlreadyUploadMediaCount = -1;
    private int mTotalLocalMediaCount = 0;

    private long mTotalCacheSize = 0;

    public SettingPresenterImpl(DataRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public void initView(Context context) {

        mAutoUploadOrNot = mRepository.getAutoUploadOrNot();

        mView.setAutoUploadPhotosSwitchChecked(mAutoUploadOrNot);

        calcAlreadyUploadMediaCountAndTotalCacheSize(context);

    }

    private void calcAlreadyUploadMediaCountAndTotalCacheSize(final Context context) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                mAlreadyUploadMediaCount = mRepository.getAlreadyUploadMediaCount();
                mTotalLocalMediaCount = mRepository.getTotalMediaCount();

                mTotalCacheSize = FileUtil.getTotalCacheSize(context);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mView.setAlreadyUploadMediaCountTextViewVisibility(View.VISIBLE);
                mView.setAlreadyUploadMediaCountText(String.format(mView.getString(R.string.already_upload_media_count_text), mAlreadyUploadMediaCount, mTotalLocalMediaCount));

                mView.setCacheSizeText(FileUtil.formatFileSize(mTotalCacheSize));

            }
        }.execute();

    }

    @Override
    public void clearCache(Context context) {

        FileUtil.clearAllCache(context);
        mView.setCacheSizeText(FileUtil.formatFileSize(FileUtil.getTotalCacheSize(context)));
    }

    @Override
    public void onDestroy() {
        boolean isChecked = mView.getAutoUploadPhotosSwitchChecked();

        if (mAutoUploadOrNot != isChecked) {

            mRepository.saveAutoUploadOrNot(isChecked);

            if (isChecked) {

                mRepository.saveCurrentUploadDeviceID();

                mRepository.startUploadMediaInThread();

            } else {

                mRepository.stopUpload();

            }
        }
    }

    @Override
    public void attachView(SettingContract.SettingView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {

    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
