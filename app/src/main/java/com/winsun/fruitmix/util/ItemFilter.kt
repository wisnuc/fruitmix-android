package com.winsun.fruitmix.util

/**
 * Created by Administrator on 2018/3/9.
 */

public interface FilterRule<in T> {

    fun isFiltered(item: T): Boolean

}

fun <T> filterItem(itemList: List<T>, filterRule: FilterRule<T>): List<T> {

    return itemList.filter { filterRule.isFiltered(it) }

}