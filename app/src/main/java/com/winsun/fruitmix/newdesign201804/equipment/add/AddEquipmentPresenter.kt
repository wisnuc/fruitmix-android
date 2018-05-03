package com.winsun.fruitmix.newdesign201804.equipment.add

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.anim.AnimatorBuilder
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager
import com.winsun.fruitmix.interfaces.BaseView
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.equipment.add.data.*
import com.winsun.fruitmix.newdesign201804.equipment.list.data.EquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.equipment.model.BaseEquipmentItem
import com.winsun.fruitmix.newdesign201804.equipment.model.CloudConnectEquipItem
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.available_equipment_detail.view.*
import kotlinx.android.synthetic.main.equipment_detail_title.view.*
import kotlinx.android.synthetic.main.equipment_list_item.view.*
import kotlinx.android.synthetic.main.unbound_equipment_detail.view.*
import java.util.*

interface SearchEquipmentUIState {

    fun startSearchState()
    fun searchTimeoutState(showEquipmentViewPager: Boolean)
    fun searchSucceedState()

}

interface EquipmentUIState {

    fun useExistDiskData()
    fun selectDiskBeforeUseExistDiskData()
    fun reinitialization()
    fun addAvailableEquipment()

    fun refreshStationName(stationName: String)

}

interface AddEquipmentView : BaseView {

    fun enterReinitialization(equipmentName: String)

}

private const val SEARCH_TIMEOUT = 0x1001
private const val SEARCH_SUCCEED = 0x1002

private const val SEARCH_TIMEOUT_SECOND = 6 * 1000L

private const val TAG = "AddEquipmentPresenter"

class AddEquipmentPresenter(private val equipmentSearchManager: EquipmentSearchManager,
                            private val searchEquipmentUIState: SearchEquipmentUIState,
                            private val equipmentUIState: EquipmentUIState,
                            var addEquipmentView: AddEquipmentView,
                            private val newEquipmentInfoDataSource: NewEquipmentInfoDataSource,
                            private val equipmentItemDataSource: EquipmentItemDataSource) : SearchEquipmentUIState {

    private val equipmentViewPagerAdapter = EquipmentViewPagerAdapter(this)

    private val customHandler = CustomHandler(this)

    fun getViewPagerAdapter(): PagerAdapter = equipmentViewPagerAdapter

    private lateinit var currentEquipmentState: EquipmentState

    private val baseNewEquipmentStates: MutableList<EquipmentState> = mutableListOf()

    private val random = Random()

    private val mEquipmentSerialNumbers = mutableListOf<String>()
    private val mEquipmentIps = mutableListOf<String>()

    private val typeArray: IntArray = intArrayOf(0, 1, 1, 2)
    private var type = 0

    override fun startSearchState() {

        searchEquipmentUIState.startSearchState()

        equipmentSearchManager.startDiscovery {

            if (!checkEquipmentIsFounded(it))
                return@startDiscovery

            customHandler.removeMessages(SEARCH_TIMEOUT)
            customHandler.sendEmptyMessageDelayed(SEARCH_SUCCEED, 3 * 1000)

            convert(it, typeArray[type], object : BaseOperateCallbackImpl() {})
            type++

        }

        customHandler.sendEmptyMessageDelayed(SEARCH_TIMEOUT, SEARCH_TIMEOUT_SECOND)

    }

    private fun checkEquipmentIsFounded(equipment: Equipment): Boolean {

        if (equipment.serialNumber.isNotEmpty()) {

            if (mEquipmentSerialNumbers.contains(equipment.serialNumber)) {

                Log.d(TAG, "getEquipmentTypeInfo: serial number has founded: " + equipment.serialNumber)

                return false

            } else {

                mEquipmentSerialNumbers.add(equipment.serialNumber)

            }

        }

        if (mEquipmentIps.contains(equipment.hosts[0])) {

            Log.d(TAG, "getEquipmentTypeInfo: host has founded: " + equipment.hosts[0])

            return false

        } else
            mEquipmentIps.add(equipment.hosts[0])

        return true

    }

    private fun convert(equipment: Equipment, type: Int, baseOperateCallback: BaseOperateCallback) {

        when (type) {
            0 -> newEquipmentInfoDataSource.getAvailableEquipmentInfo(equipment, object : BaseLoadDataCallbackImpl<AvailableEquipmentInfo>() {
                override fun onSucceed(data: MutableList<AvailableEquipmentInfo>?, operationResult: OperationResult?) {
                    super.onSucceed(data, operationResult)

                    baseNewEquipmentStates.add(convert(data!![0]))

                    baseOperateCallback.onSucceed()
                }
            })

            1 -> newEquipmentInfoDataSource.getUnboundEquipmentInfo(equipment, object : BaseLoadDataCallbackImpl<UnBoundEquipmentInfo>() {
                override fun onSucceed(data: MutableList<UnBoundEquipmentInfo>?, operationResult: OperationResult?) {
                    super.onSucceed(data, operationResult)

                    baseNewEquipmentStates.add(convert(data!![0]))

                    baseOperateCallback.onSucceed()
                }
            })

            2 -> newEquipmentInfoDataSource.getReinitializationEquipmentInfo(equipment, object : BaseLoadDataCallbackImpl<ReinitializationEquipmentInfo>() {
                override fun onSucceed(data: MutableList<ReinitializationEquipmentInfo>?, operationResult: OperationResult?) {
                    super.onSucceed(data, operationResult)

                    baseNewEquipmentStates.add(convert(data!![0]))

                    baseOperateCallback.onSucceed()

                }
            })

        }

    }

    private fun convert(baseNewEquipmentInfo: BaseNewEquipmentInfo): EquipmentState {

        return when (baseNewEquipmentInfo) {
            is AvailableEquipmentInfo -> AvailableEquipmentState(equipmentUIState, baseNewEquipmentInfo)
            is UnBoundEquipmentInfo -> UnboundEquipmentState(equipmentUIState, baseNewEquipmentInfo)
            is ReinitializationEquipmentInfo -> ReinitializationEquipmentState(equipmentUIState, baseNewEquipmentInfo)
            else -> throw IllegalArgumentException("current equipment type error")
        }

    }

    override fun searchTimeoutState(showEquipmentViewPager: Boolean) {

        equipmentSearchManager.stopDiscovery()

        searchEquipmentUIState.searchTimeoutState(showEquipmentViewPager)

    }

    override fun searchSucceedState() {

        searchEquipmentUIState.searchSucceedState()

        equipmentViewPagerAdapter.setEquipmentStates(baseNewEquipmentStates)
        equipmentViewPagerAdapter.notifyDataSetChanged()


    }

    fun onPageSelect(position: Int) {

        currentEquipmentState = baseNewEquipmentStates[position]

        currentEquipmentState.refreshView()

    }

    fun operateBtnOnClick(context: Context, btn: View) {

        currentEquipmentState.operateBtnOnClick(context, this, btn)

    }

    fun getItemSize() = baseNewEquipmentStates.size

    fun addEquipmentItem(baseEquipmentItem: BaseEquipmentItem, baseOperateCallback: BaseOperateCallback) {

        equipmentItemDataSource.addEquipmentItems(baseEquipmentItem, baseOperateCallback)

    }

    fun onDestroy() {
        equipmentSearchManager.stopDiscovery()
    }

}

private class CustomHandler(val addEquipmentPresenter: AddEquipmentPresenter) : Handler() {

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        when (msg?.what) {
            SEARCH_SUCCEED -> addEquipmentPresenter.searchSucceedState()
            SEARCH_TIMEOUT -> addEquipmentPresenter.searchTimeoutState(addEquipmentPresenter.getItemSize() > 0)
        }

    }

}

private class EquipmentViewPagerAdapter(val addEquipmentPresenter: AddEquipmentPresenter) : PagerAdapter() {

    private val mEquipmentStates: MutableList<EquipmentState> = mutableListOf()

    private var findFirstEquipment = true

    fun setEquipmentStates(equipmentStates: List<EquipmentState>) {
        mEquipmentStates.clear()
        mEquipmentStates.addAll(equipmentStates)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val view = View.inflate(container.context, R.layout.equipment_list_item, null)

        if (findFirstEquipment) {

            showEquipmentAnimation(view)

            findFirstEquipment = false

        }

        view.setOnClickListener {
            mEquipmentStates[position].equipmentIconOnClick(container.context, addEquipmentPresenter)
        }

        container.addView(view)

        return view
    }

    fun showEquipmentAnimation(view: View) {

        AnimatorBuilder(view.context, R.animator.ring_scale, view.ringIconIv).startAnimator()

        view.equipmentIconIv.alpha = 0f

        AnimatorBuilder(view.context,R.animator.equipment_icon_alpha,view.equipmentIconIv).setStartDelay(1000)
                .startAnimator()

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return mEquipmentStates.size
    }

}

private abstract class EquipmentState(val equipmentUIState: EquipmentUIState) {

    abstract fun refreshView()

    abstract fun equipmentIconOnClick(context: Context, addEquipmentPresenter: AddEquipmentPresenter)

    abstract fun operateBtnOnClick(context: Context, addEquipmentPresenter: AddEquipmentPresenter, btn: View)
}

private class AvailableEquipmentState(equipmentUIState: EquipmentUIState,
                                      val availableEquipmentInfo: AvailableEquipmentInfo) : EquipmentState(equipmentUIState) {

    override fun refreshView() {

        equipmentUIState.refreshStationName(availableEquipmentInfo.equipmentName)

        equipmentUIState.addAvailableEquipment()

    }

    override fun equipmentIconOnClick(context: Context, addEquipmentPresenter: AddEquipmentPresenter) {

        showAvailableEquipmentDetail(context, availableEquipmentInfo)

    }

    override fun operateBtnOnClick(context: Context, addEquipmentPresenter: AddEquipmentPresenter, btn: View) {

        addEquipment(addEquipmentPresenter, context, btn, availableEquipmentInfo.equipmentName)

    }

}

private fun addEquipment(addEquipmentPresenter: AddEquipmentPresenter, context: Context, btn: View, name: String) {
    val baseEquipmentItem = CloudConnectEquipItem(name, Util.createLocalUUid())

    addEquipmentPresenter.addEquipmentView.showProgressDialog(context.getString(R.string.operating_title,
            context.getString(R.string.add_equipment)))

    addEquipmentPresenter.addEquipmentItem(baseEquipmentItem, object : BaseOperateCallback {
        override fun onSucceed() {

            addEquipmentPresenter.addEquipmentView.dismissDialog()

            SnackbarUtil.showSnackBar(btn, Snackbar.LENGTH_SHORT, messageStr = context.getString(R.string.success, context.getString(R.string.add_equipment)))

            addEquipmentPresenter.addEquipmentView.finishView()

        }

        override fun onFail(operationResult: OperationResult?) {

            addEquipmentPresenter.addEquipmentView.dismissDialog()

            SnackbarUtil.showSnackBar(btn, Snackbar.LENGTH_SHORT, messageStr = operationResult?.getResultMessage(context).toString())

        }
    })
}

private class UnboundEquipmentState(equipmentUIState: EquipmentUIState,
                                    val unBoundEquipmentInfo: UnBoundEquipmentInfo) : EquipmentState(equipmentUIState) {

    override fun refreshView() {

        equipmentUIState.refreshStationName(unBoundEquipmentInfo.equipmentName)

        if (unBoundEquipmentInfo.selectBoundEquipmentDiskInfo == null)
            equipmentUIState.selectDiskBeforeUseExistDiskData()
        else
            equipmentUIState.useExistDiskData()
    }

    override fun equipmentIconOnClick(context: Context, addEquipmentPresenter: AddEquipmentPresenter) {

        showUnboundEquipmentDetail(context, unBoundEquipmentInfo, addEquipmentPresenter, {
            refreshView()
        })

    }

    override fun operateBtnOnClick(context: Context, addEquipmentPresenter: AddEquipmentPresenter, btn: View) {

        if (unBoundEquipmentInfo.selectBoundEquipmentDiskInfo != null)
            addEquipment(addEquipmentPresenter, context, btn, unBoundEquipmentInfo.equipmentName)
        else {

            SnackbarUtil.showSnackBar(btn, Snackbar.LENGTH_SHORT, R.string.select_disk_hint)

        }
    }
}

private class ReinitializationEquipmentState(equipmentUIState: EquipmentUIState,
                                             val reinitializationEquipmentInfo: ReinitializationEquipmentInfo) : EquipmentState(equipmentUIState) {

    override fun refreshView() {
        equipmentUIState.refreshStationName(reinitializationEquipmentInfo.equipmentName)

        equipmentUIState.reinitialization()
    }

    override fun equipmentIconOnClick(context: Context, addEquipmentPresenter: AddEquipmentPresenter) {


    }

    override fun operateBtnOnClick(context: Context, addEquipmentPresenter: AddEquipmentPresenter, btn: View) {

        addEquipmentPresenter.addEquipmentView.enterReinitialization(reinitializationEquipmentInfo.equipmentName)

    }

}


private fun showAvailableEquipmentDetail(context: Context, availableEquipmentInfo: AvailableEquipmentInfo) {

    val view = View.inflate(context, R.layout.available_equipment_detail, null)

    view.equipment_ip.text = availableEquipmentInfo.equipmentIP
    view.equipment_name.text = availableEquipmentInfo.equipmentName

    val availableEquipmentDiskInfo = availableEquipmentInfo.availableEquipmentDiskInfo

    view.admin_name_tv.text = availableEquipmentDiskInfo.admin.userName
    view.available_capacity_tv.text = FileUtil.formatFileSize(availableEquipmentDiskInfo.availableDiskSize)
    view.total_capacity.text = FileUtil.formatFileSize(availableEquipmentDiskInfo.totalDiskSize)

    AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .create().show()

}

private fun showUnboundEquipmentDetail(context: Context, unBoundEquipmentInfo: UnBoundEquipmentInfo,
                                       addEquipmentPresenter: AddEquipmentPresenter, refreshView: () -> Unit) {

    val view = View.inflate(context, R.layout.unbound_equipment_detail, null)

    val alertDialog = AlertDialog.Builder(context)
            .setCancelable(true)
            .create()

    view.equipment_ip.text = unBoundEquipmentInfo.equipmentIP
    view.equipment_name.text = unBoundEquipmentInfo.equipmentName

    view.reinitializeBtn.setOnClickListener {
        addEquipmentPresenter.addEquipmentView.enterReinitialization(unBoundEquipmentInfo.equipmentName)
    }

    view.unboundEquipmentRecyclerView.layoutManager = LinearLayoutManager(context)
    view.unboundEquipmentRecyclerView.itemAnimator = DefaultItemAnimator()

    val unboundEquipmentRecyclerViewAdapter = UnboundEquipmentRecyclerViewAdapter()

    view.unboundEquipmentRecyclerView.adapter = unboundEquipmentRecyclerViewAdapter

    unboundEquipmentRecyclerViewAdapter.setItemList(unBoundEquipmentInfo.unboundEquipmentDiskInfos)
    unboundEquipmentRecyclerViewAdapter.notifyDataSetChanged()

    if (unBoundEquipmentInfo.selectBoundEquipmentDiskInfo == null) {

        view.cancel_btn.visibility = View.VISIBLE

        view.got_it_btn.setText(R.string.yes)
        view.got_it_btn.setOnClickListener {

            unBoundEquipmentInfo.selectBoundEquipmentDiskInfo =
                    unBoundEquipmentInfo.unboundEquipmentDiskInfos[unboundEquipmentRecyclerViewAdapter.getSelectUnboundEquipmentDiskInfoPosition()]

            alertDialog.dismiss()

            refreshView()

        }

        view.cancel_btn.setOnClickListener {
            alertDialog.dismiss()
        }

    } else {

        view.cancel_btn.visibility = View.INVISIBLE

        view.got_it_btn.setText(R.string.got_it)
        view.got_it_btn.setOnClickListener {
            alertDialog.dismiss()
        }

    }

    alertDialog.setView(view)

    alertDialog.show()

}

