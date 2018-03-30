package com.winsun.fruitmix.group.presenter;

import android.content.DialogInterface;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateCallbackWrapper;
import com.winsun.fruitmix.databinding.GroupAddPingItemBinding;
import com.winsun.fruitmix.databinding.GroupPingItemBinding;
import com.winsun.fruitmix.eventbus.MqttMessageEvent;
import com.winsun.fruitmix.group.data.model.AudioComment;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.RetryFailUserCommentStrategy;
import com.winsun.fruitmix.group.data.model.SystemMessageTextComment;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.group.data.model.UserCommentViewFactory;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.group.data.viewmodel.GroupContentViewModel;
import com.winsun.fruitmix.group.usecase.PlayAudioUseCase;
import com.winsun.fruitmix.group.view.GroupContentView;
import com.winsun.fruitmix.group.view.customview.CustomArrowToggleButton;
import com.winsun.fruitmix.group.view.customview.UserCommentView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.RemoteGroupParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.recyclerview.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/21.
 */

public class GroupContentPresenter implements CustomArrowToggleButton.PingToggleListener, ActiveView,
        RetryFailUserCommentStrategy {

    public static final String TAG = GroupContentPresenter.class.getSimpleName();

    private PlayAudioUseCase playAudioUseCase;

    private ImageLoader imageLoader;

    private GroupRepository groupRepository;

    private GroupContentViewModel groupContentViewModel;

    private GroupContentAdapter groupContentAdapter;

    private PingViewPageAdapter pingViewPageAdapter;

    private User currentLoggedInUser;

    private String groupUUID;

    private String stationID;

//    private PrivateGroup currentPrivateGroup;

    private LoadingViewModel mLoadingViewModel;

    private ToolbarViewModel mToolbarViewModel;

    private GroupContentView groupContentView;

    private List<UserComment> userComments;

    private UserDataRepository mUserDataRepository;

    private PrivateGroup mPrivateGroup;

    private SystemSettingDataSource mSystemSettingDataSource;

    public static final int RESULT_GROUP_REFRESH = 0x1001;

    public GroupContentPresenter(GroupContentView groupContentView, String groupUUID,
                                 UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource,
                                 GroupRepository groupRepository, GroupContentViewModel groupContentViewModel,
                                 LoadingViewModel loadingViewModel, ToolbarViewModel toolbarViewModel,
                                 ImageLoader imageLoader, PlayAudioUseCase playAudioUseCase) {

        Log.d(TAG, "init GroupContentPresenter");

        mLoadingViewModel = loadingViewModel;
        mToolbarViewModel = toolbarViewModel;

        this.playAudioUseCase = playAudioUseCase;
        this.imageLoader = imageLoader;
        this.groupContentView = groupContentView;
        this.groupUUID = groupUUID;
        this.groupRepository = groupRepository;
        this.groupContentViewModel = groupContentViewModel;

        mUserDataRepository = userDataRepository;

        mSystemSettingDataSource = systemSettingDataSource;

        currentLoggedInUser = userDataRepository.getUserByUUID(systemSettingDataSource.getCurrentLoginUserUUID());

//        currentLoggedInUser = new User();
//        currentLoggedInUser.setUuid(FakeGroupDataSource.MYSELF_UUID);

        groupContentAdapter = new GroupContentAdapter();

        pingViewPageAdapter = new PingViewPageAdapter();

        userComments = new ArrayList<>();

    }

    public GroupContentAdapter getGroupContentAdapter() {
        return groupContentAdapter;
    }

    public PingViewPageAdapter getPingViewPageAdapter() {
        return pingViewPageAdapter;
    }

    public boolean checkCanSend() {

        String currentStationID = mSystemSettingDataSource.getCurrentLoginStationID();

        return currentStationID.equals(stationID);

    }

    public void refreshView() {

        refreshTitleFromMemory();

        refreshUserCommentData();
    }

    public void refreshTitleFromMemory() {

        mPrivateGroup = groupRepository.getGroupFromMemory(groupUUID);

        stationID = mPrivateGroup.getStationID();

        String groupName = mPrivateGroup.getName();

        if (groupName.isEmpty()) {
            groupName = groupContentView.getString(R.string.group_chat, mPrivateGroup.getUsers().size());
        }

        mToolbarViewModel.titleText.set(groupName);

    }

    private void refreshUserCommentData() {

        GroupRequestParam groupRequestParam = new GroupRequestParam(mPrivateGroup.getUUID(), mPrivateGroup.getStationID());

        groupRepository.getAllUserCommentByGroupUUID(groupRequestParam, new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<UserComment>() {
            @Override
            public void onSucceed(List<UserComment> data, OperationResult operationResult) {

                mPrivateGroup.resetUnreadCommentCount();
                groupRepository.updateGroupUnreadCommentCountInDB(mPrivateGroup.getUUID(), mPrivateGroup.getUnreadCommentCount());

                mLoadingViewModel.showLoading.set(false);

                userComments = data;

                refreshViewWithNewUserComment();

//                refreshPinView();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mLoadingViewModel.showLoading.set(false);

                groupContentView.showToast(operationResult.getResultMessage(groupContentView.getContext()));

            }
        }, this));
    }

    private void refreshViewWithNewUserComment() {

        if (userComments.size() == 0)
            return;

        List<UserComment> showComments = new ArrayList<>();

        for (UserComment userComment : userComments) {

            if (userComment instanceof SystemMessageTextComment) {

                if (((SystemMessageTextComment) userComment).showMessage())
                    showComments.add(userComment);

            } else
                showComments.add(userComment);

        }

        Collections.sort(showComments, new Comparator<UserComment>() {
            @Override
            public int compare(UserComment o1, UserComment o2) {

/*                long o1Param = o1.getCreateTime();

                long o2Param = o2.getCreateTime();*/

                long o1Param = o1.getStoreTime();

                long o2Param = o2.getStoreTime();

                if (o1Param == o2Param) {
                    o1Param = o1.getIndex();
                    o2Param = o2.getIndex();
                }

                if (o1Param > o2Param)
                    return 1;
                else if (o2Param > o1Param) {
                    return -1;
                } else
                    return 0;
            }
        });

        groupContentAdapter.setUserComments(showComments);
        groupContentAdapter.notifyDataSetChanged();

        smoothToChatListEnd(showComments);

    }

    public void handleMqttMessage(MqttMessageEvent mqttMessageEvent) {

        String message = mqttMessageEvent.getMessage();

        try {

            List<PrivateGroup> newGroups = new RemoteGroupParser().parse(message);

            long currentGroupIndex = mPrivateGroup.getLastCommentIndex();

            Log.d(TAG, "handleMqttMessage: currentGroupIndex:" + currentGroupIndex);

            groupRepository.refreshGroupInMemory(newGroups);

            for (PrivateGroup group : newGroups) {

                if (group.getUUID().equals(groupUUID)) {

                    Log.d(TAG, "handleMqttMessage: currentGroup refresh title from memory");

                    refreshTitleFromMemory();

                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void handleGetNewCommentFinishedMessage(String groupUUID) {

        Log.d(TAG, "handleGetNewCommentFinishedMessage: groupUUID: " + groupUUID);

        if (groupUUID.equals(mPrivateGroup.getUUID())) {

            Log.d(TAG, "handleGetNewCommentFinishedMessage,message is current group,so refreshUserComment ");

            refreshUserCommentData();

        }

    }


    public void refreshPin() {

        refreshUserCommentData();

        refreshPinView();

    }

    private void refreshPinView() {

        List<Pin> pins = new ArrayList<>();

        groupContentViewModel.showPing.set(false);

        List<PinView> pinViews = new ArrayList<>(pins.size() + 1);

        for (Pin pin : pins) {
            PinView pinView = new PinContentView(pin);
            pinViews.add(pinView);
        }
        pinViews.add(new AddPinView());

        pingViewPageAdapter.setPingViews(pinViews);
        pingViewPageAdapter.notifyDataSetChanged();

    }

    public void onDestroy() {

        Log.d(TAG, "onDestroy: ");

        groupContentView = null;

    }

    @Override
    public void onPingToggleArrowToDown() {

        groupContentViewModel.showPing.set(true);

    }

    @Override
    public void onPingToggleArrowToUp() {

        groupContentViewModel.showPing.set(false);

    }

    @Override
    public boolean isActive() {
        return groupContentView != null;
    }

    @Override
    public void handleRetryFailUserComment(final UserComment failUserCommentInDraft) {

        if (groupContentView == null || groupContentView.getContext() == null)
            return;

        new AlertDialog.Builder(groupContentView.getContext()).setTitle(groupContentView.getString(R.string.retry_or_not))
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        retryFailUserComment(failUserCommentInDraft);

                    }
                }).setNegativeButton(R.string.cancel, null).create().show();

    }

    private void retryFailUserComment(final UserComment failUserCommentInDraft) {
        GroupRequestParam groupRequestParam = new GroupRequestParam(failUserCommentInDraft.getGroupUUID(),
                failUserCommentInDraft.getStationID());

        groupRepository.retryFailUserComment(groupRequestParam, failUserCommentInDraft, new BaseOperateCallbackWrapper(
                new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        int position = userComments.indexOf(failUserCommentInDraft);

                        getGroupContentAdapter().notifyItemChanged(position);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        groupContentView.showToast(operationResult.getResultMessage(groupContentView.getContext()));

                    }
                }, this
        ));
    }

    private class GroupContentAdapter extends RecyclerView.Adapter<UserCommentViewHolder> {

        private List<UserComment> mUserComments;

        private UserCommentViewFactory factory;

        GroupContentAdapter() {
            mUserComments = new ArrayList<>();

            factory = UserCommentViewFactory.getInstance(imageLoader, playAudioUseCase,
                    GroupContentPresenter.this);
        }

        void setUserComments(List<UserComment> userComments) {

            mUserComments.clear();

            mUserComments.addAll(userComments);

        }

        @Override
        public UserCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            UserCommentView userCommentView = factory.createUserCommentView(viewType);

            ViewDataBinding viewDataBinding = userCommentView.getViewDataBinding(parent.getContext(), parent);

            return new UserCommentViewHolder(viewDataBinding.getRoot(), userCommentView);

        }

        @Override
        public void onBindViewHolder(UserCommentViewHolder holder, int position) {

            UserComment preUserComment;
            UserComment currentUserComment;

            if (position == 0)
                preUserComment = null;
            else
                preUserComment = mUserComments.get(position - 1);

            currentUserComment = mUserComments.get(position);

            UserCommentShowStrategy userCommentShowStrategy = new UserCommentShowStrategy(preUserComment, currentUserComment, currentLoggedInUser.getAssociatedWeChatGUID());

            holder.userCommentView.refreshCommentView(groupContentView.getContext(), groupContentView.getToolbar(), userCommentShowStrategy, currentUserComment);

        }

        @Override
        public int getItemCount() {
            return mUserComments.size();
        }

        @Override
        public int getItemViewType(int position) {

            return factory.getUserCommentViewType(mUserComments.get(position));

        }

    }


    private class UserCommentViewHolder extends RecyclerView.ViewHolder {

        private UserCommentView userCommentView;

        UserCommentViewHolder(View itemView, UserCommentView userCommentView) {
            super(itemView);
            this.userCommentView = userCommentView;
        }

    }


    public void sendTxt(String text) {

        TextComment textComment = new TextComment(Util.createLocalUUid(), currentLoggedInUser, System.currentTimeMillis(), groupUUID, stationID, text);

        insertUserComment(textComment);

    }

    public void sendAudio(String filePath, long audioRecordTime) {

        AudioComment audioComment = new AudioComment(Util.createLocalUUid(), currentLoggedInUser, System.currentTimeMillis(), groupUUID,
                stationID, filePath, audioRecordTime);

        insertUserComment(audioComment);

    }

    private void insertUserComment(final UserComment userComment) {

        GroupRequestParam groupRequestParam = new GroupRequestParam(mPrivateGroup.getUUID(), mPrivateGroup.getStationID());

        groupRepository.insertUserComment(groupRequestParam, userComment, new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                userComments.add(userComment);

                int lastPosition = userComments.size() - 1;

                groupContentAdapter.setUserComments(userComments);

                groupContentAdapter.notifyItemInserted(lastPosition);

                groupContentView.smoothToChatListPosition(lastPosition);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                groupContentView.showToast(operationResult.getResultMessage(groupContentView.getContext()));

            }
        });
    }


    public void smoothToChatListEnd(List<UserComment> userComments) {

        if (userComments.size() == 0)
            return;

        groupContentView.smoothToChatListPosition(userComments.size() - 1);
    }

    private class PingViewPageAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<PinView> mPinViews;

        PingViewPageAdapter() {
            mPinViews = new ArrayList<>();
        }

        void setPingViews(List<PinView> pinViews) {
            mPinViews.clear();
            mPinViews.addAll(pinViews);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewDataBinding binding;

            if (viewType == PinView.TYPE_PING) {
                binding = GroupPingItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            } else
                binding = GroupAddPingItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new BindingViewHolder(binding);
        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            mPinViews.get(position).bindView(holder.getViewDataBinding());

        }

        @Override
        public int getItemCount() {
            return mPinViews.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mPinViews.get(position).getViewType();
        }
    }

    public interface PinView {

        int TYPE_PING = 0;
        int TYPE_ADD_PING = 1;

        int getViewType();

        void bindView(ViewDataBinding viewDataBinding);

        void onClick();

    }

    private class PinContentView implements PinView {

        private Pin pin;

        PinContentView(Pin pin) {
            this.pin = pin;
        }

        @Override
        public int getViewType() {
            return TYPE_PING;
        }

        @Override
        public void bindView(ViewDataBinding viewDataBinding) {

            viewDataBinding.setVariable(BR.ping, pin);

            viewDataBinding.setVariable(BR.pingView, this);

            viewDataBinding.executePendingBindings();

        }

        @Override
        public void onClick() {

            groupContentView.showPinContent(groupUUID, pin.getUuid());

        }
    }

    private class AddPinView implements PinView {

        @Override
        public int getViewType() {
            return TYPE_ADD_PING;
        }

        @Override
        public void bindView(ViewDataBinding viewDataBinding) {

            viewDataBinding.setVariable(BR.pingView, this);

            viewDataBinding.executePendingBindings();

        }

        @Override
        public void onClick() {

            groupContentView.showCreatePing();
        }
    }


}
