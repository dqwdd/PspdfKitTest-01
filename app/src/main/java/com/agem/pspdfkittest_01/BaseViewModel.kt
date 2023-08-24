package com.agem.pspdfkittest_01

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        throwable.printStackTrace()
    }

    open fun launchViewModelScope(doWork: suspend() -> Unit) =
        viewModelScope.launch(viewModelScope.coroutineContext +
                Dispatchers.IO +
                coroutineExceptionHandler){
            doWork()
        }

    open val isLoading = MutableLiveData(View.INVISIBLE)
    open fun showLoading() = isLoading.postValue(View.VISIBLE)
    open fun hideLoading() = isLoading.postValue(View.INVISIBLE)
    //abstract fun <T> Array(size: Int): Array<T>
}