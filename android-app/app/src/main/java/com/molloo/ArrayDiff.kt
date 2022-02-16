package com.molloo

import androidx.recyclerview.widget.DiffUtil

class ArrayDiff<T>(private val old:Array<T>, private val newArr:Array<T>, private val isSame:(o:T, n:T) -> Result) : DiffUtil.Callback() {
    enum class Result {
        NOTHING,
        ITEMS,
        CONTENTS,
        ITEM_CONTENTS,
    }

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return newArr.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val result = isSame(old[oldItemPosition], newArr[newItemPosition])
        return result == Result.ITEMS || result == Result.ITEM_CONTENTS
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val result = isSame(old[oldItemPosition], newArr[newItemPosition])
        return result == Result.CONTENTS || result == Result.ITEM_CONTENTS
    }
}