package com.winsun.fruitmix.list.data;

import android.content.Context;

import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.base.data.InjectBaseDataOperator;
import com.winsun.fruitmix.base.data.retry.RefreshTokenRetryStrategy;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.manager.InjectSCloudTokenManager;
import com.winsun.fruitmix.token.manager.TokenManager;

/**
 * Created by Administrator on 2018/3/5.
 */

public class InjectMediaInTweetDataRepository {

    public static MediaInTweetDataRepository provideInstance(Context context, MediaComment mediaComment) {

        GroupRequestParam groupRequestParam = new GroupRequestParam(mediaComment.getGroupUUID(), mediaComment.getStationID());

        MediaInTweetRemoteDataSource mediaInTweetRemoteDataSource = new MediaInTweetRemoteDataSource(InjectHttp.provideIHttpUtil(context),
                InjectHttp.provideHttpRequestFactory(context), groupRequestParam);

        TokenManager tokenManager = InjectSCloudTokenManager.provideInstance(context);

        BaseDataOperator baseDataOperator = InjectBaseDataOperator.provideInstance(context, tokenManager, mediaInTweetRemoteDataSource,
                new RefreshTokenRetryStrategy(tokenManager));

        MediaInTweetDataSource mediaInTweetDataSource = new MediaInTweetDataSourceWrapper(mediaInTweetRemoteDataSource, baseDataOperator);

        return new MediaInTweetDataRepository(ThreadManagerImpl.getInstance(),
                mediaComment, mediaInTweetDataSource);

    }

}
