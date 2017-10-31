package com.winsun.fruitmix.generate.media;

import android.content.Context;

import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/8/18.
 */

public class InjectGenerateMediaThumbUseCase {

    public static GenerateMediaThumbUseCase provideGenerateMediaThumbUseCase(Context context) {

        return new GenerateMediaThumbUseCase(InjectMedia.provideMediaDataSourceRepository(context), ThreadManagerImpl.getInstance());

    }

}
