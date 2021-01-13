package com.fzc.nine

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import com.fzc.nine.scratch.R
import com.fzc.nine.scratch.findViewById


class MainFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewById<Button>(R.id.btn_goto_scratch).setOnClickListener(this)
        findViewById<Button>(R.id.btn_spin_nine).setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_goto_scratch -> {
                Navigation.findNavController(v!!)
                    .navigate(R.id.action_mainFragment_to_scratchNineFragment)
            }
            R.id.btn_spin_nine -> {
                Navigation.findNavController(v!!)
                    .navigate(R.id.action_mainFragment_to_spinNineFragment)
            }
        }
    }
}