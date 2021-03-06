package com.iwelogic.minecraft.mods.ui.onboarding

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.iwelogic.minecraft.mods.ui.base.BaseViewModel
import com.iwelogic.minecraft.mods.ui.base.Const
import com.iwelogic.minecraft.mods.ui.base.Const.VALUE
import com.iwelogic.minecraft.mods.ui.base.SingleLiveEvent
import com.iwelogic.minecraft.mods.utils.ignoreFirst
import com.iwelogic.minecraft.mods.utils.writeString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(@ApplicationContext applicationContext: Context) : BaseViewModel(applicationContext) {

    var age: MutableLiveData<Int> = MutableLiveData(45)
    var openMain: SingleLiveEvent<Boolean> = SingleLiveEvent()
    var openUrl: SingleLiveEvent<Boolean> = SingleLiveEvent()
    var selected: MutableLiveData<Boolean> = MutableLiveData(false)
    private val ageObserver: (Int) -> Unit = {
        selected.postValue(it > 0)
    }

    init {
        val params = Bundle()
        FirebaseAnalytics.getInstance(applicationContext).logEvent("open_onboarding", params)
    }

    fun subscribeOnAgeChanges() {
        age.ignoreFirst().observeForever(ageObserver)
    }

    fun onClickOk() {
        val contentRating = when (age.value) {
            in 0..6 -> "G"
            in 7..11 -> "PG"
            in 12..17 -> "T"
            else -> "MA"
        }
        val params = Bundle()
        params.putString(VALUE, contentRating)
        context.get()?.let { FirebaseAnalytics.getInstance(it).logEvent(Const.CONTENT_RATING, params) }
        context.get()?.writeString(Const.CONTENT_RATING, contentRating)
        context.get()?.writeString(Const.AGE, age.value.toString())
        val requestConfiguration = MobileAds.getRequestConfiguration().toBuilder().setMaxAdContentRating(contentRating).setTestDeviceIds(listOf("5571260002C1C3A1FD32D49B3E5332C1", "81AC2F5CC6A169492DFD647D9F39B4AA")).build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        openMain.invoke(true)
    }

    fun onClickPrivacyPolicy() {
        openUrl.invoke(true)
    }

    override fun onCleared() {
        super.onCleared()
        age.removeObserver(ageObserver)
    }
}
