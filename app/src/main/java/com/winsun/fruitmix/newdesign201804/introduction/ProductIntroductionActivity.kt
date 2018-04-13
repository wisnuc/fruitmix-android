package com.winsun.fruitmix.newdesign201804.introduction

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.login.LoginEntranceActivity
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource
import kotlinx.android.synthetic.main.activity_product_introduction.*
import kotlinx.android.synthetic.main.product_introduction_item.view.*

class ProductIntroductionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_introduction)

        val introductionViewPageAdapter = IntroductionViewPageAdapter()

        viewpager.adapter = introductionViewPageAdapter
        viewpager_indicator.setViewPager(viewpager)

        introductionViewPageAdapter.registerDataSetObserver(viewpager_indicator.dataSetObserver)

    }

    private fun setShowProductIntroduction() {
        val systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(this)

        systemSettingDataSource.setShowProductIntroduction(false)
    }

    private inner class IntroductionViewPageAdapter : PagerAdapter() {

        private val pageList: IntArray = intArrayOf(1, 2, 3)

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val view = LayoutInflater.from(container.context).inflate(R.layout.product_introduction_item, container, false)

//            view.introduction_img.setImageResource(pageList[position])

            if (position == pageList.size - 1)
                view.login_entrance.visibility = View.VISIBLE
            else
                view.login_entrance.visibility = View.INVISIBLE

            view.login_entrance.setOnClickListener {

                setShowProductIntroduction()

                val intent = Intent(it.context, LoginEntranceActivity::class.java)

                it.context.startActivity(intent)

                finish()

            }

            container.addView(view)

            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

            container.removeView(`object` as View?)

        }


        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return `object` == view
        }


        override fun getCount(): Int {
            return pageList.size
        }


    }


}
