package com.winsun.fruitmix.newdesign201804.file.list.data

import com.winsun.fruitmix.file.data.model.AbstractLocalFile
import com.winsun.fruitmix.file.data.model.LocalFile
import com.winsun.fruitmix.file.data.model.LocalFolder

data class FileUploadParam (val driveUUID:String,val dirUUID:String,val abstractLocalFile: AbstractLocalFile)