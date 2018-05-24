package com.winsun.fruitmix.newdesign201804.file.upload

import android.app.Activity
import android.content.Context
import android.content.Intent

class UploadFileUtil{

    companion object {


        fun showFileBrowser(activity:Activity, requestCode:Int){

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"

            intent.addCategory(Intent.CATEGORY_OPENABLE)

            try {
                activity.startActivityForResult(
                        Intent.createChooser(intent,"请选择上传的文件")
                        ,requestCode)

            }catch (e:Exception){

            }

        }


    }


}