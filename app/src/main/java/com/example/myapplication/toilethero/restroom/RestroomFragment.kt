package com.example.myapplication.toilethero.restroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R

/**
 * A simple [Fragment] subclass.
 * Use the [RestroomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RestroomFragment : Fragment() {
    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * 使用此工廠方法來創建此 Fragment 的新實例，並使用提供的參數。
         *
         * @param param1 參數 1。
         * @param param2 參數 2。
         * @return 新的 RestroomFragment 實例。
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RestroomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // init parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate Fragment
        return inflater.inflate(R.layout.fragment_restroom, container, false)
    }
}
