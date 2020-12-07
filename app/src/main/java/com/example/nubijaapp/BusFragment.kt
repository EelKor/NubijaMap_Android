package com.example.nubijaapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class BusFragment: Fragment() {

    companion object {
        const val TAG : String = "로그"

        fun newInstance() : BusFragment {
            return BusFragment()
        }
    }

    //메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "BusFragment - onCreate() called")

    }

    //프레그먼트를 안고 있는 액티비티에 붙었을때
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    //뷰가 생성되었을때
    //프래그먼트와 레이아웃을 연결
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "BusFragment - onCreateView() called")

        val view = inflater.inflate(R.layout.fragment_bus, container, false)

        return view
    }
}