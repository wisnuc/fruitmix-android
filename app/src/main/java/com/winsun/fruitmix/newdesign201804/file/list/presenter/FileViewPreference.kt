package com.winsun.fruitmix.newdesign201804.file.list.presenter

enum class SortDirection {
    POSITIVE, NEGATIVE
}

enum class SortMode {
    NAME, MODIFY_TIME, CREATE_TIME, SIZE
}

object SortPolicy {

    private var currentSortDirection = SortDirection.POSITIVE
    private var currentSortMode = SortMode.NAME

    fun setCurrentSortMode(sortMode: SortMode) {

        if (currentSortMode == sortMode) {

            if (currentSortDirection == SortDirection.POSITIVE)
                currentSortDirection = SortDirection.NEGATIVE
            else
                currentSortDirection = SortDirection.POSITIVE

        } else {

            currentSortMode = sortMode
            currentSortDirection = SortDirection.POSITIVE

        }

    }

    fun getCurrentSortMode(): SortMode {
        return currentSortMode
    }

    fun getCurrentSortDirection(): SortDirection {
        return currentSortDirection
    }

}

enum class FileViewMode{
    GRID,LIST
}

object FileViewModePolicy{

    private var currentFileViewMode = FileViewMode.GRID

    fun setCurrentFileViewMode(fileViewMode: FileViewMode){
        currentFileViewMode = fileViewMode
    }

    fun getCurrentFileViewMode():FileViewMode{
        return currentFileViewMode
    }

}





