package com.winsun.fruitmix.newdesign201804.file.list

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.winsun.fruitmix.util.Util

class MainPageDividerItemDecoration(var spanCount: Int, var totalItemCount: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {

        super.getItemOffsets(outRect, view, parent, state)

        val position = parent?.getChildAdapterPosition(view)

        val context = view?.context

        if (position == totalItemCount - 1 || position == totalItemCount) {

            outRect?.right = Util.dip2px(context, 4F)

        } else {
            outRect?.right = Util.dip2px(context, 4F)
            outRect?.bottom = Util.dip2px(context, 4F)
        }

/*        if (position!! % spanCount == 0) {

            if (position == totalItemCount - 1 || position == totalItemCount) {

                outRect?.right = Util.dip2px(context, 4F)

            } else {
                outRect?.right = Util.dip2px(context, 4F)
                outRect?.bottom = Util.dip2px(context, 4F)
            }

        } else {

            if (position == totalItemCount - 1 || position == totalItemCount) {

            } else {
                outRect?.bottom = Util.dip2px(context, 4F)
            }

        }*/

    }

}