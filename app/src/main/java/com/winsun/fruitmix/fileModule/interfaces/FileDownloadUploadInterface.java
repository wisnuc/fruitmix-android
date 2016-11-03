package com.winsun.fruitmix.fileModule.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2016/11/3.
 */

public interface FileDownloadUploadInterface {

    @GET
    @Streaming
    Call<ResponseBody> downloadFile(@Url String fileUrl);

}
