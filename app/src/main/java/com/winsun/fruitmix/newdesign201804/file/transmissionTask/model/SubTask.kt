package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

enum class SubTaskState{
    CONFLICT,WORKING
}

data class SubTaskError(val code:String)

data class SubTask(val parentUUID:String,val srcUUID:String,val srcName:String,val subTaskState: SubTaskState,
                   val subTaskError: SubTaskError)

enum class ConflictSubTaskPolicy(val value:String?){
    SKIP("skip"),MERGE("keep"),REPLACE("replace"),RENAME("rename"),NULL(null)
}