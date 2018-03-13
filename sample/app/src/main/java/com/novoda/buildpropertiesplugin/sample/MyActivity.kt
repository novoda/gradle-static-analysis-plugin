package com.novoda.buildpropertiesplugin.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_my.*

class MyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        button.setOnClickListener({ startActivity(Intent(this, SomeOtherActivity::class.java)) })
    }
}
