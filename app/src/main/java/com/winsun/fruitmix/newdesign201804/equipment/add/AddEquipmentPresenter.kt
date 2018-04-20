package com.winsun.fruitmix.newdesign201804.equipment.add

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.equipment.add.data.NewEquipmentInfoDataSource
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

private const val SEARCH_TIMEOUT = 0x1001
private const val SEARCH_SUCCEED = 0x1002

private const val SEARCH_TIMEOUT_SECOND = 6 * 1000L

class AddEquipmentPresenter(private val equipmentSearchManager: EquipmentSearchManager,
                            private val searchEquipmentUIState: SearchEquipmentUIState,
                            private val equipmentUIState: EquipmentUIState,
                            private val newEquipmentInfoDataSource: NewEquipmentInfoDataSource) : SearchEquipmentUIState {

    private val equipments: MutableList<Equipment> = mutableListOf()
    private val equipmentViewPagerAdapter = EquipmentViewPagerAdapter()

    private val customHandler = CustomHandler(this)

    fun getViewPagerAdapter(): PagerAdapter = equipmentViewPagerAdapter

    private lateinit var currentEquipmentState: EquipmentState

    private val baseNewEquipmentStates: MutableList<EquipmentState> = mutableListOf()

    private val random = Random()

    override fun startSearchState() {

        equipmentSearchManager.startDiscovery {

            customHandler.removeMessages(SEARCH_TIMEOUT)
            customHandler.sendEmptyMessage(SEARCH_SUCCEED)

            convert(it, object : BaseOperateCallbackImpl() {
                override fun onSucceed() {
                    super.onSucceed()

                    equipmentViewPagerAdapter.setEquipmentStates(baseNewEquipmentStates)
                    equipmentViewPagerAdapter.notifyDataSetChanged()

                }
            })

        }

        customHandler.sendEmptyMessageDelayed(SEARCH_TIMEOUT, SEARCH_TIMEOUT_SECOND)
    }

    private fun convert(baseNewEquipmentInfo: BaseNewEquipmentInfo): EquipmentState {

        return when (baseNewEquipmentInfo) {
            is AvailableEquipmentInfo -> AvailableEquipmentState(equipmentUIState, baseNewEquipmentInfo)
            is UnBoundEquipmentInfo -> UnboundEquipmentState(equipmentUIState, baseNewEquipmentInfo)
            is ReinitializationEquipmentInfo -> ReinitializationEquipmentState(equipmentUIState, baseNewEquipmentInfo)
            else -> throw IllegalArgumentException("current equipment type error")
        }

    }

    private fun convert(equipment: Equipment, baseOperateCallback: BaseOperateCallback) {

        when (random.nextInt(3)) {
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

    override fun searchTimeoutState(showEquipmentViewPager: Boolean) {

        equipmentSearchManager.stopDiscovery()

        searchEquipmentUIState.searchTimeoutState(showEquipmentViewPager)
    }

    override fun searchSucceedState() {

        searchEquipmentUIState.searchSucceedState()

    }

    fun onPageSelect(position: Int) {

        currentEquipmentState = baseNewEquipmentStates[position]

        currentEquipmentState.refreshView()

    }

    fun operateBtnOnClick(context: Context) {

        currentEquipmentState.operateBtnOnClick(context)

    }

    fun getItemSize() = baseNewEquipmentStates.size

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

private class EquipmentViewPagerAdapter : PagerAdapter() {

    private val mEquipmentStates: MutableList<EquipmentState> = mutableListOf()

    fun setEquipmentStates(equipmentStates: List<EquipmentState>) {
        mEquipmentStates.clear()
        mEquipmentStates.addAll(equipmentStates)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val view = View.inflate(container.context, R.layout.equipment_list_item, null)

        view.setOnClickListener {
            mEquipmentStates[position].equipmentIconOnClick(container.context)
        }

        container.addView(view)

        return view
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

    abstract fun equipmentIconOnClick(context: Context)

    abstract fun operateBtnOnClick(context: Context)
}

private class AvailableEquipmentState(equipmentUIState: EquipmentUIState,
                                      val availableEquipmentInfo: AvailableEquipmentInfo) : EquipmentState(equipmentUIState) {

    override fun refreshView() {

        equipmentUIState.refreshStationName(availableEquipmentInfo.name)

        equipmentUIState.addAvailableEquipment()
    }

    override fun equipmentIconOnClick(context: Context) {

        showAvailableEquipmentDetail(context)

    }

    override fun operateBtnOnClick(context: Context) {

    }

}

private class UnboundEquipmentState(equipmentUIState: EquipmentUIState,
                                    val unBoundEquipmentInfo: UnBoundEquipmentInfo) : EquipmentState(equipmentUIState) {
    override fun refreshView() {

        equipmentUIState.refreshStationName(unBoundEquipmentInfo.name)

        if (unBoundEquipmentInfo.unboundEquipmentDiskInfos.size > 1)
            equipmentUIState.useExistDiskData()
        else
            equipmentUIState.selectDiskBeforeUseExistDiskData()
    }

    override fun equipmentIconOnClick(context: Context) {

        showUnboundEquipmentDetail(context)

    }

    override fun operateBtnOnClick(context: Context) {

    }

}

private class ReinitializationEquipmentState(equipmentUIState: EquipmentUIState,
                                             val reinitializationEquipmentInfo: ReinitializationEquipmentInfo) : EquipmentState(equipmentUIState) {

    override fun refreshView() {
        equipmentUIState.refreshStationName(reinitializationEquipmentInfo.name)

        equipmentUIState.reinitialization()
    }

    override fun equipmentIconOnClick(context: Context) {


    }

    override fun operateBtnOnClick(context: Context) {


    }

}


private fun showAvailableEquipmentDetail(context: Context) {

    AlertDialog.Builder(context)
            .setView(R.layout.available_equipment_detail)
            .create().show()

}

private fun showUnboundEquipmentDetail(context: Context) {

    val view = View.inflate(context, R.layout.unbound_equipment_detail_item, null)


    AlertDialog.Builder(context)
            .setView(R.layout.unbound_equipment_detail)
            .create().show()
}


public class AddEquipmentByIpPresenter {


}
