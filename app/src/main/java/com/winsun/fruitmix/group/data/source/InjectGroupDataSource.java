package com.winsun.fruitmix.group.data.source;

import android.content.Context;

import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.base.data.InjectBaseDataOperator;
import com.winsun.fruitmix.base.data.retry.RefreshTokenRetryStrategy;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.data.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.token.data.TokenDataSource;
import com.winsun.fruitmix.token.manager.InjectSCloudTokenManager;
import com.winsun.fruitmix.token.manager.SCloudTokenManager;
import com.winsun.fruitmix.token.manager.TokenManager;
import com.winsun.fruitmix.token.param.SCloudTokenParam;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;

/**
 * Created by Administrator on 2017/8/4.
 */

public class InjectGroupDataSource {

    public static GroupRepository provideGroupRepository(Context context) {

        GroupRemoteDataSource groupRemoteDataSource = (GroupRemoteDataSource) GroupRemoteDataSource.getInstance(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(context));

        TokenManager tokenManager = InjectSCloudTokenManager.provideInstance(context);

        BaseDataOperator baseDataOperator = InjectBaseDataOperator.provideInstance(context,
                tokenManager, groupRemoteDataSource,
                new RefreshTokenRetryStrategy(tokenManager));

        GroupDataSourceConditionCheckWrapper groupDataSourceConditionCheckWrapper = new
                GroupDataSourceConditionCheckWrapper(groupRemoteDataSource,
                baseDataOperator);

        DBUtils dbUtils = DBUtils.getInstance(context);

        GroupLocalDataSource groupLocalDataSource = new GroupLocalDataSource(dbUtils);

        User currentUser = InjectUser.provideRepository(context).getUserByUUID(
                InjectSystemSettingDataSource.provideSystemSettingDataSource(context).getCurrentLoginUserUUID()
        );

        MediaDataSourceRepository mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(context);

        return GroupRepository.getInstance(groupDataSourceConditionCheckWrapper, groupLocalDataSource,
                new GroupTweetInDraftDataSource(dbUtils),
                ThreadManagerImpl.getInstance(), currentUser, mediaDataSourceRepository);

//        GroupDataSource fakeGroupDataSource = FakeGroupDataSource.getInstance();
//        return GroupRepository.getInstance(fakeGroupDataSource,ThreadManagerImpl.getInstance());

    }

}
