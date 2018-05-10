package com.winsun.fruitmix.newdesign201804.mainpage

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.winsun.fruitmix.BR
import com.winsun.fruitmix.R
import com.winsun.fruitmix.mainpage.MainPagePresenterImpl
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.EQUIPMENT_IP_KEY
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.EQUIPMENT_NAME_KEY
import com.winsun.fruitmix.newdesign201804.file.list.FilePage
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.BindingViewHolder
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import kotlinx.android.synthetic.main.activity_main_page.*
import kotlinx.android.synthetic.main.main_page_layout.*
import kotlinx.android.synthetic.main.navigation_menu_item.view.*

class MainPageActivity : AppCompatActivity(), DrawerView {

    private lateinit var filePage: FilePage

    private lateinit var mainPageAdapter: MainPageAdapter
    private val mainPageDrawerItemAdapter = MainPageDrawerItemAdapter(this)

    private val mainPages = mutableListOf<MainPage>()

    private var currentPagePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val equipmentName = intent.getStringExtra(EQUIPMENT_NAME_KEY)

        val equipmentIP = intent.getStringExtra(EQUIPMENT_IP_KEY)

        equipmentNameTv.text = equipmentName
        equipmentIPTv.text = equipmentIP

        filePage = FilePage(this)

        mainPages.add(filePage)

        mainPageAdapter = MainPageAdapter(mainPages)

        mainPageViewPager.adapter = mainPageAdapter

        bottomNavigationView.setOnNavigationItemSelectedListener {

            return@setOnNavigationItemSelectedListener true

        }

        drawerRecyclerView.layoutManager = LinearLayoutManager(this)
        drawerRecyclerView.adapter = mainPageDrawerItemAdapter

        onPageSelect(currentPagePosition)

    }

    private fun onPageSelect(position: Int) {
        mainPages[position].refreshView()

        refreshDrawer(mainPages[position])
    }

    private fun refreshDrawer(mainPage: MainPage) {

        drawerTitle.text = mainPage.getDrawerTitle()

        mainPageDrawerItemAdapter.setItemList(mainPage.getDrawerItems())
        mainPageDrawerItemAdapter.notifyDataSetChanged()

    }

    override fun onBackPressed() {

        val mainPage = mainPages[currentPagePosition]

        if (mainPage.useDefaultBackPressFunction())
            super.onBackPressed()
        else
            mainPage.onBackPressed()

    }

    private class MainPageAdapter(val mainPages: List<MainPage>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val view = mainPages[position].view

            container.addView(view)

            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return `object` == view
        }

        override fun getCount(): Int {
            return mainPages.size
        }

    }


    private class MainPageDrawerItemAdapter(val drawerView: DrawerView) : BaseRecyclerViewAdapter<SimpleViewHolder, DrawerItem>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view = LayoutInflater.from(parent?.context).inflate(R.layout.navigation_menu_item, parent, false)

            view.menu_icon

            return SimpleViewHolder(view)

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val view = holder?.itemView

            val drawerItem = mItemList[position]

            view?.menu_icon?.setImageResource(drawerItem.menuResID)
            view?.menu_text?.text = drawerItem.menuStr
            view?.menu_layout?.setOnClickListener {
                drawerItem.onClick()
                drawerView.closeDrawer()
            }

        }
    }

    override fun closeDrawer() {
        drawerLayout.closeDrawer(Gravity.START)
    }

}




