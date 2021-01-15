package com.fzc.nine.spin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fzc.nine.scratch.R
import com.fzc.nine.scratch.findViewById

class SpinNineFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_spin_nine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNineSpin()
    }

    private fun initNineSpin() {
//        val spinNineView = findViewById<NineGridSpinLayout>(R.id.nine_grid_layout)
//
//        val dataList = ArrayList<Int>(9)
//        (5..13).forEach {
//            dataList.add(it)
//        }
//        spinNineView.setData(dataList)
//        spinNineView.setCompleteListener {
//            tokensAnimView?.visibility = View.VISIBLE
//            tokensAnimView?.playAnimation()
//        }
//        spinNineView.setOnCenterClickListener {
//            spinNineView.startSpinWithTarget((10..13).random())
//        }

        val nineSpinView = findViewById<NineSpinView>(R.id.view_nie_spin)
        val dataList = ArrayList<ItemInfo>(9)
        (5..13).forEach {
            dataList.add(ItemInfo(it))
        }
        nineSpinView.setData(dataList)
        nineSpinView.setOnCenterClickListener {
            val targetIndex = (10..13).random()
            nineSpinView.startSpinWithTargetReward(targetIndex)
        }
        nineSpinView.setCompleteListener {
            Log.e("TAG", "spin complete:----- ", )
        }
    }
}