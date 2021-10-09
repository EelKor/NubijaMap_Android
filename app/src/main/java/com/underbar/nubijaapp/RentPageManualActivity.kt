package com.underbar.nubijaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.underbar.nubijaapp.databinding.ActivityMainBinding
import com.underbar.nubijaapp.databinding.ActivityRentPageBinding
import com.underbar.nubijaapp.databinding.ActivityRentPageManualBinding

class RentPageManualActivity : AppCompatActivity() {

    lateinit var binding: ActivityRentPageManualBinding
    
    // 프래그먼트 페이지 인덱스
    private var page: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 뷰 바인딩
        binding = ActivityRentPageManualBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        
        // 초기 프래그먼트 로딩
        setFragment()
        
        // 다음 버튼 누르면 프래그먼트 전환
        binding.btnNext.setOnClickListener {
            page += 1

            if (page > 5)   {
                val rentPage = Intent(this, RentPageActivity::class.java)
                startActivity(rentPage)
                finish()
            }
            else
                replaceFragment(page)

        }
        
        // 이전 프래그먼트로 전환
        binding.btnBefore.setOnClickListener {
            page -= 1

            if (page < 1)
                finish()
            else
                replaceFragment(page)
        }
    }

    private fun setFragment()   {
        val transaction = supportFragmentManager.beginTransaction()
            .add(R.id.frameLayout, ManualPage1Fragment())

        transaction.commit()
    }

    private fun replaceFragment(page: Int)  {
        when(page)  {

            1 -> {
                val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, ManualPage1Fragment())
                transaction.commit()
            }

            2 -> {
                val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, ManualPage2Fragment())
                transaction.commit()
            }

            3 -> {
                val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, ManualPage3Fragment())
                transaction.commit()
            }

            4 -> {
                val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, ManualPage4Fragment())
                transaction.commit()
            }

            5 -> {
                val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, ManualPage5Fragment())
                transaction.commit()
            }
        }


    }
}