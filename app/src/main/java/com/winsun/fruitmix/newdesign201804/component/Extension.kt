package com.winsun.fruitmix.newdesign201804.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun ViewGroup.inflateView(resource:Int): View {
    return LayoutInflater.from(context).inflate(resource,this,false)
}