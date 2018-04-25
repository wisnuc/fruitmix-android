package com.winsun.fruitmix.newdesign201804.equipment.reinitialization

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.newdesign201804.equipment.add.DiskMode
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentItem
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentType
import com.winsun.fruitmix.newdesign201804.equipment.list.data.EquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.data.ReinitializationEquipmentDiskInfo
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.page.FinishReinitializationPage
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.page.InitialPage
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.page.SelectDiskModePage
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.page.SetPasswordPage
import com.winsun.fruitmix.user.User

public class ReinitializationPresenter(val context: Context, val admin: User, val pager: ViewPager,
                                       private val finishActivity: () -> Unit, val equipmentName: String,
                                       private val equipmentItemDataSource: EquipmentItemDataSource) {

    private val reinitializationEquipmentDiskInfos: MutableList<ReinitializationEquipmentDiskInfo> = mutableListOf()

    private var mSelectReinitializationEquipmentDiskInfos: List<ReinitializationEquipmentDiskInfo> = mutableListOf()
    private var mSelectDiskMode: DiskMode = DiskMode.SINGLE

    private var setPassword = ""

    private lateinit var selectDiskModePage: SelectDiskModePage
    private lateinit var setPasswordPage: SetPasswordPage
    private lateinit var finishReinitializationPage: FinishReinitializationPage

    fun init() {

        reinitializationEquipmentDiskInfos.add(ReinitializationEquipmentDiskInfo(
                "WD", 1.5 * 1024 * 1024 * 1024 * 1024, 2.0 * 1024 * 1024 * 1024 * 1024
        ))

        reinitializationEquipmentDiskInfos.add(ReinitializationEquipmentDiskInfo(
                "WD", 1.2 * 1024 * 1024 * 1024 * 1024, 2.0 * 1024 * 1024 * 1024 * 1024
        ))

        selectDiskModePage = SelectDiskModePage(context, reinitializationEquipmentDiskInfos,
                { selectReinitializationEquipmentDiskInfos, diskMode ->
                    mSelectReinitializationEquipmentDiskInfos = selectReinitializationEquipmentDiskInfos
                    mSelectDiskMode = diskMode
                })

        setPasswordPage = SetPasswordPage(context, admin, {

            pager.currentItem = 0

        }, {

            setPassword = it

            equipmentItemDataSource.addEquipmentItems(EquipmentItem(EquipmentType.CLOUD_CONNECTED, equipmentName),
                    object : BaseOperateCallbackImpl() {})

            pager.currentItem = 2

        })

        finishReinitializationPage = FinishReinitializationPage(context, admin, mSelectReinitializationEquipmentDiskInfos,
                mSelectDiskMode, {

            finishActivity()

        })

        val initialPages = listOf(selectDiskModePage, setPasswordPage, finishReinitializationPage)

        val reinitialViewPagerAdapter = ReinitialViewPagerAdapter(initialPages)

        pager.adapter = reinitialViewPagerAdapter
        reinitialViewPagerAdapter.notifyDataSetChanged()

    }


    inner class ReinitialViewPagerAdapter(private val initialPages: List<InitialPage>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val initialPage = initialPages[position]

            initialPage.refreshView()

            val view = initialPage.getView()

            container.addView(view)

            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

            container.removeView(`object` as View)

        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return initialPages.size
        }

    }

}

