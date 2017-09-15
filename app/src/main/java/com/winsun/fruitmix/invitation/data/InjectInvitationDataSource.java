package com.winsun.fruitmix.invitation.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/8/28.
 */

public class InjectInvitationDataSource {

    public static InvitationDataSource provideInvitationDataSource(Context context) {

        return new InvitationDataRepository(ThreadManagerImpl.getInstance(), new InvitationRemoteDataSource(InjectHttp.provideIHttpUtil(context),
                InjectHttp.provideHttpRequestFactory(context)));

    }

}
