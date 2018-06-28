package com.winsun.fruitmix.http

import okhttp3.ResponseBody

data class HttpResponseBody(val code:Int,val responseBody: ResponseBody)