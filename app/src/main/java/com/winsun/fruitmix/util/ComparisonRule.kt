package com.winsun.fruitmix.util

/**
 * Created by Administrator on 2018/3/9.
 */
interface ComparisonRule {

    /**
     * a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    fun <T> compare(param1: T, param2: T): Int

}