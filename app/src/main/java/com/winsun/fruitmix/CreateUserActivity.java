package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateUserActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "CreateUserActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.user_name_layout)
    TextInputLayout userNameInputLayout;
    @BindView(R.id.user_name_edittext)
    TextInputEditText userNameEditText;
    @BindView(R.id.user_password_layout)
    TextInputLayout userPasswordInputLayout;
    @BindView(R.id.user_password_edittext)
    TextInputEditText userPasswordEditText;
    @BindView(R.id.confirm_password_layout)
    TextInputLayout confirmPasswordLayout;
    @BindView(R.id.confirm_password_edittext)
    TextInputEditText confirmPasswordEditText;
    @BindView(R.id.create_user_button)
    Button createUserBtn;

    ProgressDialog mDialog;

    private List<String> remoteUserNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        ButterKnife.bind(this);

        int size = LocalCache.RemoteUserMapKeyIsUUID.size();
        remoteUserNames = new ArrayList<>(size);

        final Collection<User> users = LocalCache.RemoteUserMapKeyIsUUID.values();
        for (User user : users) {
            remoteUserNames.add(user.getUserName());
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        createUserBtn.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        switch (action) {
            case Util.REMOTE_USER_CREATED: {

                if (mDialog != null)
                    mDialog.dismiss();

                OperationResult operationResult = operationEvent.getOperationResult();

                if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {
                    handleCreateUser();
                } else {
                    Toast.makeText(this, operationResult.getResultMessage(this), Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }

    }

    private void handleCreateUser() {
        setResult(RESULT_OK);

        finish();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.create_user_button:

                Util.hideSoftInput(this);

                if (!Util.getNetworkState(this)) {
                    Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }

                String userName = userNameEditText.getText().toString();

                if (remoteUserNames.contains(userName)) {

                    userNameInputLayout.setErrorEnabled(true);
                    userNameInputLayout.setError(getString(R.string.username_not_unique));

                    return;

                } else if (userName.isEmpty()) {

                    userNameInputLayout.setErrorEnabled(true);
                    userNameInputLayout.setError(getString(R.string.empty_username));

                    return;

                } else {
                    userNameInputLayout.setErrorEnabled(false);
                }

                String password = userPasswordEditText.getText().toString();

                String confirmPassword = confirmPasswordEditText.getText().toString();

                if (!password.equals(confirmPassword)) {

                    confirmPasswordLayout.setErrorEnabled(true);
                    confirmPasswordLayout.setError(getString(R.string.not_same_password));

                    return;

                } else {

                    confirmPasswordLayout.setErrorEnabled(false);

                }

                mDialog = ProgressDialog.show(CreateUserActivity.this, null, String.format(getString(R.string.operating_title),getString(R.string.create_user)), true, false);

                FNAS.createRemoteUser(userName, password);

                break;
        }

    }

}
