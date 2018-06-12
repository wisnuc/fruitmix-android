package com.winsun.fruitmix.newdesign201804.file.transmission.model

data class Transmission(val dirUUID:String,val userUUID:String,val uuid:String,val id:String,
                        val name:String,val rateDownload:Double,val percentDone:Double,
                        val eta:Double,val status:Int)