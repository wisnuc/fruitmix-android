package com.winsun.fruitmix.http

import java.io.File

/**
 * Created by Administrator on 2017/12/18.
 */

data class FileFormData(val name: String, val fileName: String, val File: File)

data class TextFormData(val name: String, val value: String)