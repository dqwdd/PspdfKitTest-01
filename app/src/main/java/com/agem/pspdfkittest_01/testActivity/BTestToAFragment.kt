package com.agem.pspdfkittest_01.testActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agem.pspdfkittest_01.databinding.FragmentBTestToABinding

class BTestToAFragment : Fragment() {

    private lateinit var binding: FragmentBTestToABinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBTestToABinding.inflate(layoutInflater)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//    }
}