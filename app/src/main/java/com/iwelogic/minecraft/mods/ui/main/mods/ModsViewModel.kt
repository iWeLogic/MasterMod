package com.iwelogic.minecraft.mods.ui.main.mods

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iwelogic.minecraft.mods.App
import com.iwelogic.minecraft.mods.data.MultiMap
import com.iwelogic.minecraft.mods.data.Repository
import com.iwelogic.minecraft.mods.data.Result
import com.iwelogic.minecraft.mods.models.Filter
import com.iwelogic.minecraft.mods.models.FilterValue
import com.iwelogic.minecraft.mods.models.Mod
import com.iwelogic.minecraft.mods.models.Sort
import com.iwelogic.minecraft.mods.ui.base.BaseViewModel
import com.iwelogic.minecraft.mods.ui.base.SingleLiveEvent
import com.iwelogic.minecraft.mods.models.Type
import com.iwelogic.minecraft.mods.utils.deepCopy
import com.iwelogic.minecraft.mods.utils.isTrue
import com.iwelogic.minecraft.mods.utils.readBoolean
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ModsViewModel @AssistedInject constructor(@ApplicationContext applicationContext: Context, private val repository: Repository, @Assisted val type: Type) : BaseViewModel(applicationContext) {

    companion object {
        fun provideFactory(assistedFactory: ModsViewModelFactory, type: Type): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return assistedFactory.create(type) as T
            }
        }

        const val PAGE_SIZE = 30
    }

    private var job: Job? = null
    private val changeObserver: (Any) -> Unit = {
        onReload()
    }
    val sort: MutableLiveData<Sort> = MutableLiveData(Sort.DATE)
    val mods: MutableLiveData<MutableList<Mod>> = MutableLiveData(ArrayList())
    val title: MutableLiveData<String> = MutableLiveData()
    val openMod: SingleLiveEvent<Mod> = SingleLiveEvent()
    val openSearch: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val openFavorite: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val openFilter: SingleLiveEvent<List<FilterValue>> = SingleLiveEvent()
    val spanCount: MutableLiveData<Int> = MutableLiveData(1)
    var finished = false
    private val filters: MutableLiveData<List<FilterValue>> = MutableLiveData(ArrayList())

    val onSelectSort: (Sort) -> Unit = {
        sort.postValue(it)
    }

    val onClick: (Mod) -> Unit = {
        showInterstitial.invoke {
            openMod.invoke(it)
        }
    }

    val onScroll: (Int) -> Unit = {
        if ((mods.value?.size ?: 0) < it + 5)
            load()
    }

    init {
        filters.value = Filter.getFiltersByCategory(type.id).map { FilterValue(it, true) }
        load()

        spanCount.postValue(type.spanCount * if (App.isTablet) 2 else 1)
        title.postValue(applicationContext.getString(type.title))
        sort.observeForever(changeObserver)
        filters.observeForever(changeObserver)
    }

    fun onClickFilter() {
        openFilter.invoke(filters.value.deepCopy() ?: ArrayList())
    }

    fun onClickSearch() {
        openSearch.invoke(true)
    }

    fun onClickFavorite() {
        openFavorite.invoke(true)
    }

    fun setNewFilters(newFilters: List<FilterValue>) {
        for (i in newFilters.indices) {
            if (newFilters[i].value != filters.value?.get(i)?.value) {
                filters.postValue(newFilters)
            }
        }
    }

    private fun load() {
        if (!job?.isActive.isTrue() && mods.value?.none { it.type == Type.PROGRESS }.isTrue() && !finished) {
            job = viewModelScope.launch {
                val queries: MultiMap<String, Any> = MultiMap()
                queries["property"] = "id"
                queries["property"] = "installs"
                queries["property"] = "likes"
                queries["property"] = "objectId"
                if (type != Type.SKINS) {
                    queries["property"] = "title"
                    queries["property"] = "description"
                    queries["property"] = "fileSize"
                    queries["property"] = "countImages"
                    queries["property"] = "version"
                }
                queries["pageSize"] = PAGE_SIZE
                queries["sortBy"] = sort.value?.query ?: ""
                queries["where"] = Filter.getQuery(filters.value)
                queries["offset"] = mods.value?.size ?: 0

                repository.getMods(type.id, queries).catch {
                    showProgressInList(false)
                    progress.postValue(false)
                    error.postValue(it.message)
                }.collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            error.postValue(null)
                            if (mods.value.isNullOrEmpty()) progress.postValue(true)
                            else showProgressInList(true)
                        }
                        is Result.Finish -> {
                            showProgressInList(false)
                            progress.postValue(false)
                        }
                        is Result.Success -> {
                            val data = result.data?.toMutableList()?.onEach { it.type = Type.values().firstOrNull { it == type } } ?: ArrayList()
                            if (data.isNotEmpty() && context.get()?.readBoolean("banner_in_list").isTrue()) data.add(4, Mod(type = Type.AD))
                            mods.value?.addAll(data)
                            mods.postValue(mods.value)
                            if (data.size < PAGE_SIZE) finished = true
                        }
                        is Result.Error -> error.postValue(result.message)
                    }
                }
            }
        }
    }

    private fun showProgressInList(status: Boolean) {
        if (status) mods.value?.add(Mod(type = Type.PROGRESS))
        else mods.value?.removeAll { it.type == Type.PROGRESS }
        mods.postValue(mods.value)
    }

    override fun onReload() {
        job?.cancel()
        showProgressInList(false)
        progress.postValue(false)
        mods.value?.clear()
        mods.postValue(mods.value)
        finished = false
        load()
    }

    override fun onCleared() {
        super.onCleared()
        sort.removeObserver(changeObserver)
        filters.removeObserver(changeObserver)
    }
}

@AssistedFactory
interface ModsViewModelFactory {
    fun create(type: Type): ModsViewModel
}