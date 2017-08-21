package com.winsun.fruitmix.generate.media;

import android.content.Context;

import com.winsun.fruitmix.media.InjectMedia;

/**
 * Created by Administrator on 2017/8/18.
 */

public class InjectGenerateMediaThumbUseCase {

    public static GenerateMediaThumbUseCase provideGenerateMediaThumbUseCase(Context context) {

        return GenerateMediaThumbUseCase.getInstance(InjectMedia.provideMediaDataSourceRepository(context));

    }

}
