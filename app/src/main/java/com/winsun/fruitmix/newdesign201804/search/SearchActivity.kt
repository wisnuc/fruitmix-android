package com.winsun.fruitmix.newdesign201804.search

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R

class SearchActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun generateContent(root: ViewGroup?): View {

        TODO()
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.search_file)
    }

}
