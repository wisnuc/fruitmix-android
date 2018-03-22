package com.winsun.fruitmix.group.presenter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.szysky.customize.siv.ImageLoader;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.databinding.GroupListItemBinding;
import com.winsun.fruitmix.eventbus.MqttMessageEvent;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.SystemMessageTextComment;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.viewmodel.GroupListViewModel;
import com.winsun.fruitmix.group.view.GroupListPageView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.mqtt.MqttUseCase;
import com.winsun.fruitmix.parser.RemoteGroupParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.data.TokenDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.recyclerview.BindingViewHolder;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/21.
 */

public class GroupListPresenter implements ActiveView {

    public static final String TAG = GroupListPresenter.class.getSimpleName();

    private GroupRepository groupRepository;

    private User mCurrentUser;

    private TokenDataSource mTokenDataSource;

    private GroupListAdapter groupListAdapter;

    private LoadingViewModel loadingViewModel;
    private NoContentViewModel noContentViewModel;
    private GroupListViewModel groupListViewModel;

    private GroupListPageView groupListPageView;

    private UserDataRepository mUserDataRepository;

    private SystemSettingDataSource mSystemSettingDataSource;

    private MqttUseCase mMqttUseCase;

    private boolean alreadyCallGetGroupList = false;

    private boolean alreadyInitialMqttService = false;

    public GroupListPresenter(GroupListPageView groupListPageView, User currentUser,
                              TokenDataSource tokenDataSource, GroupRepository groupRepository,
                              LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel,
                              GroupListViewModel groupListViewModel, UserDataRepository userDataRepository,
                              SystemSettingDataSource systemSettingDataSource, MqttUseCase mqttUseCase) {
        this.groupRepository = groupRepository;
        mCurrentUser = currentUser;
        mTokenDataSource = tokenDataSource;
        this.loadingViewModel = loadingViewModel;
        this.noContentViewModel = noContentViewModel;
        this.groupListViewModel = groupListViewModel;

        this.groupListPageView = groupListPageView;

        mUserDataRepository = userDataRepository;

        mSystemSettingDataSource = systemSettingDataSource;

        mMqttUseCase = mqttUseCase;

        groupListAdapter = new GroupListAdapter();

        ImageLoader.getInstance(groupListPageView.getContext()).setPicUrlRegex("https?://.*?");

    }

    public GroupListAdapter getGroupListAdapter() {
        return groupListAdapter;
    }

    public void refreshGroups() {

        if (alreadyCallGetGroupList) {

            groupListPageView.finishSwipeRefreshAnimation();

            return;

        }

        if (mSystemSettingDataSource.getCurrentWAToken().isEmpty()) {

            groupListPageView.finishSwipeRefreshAnimation();

            loadingViewModel.showLoading.set(false);

            groupListViewModel.showNoWATokenExplainLayout.set(true);

            String currentUserGUID = mCurrentUser.getAssociatedWeChatGUID();

            if (currentUserGUID != null && currentUserGUID.length() > 0) {

                groupListViewModel.showGoToBindWeChatBtn.set(false);

                groupListViewModel.explainTextField.set(groupListPageView.getString(R.string.login_with_wechat_to_use_group));

            } else {

                groupListViewModel.showGoToBindWeChatBtn.set(true);

                groupListViewModel.explainTextField.set(groupListPageView.getString(R.string.bind_wechat_to_use_group));

            }

            groupListViewModel.showRecyclerView.set(false);
            groupListViewModel.showAddFriendsFAB.set(false);

            return;
        }

        alreadyCallGetGroupList = true;

        groupRepository.getGroupList(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<PrivateGroup>() {
            @Override
            public void onSucceed(final List<PrivateGroup> data, OperationResult operationResult) {

                groupListPageView.finishSwipeRefreshAnimation();

                loadingViewModel.showLoading.set(false);

                if (data.size() > 0) {

                    /*for (PrivateGroup group : data) {

                        List<User> users = group.getUsers();
                        List<User> usersWithInfo = new ArrayList<>(users.size());

                        for (User user : users) {

                            User userWithInfo = mUserDataRepository.getUserByGUID(user.getAssociatedWeChatGUID());

                            if (userWithInfo != null)
                                usersWithInfo.add(userWithInfo);

                        }

                        group.clearUsers();
                        group.addUsers(usersWithInfo);

                    }*/

                    noContentViewModel.showNoContent.set(false);

                    groupListViewModel.showRecyclerView.set(true);

                    groupListAdapter.setPrivateGroups(data);
                    groupListAdapter.notifyDataSetChanged();

                } else {

                    noContentViewModel.showNoContent.set(true);

                    groupListViewModel.showRecyclerView.set(false);

                }

                groupListViewModel.showNoWATokenExplainLayout.set(false);

                groupListViewModel.showAddFriendsFAB.set(false);

                initMqttService();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                groupListPageView.finishSwipeRefreshAnimation();

                loadingViewModel.showLoading.set(false);

                noContentViewModel.showNoContent.set(true);

                groupListViewModel.showNoWATokenExplainLayout.set(false);

                groupListViewModel.showRecyclerView.set(false);
                groupListViewModel.showAddFriendsFAB.set(false);

                initMqttService();

            }
        }, this));

    }

    public void resetAlreadyCallGetGroupList() {
        alreadyCallGetGroupList = false;
    }

    private void initMqttService() {

        if (!alreadyInitialMqttService) {

            alreadyInitialMqttService = true;

            mMqttUseCase.initMqttClient(groupListPageView.getContext(), mCurrentUser.getAssociatedWeChatGUID());
        }


    }

    public void onDestroyView() {
        groupListPageView = null;
    }

    public void handleMqttMessageEvent(MqttMessageEvent mqttMessageEvent) {

        String message = mqttMessageEvent.getMessage();

        try {

            Log.d(TAG, "handleMqttMessageEvent: refresh group in memory");

            List<PrivateGroup> newGroups = new RemoteGroupParser().parse(message);

            groupRepository.refreshGroupInMemory(newGroups);

            refreshGroupUsingMemoryCache();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void refreshGroupUsingMemoryCache() {
        groupListAdapter.setPrivateGroups(groupRepository.getAllGroupFromMemory());
        groupListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean isActive() {
        return groupListPageView != null;
    }

    public class GroupListAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<PrivateGroup> mPrivateGroups;

        GroupListAdapter() {
            mPrivateGroups = new ArrayList<>();
        }

        void setPrivateGroups(List<PrivateGroup> privateGroups) {

            mPrivateGroups.clear();

            mPrivateGroups.addAll(privateGroups);

            Collections.sort(mPrivateGroups, new Comparator<PrivateGroup>() {
                @Override
                public int compare(PrivateGroup o1, PrivateGroup o2) {

                    long o2Time = o2.getLastCommentTime();

                    if (o2Time == -1)
                        o2Time = o2.getModifyTime();

                    long o1Time = o1.getLastCommentTime();

                    if (o1Time == -1)
                        o1Time = o1.getModifyTime();

                    if (o2Time > o1Time)
                        return 1;
                    else if (o1Time > o2Time) {
                        return -1;
                    } else
                        return 0;

                }
            });

        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            GroupListItemBinding binding = GroupListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new BindingViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            final PrivateGroup privateGroup = mPrivateGroups.get(position);

            holder.getViewDataBinding().setVariable(BR.privateGroup, privateGroup);
            holder.getViewDataBinding().executePendingBindings();

            GroupListItemBinding binding = (GroupListItemBinding) holder.getViewDataBinding();

            Context context = binding.getRoot().getContext();

            long lastCommentIndex = privateGroup.getLastCommentIndex();

            long difference = lastCommentIndex - privateGroup.getLastReadCommentIndex();

            binding.lastCommentContent.setText(getLastCommentContent(privateGroup, context));

            if (difference > 0) {

                binding.newCommentCountTextview.setVisibility(View.VISIBLE);
                binding.newCommentCountTextview.setText(difference + "");

            } else if (lastCommentIndex == -1 && privateGroup.getLastReadCommentIndex() == -1) {

                binding.newCommentCountTextview.setVisibility(View.VISIBLE);
                binding.newCommentCountTextview.setText("1");

            } else {
                binding.newCommentCountTextview.setVisibility(View.INVISIBLE);
            }

            binding.groupListItemRootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (privateGroup.isStationOnline()) {

                        groupListPageView.gotoGroupContentActivity(privateGroup.getUUID());

                    } else {

                        groupListPageView.showToast(groupListPageView.getString(R.string.group_offline_hint));

                    }

                }
            });

            Util.fillGroupUserAvatar(privateGroup, binding.userIconView);

        }

        @Override
        public int getItemCount() {
            return mPrivateGroups.size();
        }

    }

    public String getLastCommentContent(PrivateGroup privateGroup, Context context) {

        UserComment userComment = privateGroup.getLastComment();

        if (userComment != null) {

            if (userComment instanceof MediaComment) {

                return userComment.getCreateUserName(context) + ":[" + groupListPageView.getString(R.string.photo) + "]";

            } else if (userComment instanceof FileComment) {

                return userComment.getCreateUserName(context) + ":[" + groupListPageView.getString(R.string.files) + "]";

            } else {

                if (userComment instanceof SystemMessageTextComment) {

                    SystemMessageTextComment systemMessageTextComment = (SystemMessageTextComment) userComment;

                    if (systemMessageTextComment.showMessage())
                        return systemMessageTextComment.getFormatMessage(context);
                    else
                        return "";

                }


                TextComment textComment = (TextComment) userComment;

                return textComment.getCreator().getUserName() + ":" + textComment.getText();

            }

        } else {
            return "";
        }

    }


}
