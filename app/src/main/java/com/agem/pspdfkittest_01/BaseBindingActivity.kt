package com.agem.pspdfkittest_01

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class BaseBindingActivity<T: ViewDataBinding>(@LayoutRes val layoutRes: Int) : AppCompatActivity() {

    lateinit var binding: T

    lateinit var mContext: Context
    lateinit var lifecycleOwner: LifecycleOwner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layoutRes)
        setContentView(binding.root)

        mContext = this
        lifecycleOwner = this
    }

    protected open fun setValues() {}
    protected open fun setupEvent() {}
}