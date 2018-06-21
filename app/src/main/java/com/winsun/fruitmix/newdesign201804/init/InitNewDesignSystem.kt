package com.winsun.fruitmix.newdesign201804.init

import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataRepository

class InitNewDesignSystem {

    companion object {

        fun init(){
            FileDataRepository.destroyInstance()
        }

    }

}