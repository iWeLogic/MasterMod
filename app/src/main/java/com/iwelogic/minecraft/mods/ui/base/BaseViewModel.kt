package com.iwelogic.minecraft.mods.ui.base

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.ref.WeakReference

open class BaseViewModel(applicationContext: Context) : ViewModel() {

    var context: WeakReference<Context> = WeakReference(applicationContext)
    var progress: MutableLiveData<Boolean> = MutableLiveData(false)
    var error: MutableLiveData<Boolean> = MutableLiveData(false)
    val close: SingleLiveEvent<Boolean> = SingleLiveEvent()

    fun onClickClose() {
        close.invoke(true)
    }

    fun onClickRetry() {
        reload()
    }

    open fun reload() {

    }

    fun getBase(): BaseViewModel = this
}