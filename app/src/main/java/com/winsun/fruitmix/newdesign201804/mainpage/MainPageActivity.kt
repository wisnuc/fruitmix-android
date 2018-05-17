package com.winsun.fruitmix.newdesign201804.mainpage

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.component.FileSelectModeTitle
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.EQUIPMENT_IP_KEY
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.EQUIPMENT_NAME_KEY
import com.winsun.fruitmix.newdesign201804.file.list.FilePage
import com.winsun.fruitmix.newdesign201804.file.list.FilePageActionListener
import com.winsun.fruitmix.newdesign201804.file.list.FilePageSelectActionListener
import com.winsun.fruitmix.newdesign201804.file.move.MoveFileActivity
import com.winsun.fruitmix.newdesign201804.media.MediaPage
import com.winsun.fruitmix.newdesign201804.search.SearchActivity
import com.winsun.fruitmix.newdesign201804.share.SharePage
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_main_page.*
import kotlinx.android.synthetic.main.main_page_layout.*
import kotlinx.android.synthetic.main.navigation_menu_item.view.*
import kotlinx.android.synthetic.main.search_file_card.*

private const val PAGE_FILE = 0
private const val PAGE_MEDIA = 1
private const val PAGE_SHARE = 2

class MainPageActivity : AppCompatActivity(), DrawerView, FilePageSelectActionListener, FilePageActionListener {

    private lateinit var filePage: FilePage
    private lateinit var mediaPage: MediaPage
    private lateinit var sharePage: SharePage

    private lateinit var mainPageAdapter: MainPageAdapter
    private val mainPageDrawerItemAdapter = MainPageDrawerItemAdapter(this)

    private val mainPages = mutableListOf<MainPage>()

    private var currentPagePosition = 0

    private lateinit var fileSelectModeTitle: FileSelectModeTitle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val equipmentName = intent.getStringExtra(EQUIPMENT_NAME_KEY)

        val equipmentIP = intent.getStringExtra(EQUIPMENT_IP_KEY)

        equipmentNameTv.text = equipmentName
        equipmentIPTv.text = equipmentIP

        filePage = FilePage(this)
        filePage.registerFilePageSelectActionListener(this)
        filePage.registerFilePageActionListener(this)

        mediaPage = MediaPage(this)
        sharePage = SharePage(this)

        mainPages.add(filePage)
        mainPages.add(mediaPage)
        mainPages.add(sharePage)

        mainPageAdapter = MainPageAdapter(mainPages)

        mainPageViewPager.adapter = mainPageAdapter

        mainPageViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {

                currentPagePosition = position
                onPageSelect(currentPagePosition)

            }
        })

        bottomNavigationView.setOnNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.file -> mainPageViewPager.currentItem = PAGE_FILE
                R.id.photo -> mainPageViewPager.currentItem = PAGE_MEDIA
                R.id.share -> mainPageViewPager.currentItem = PAGE_SHARE
            }

            return@setOnNavigationItemSelectedListener true

        }



        initToggleOrientationIv()

        fileSelectModeTitle = FileSelectModeTitle(file_select_mode_title,
                { filePage.quitSelectMode() },
                { enterMovePage() },
                {},
                {})

        drawerRecyclerView.layoutManager = LinearLayoutManager(this)
        drawerRecyclerView.adapter = mainPageDrawerItemAdapter

        mainPageViewPager.currentItem = PAGE_FILE
        onPageSelect(currentPagePosition)

    }

    private fun onPageSelect(position: Int) {
        mainPages[position].refreshView()

        refreshDrawer(mainPages[position])

        val size = bottomNavigationView.menu.size()

        for (item in 0 until size) {
            bottomNavigationView.menu.getItem(item).isChecked = false
        }

        bottomNavigationView.menu.getItem(position).isChecked = true

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

    override fun onDestroy() {
        super.onDestroy()

        filePage.unregisterFilePageSelectActionListener(this)
        filePage.unregisterFilePageActionListener(this)

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

    private fun openDrawer() {
        drawerLayout.openDrawer(Gravity.START)
    }

    private fun initToggleOrientationIv() {

        toggleOrientationIv.setOnClickListener {

            mainPages[currentPagePosition].toggleOrientation()

        }

        moreIv.setOnClickListener {

            mainPages[currentPagePosition].handleMoreIvClick()

        }

    }

    override fun notifySelectModeChange(isEnterSelectMode: Boolean) {
        toggleViewEffect(isEnterSelectMode)
    }

    override fun notifySelectCountChange(selectCount: Int) {

        fileSelectModeTitle.notifySelectCountChanged(selectCount)

    }

    override fun notifyFolderLevelChanged(isRootFolder: Boolean, folderName: String) {

        if (isRootFolder) {

            searchLeftIv.setImageResource(R.drawable.menu_black)
            searchLeftIv.setOnClickListener {
                openDrawer()
            }

            searchTextView.text = getString(R.string.search_file)
            searchTextView.setOnClickListener {
                Util.startActivity(this, SearchActivity::class.java)
            }

            searchIv.visibility = View.GONE

        } else {

            searchLeftIv.setImageResource(R.drawable.back_black)
            searchLeftIv.setOnClickListener {
                filePage.onBackPressed()
            }

            searchTextView.text = folderName
            searchTextView.setOnClickListener { }

            searchIv.visibility = View.VISIBLE
            searchIv.setOnClickListener {
                Util.startActivity(this, SearchActivity::class.java)
            }

        }


    }

    private fun toggleViewEffect(isSelectMode: Boolean) {

        if (isSelectMode) {
            file_select_mode_title.visibility = View.VISIBLE
            search_file_card.visibility = View.INVISIBLE

            bottomNavigationView.visibility = View.GONE
        } else {
            search_file_card?.visibility = View.VISIBLE
            file_select_mode_title.visibility = View.INVISIBLE

            bottomNavigationView.visibility = View.VISIBLE

        }

    }

    private fun enterMovePage() {

        val intent = Intent(this, MoveFileActivity::class.java)
        startActivity(intent)

    }


}




