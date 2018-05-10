package com.winsun.fruitmix.newdesign201804.mainpage

class DrawerItem(val menuResID:Int,val menuStr:String,val clickListener:()->Unit){

    fun onClick(){
        clickListener()
    }

}