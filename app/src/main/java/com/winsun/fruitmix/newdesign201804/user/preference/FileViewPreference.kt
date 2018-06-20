package com.winsun.fruitmix.newdesign201804.user.preference

enum class SortDirection {
    POSITIVE, NEGATIVE
}

enum class SortMode {
    NAME, MODIFY_TIME, CREATE_TIME, SIZE
}

class FileSortPolicy {

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

    fun setCurrentSortDirection(sortDirection: SortDirection){
        currentSortDirection = sortDirection
    }

}

enum class FileViewMode{
    GRID,LIST
}

class FileViewModePolicy{

    private var currentFileViewMode = FileViewMode.GRID

    fun setCurrentFileViewMode(fileViewMode: FileViewMode){
        currentFileViewMode = fileViewMode
    }

    fun getCurrentFileViewMode(): FileViewMode {
        return currentFileViewMode
    }

}





