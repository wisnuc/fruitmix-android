package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.winsun.fruitmix.databinding.ActivityPersonInfoBinding;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.logout.InjectLogoutUseCase;
import com.winsun.fruitmix.person.info.InjectPersonInfoDataSource;
import com.winsun.fruitmix.person.info.PersonInfoPresenter;
import com.winsun.fruitmix.person.info.PersonInfoView;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class PersonInfoActivity extends BaseActivity implements PersonInfoView, View.OnClickListener {

    public static final String TAG = PersonInfoActivity.class.getSimpleName();

    private PersonInfoPresenter personInfoPresenter;

    private User currentUser;

    public static final int GO_TO_MODIFY_USERNAME = 0;

    private ActivityPersonInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_person_info);

        initToolBar(binding, binding.toolbarLayout, getString(R.string.modify_user_info));

        personInfoPresenter = new PersonInfoPresenter(InjectUser.provideRepository(this),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(this), this, InjectPersonInfoDataSource.provideInstance(this),
                InjectTokenRemoteDataSource.provideTokenDataSource(this), InjectLogoutUseCase.provideLogoutUseCase(this),
                InjectLoginUseCase.provideLoginUseCase(this), ThreadManagerImpl.getInstance(), FileTool.getInstance());

        currentUser = personInfoPresenter.getCurrentUser();

        binding.setUser(currentUser);

        binding.userAvatar.setUser(currentUser, InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this));

        binding.setPersonInfoPresenter(personInfoPresenter);

        binding.modifyUserNameLayout.setOnClickListener(this);
        binding.modifyPasswordLayout.setOnClickListener(this);

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        personInfoPresenter.onDestroy();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        Intent intent;

        switch (v.getId()) {
            case R.id.modify_user_name_layout:

                intent = new Intent(PersonInfoActivity.this, ModifyUserNameActivity.class);
                intent.putExtra(ModifyUserNameActivity.USER_UUID_KEY, currentUser.getUuid());
                intent.putExtra(ModifyUserNameActivity.USER_NAME_KEY, currentUser.getUserName());
                startActivityForResult(intent, GO_TO_MODIFY_USERNAME);

                break;
            case R.id.modify_password_layout:

                intent = new Intent(PersonInfoActivity.this, ModifyUserPasswordActivity.class);
                intent.putExtra(ModifyUserPasswordActivity.USER_UUID_KEY, currentUser.getUuid());
                startActivity(intent);

                break;
            default:
                Log.d(TAG, "onClick: should not enter this case");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GO_TO_MODIFY_USERNAME && resultCode == RESULT_OK) {

            handleModifyUserInfoSucceed();

        }

    }

    private void handleModifyUserInfoSucceed() {
        this.setResult(RESULT_OK);

        currentUser = personInfoPresenter.getCurrentUser();

        binding.setUser(currentUser);

        binding.userAvatar.setUser(currentUser, InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this));
    }

    @Override
    public void handleBindSucceed() {

        handleModifyUserInfoSucceed();

    }
}
