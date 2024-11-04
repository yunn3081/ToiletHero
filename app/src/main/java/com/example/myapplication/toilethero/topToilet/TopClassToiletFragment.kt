package com.example.myapplication.toilethero.topToilet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentTopClassToiletBinding // 更新绑定类导入

class TopClassToiletFragment : Fragment() {

    private var _binding: FragmentTopClassToiletBinding? = null // 更新绑定类名

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val topClassToiletViewModel =  // 更新 ViewModel 变量名称
            ViewModelProvider(this).get(TopClassToiletViewModel::class.java) // 确保 ViewModel 的类名已更新

        _binding = FragmentTopClassToiletBinding.inflate(inflater, container, false) // 更新绑定类实例化
        val root: View = binding.root

        val textView: TextView = binding.textTopClassToilet // 更新为新布局文件中的 ID
        topClassToiletViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
