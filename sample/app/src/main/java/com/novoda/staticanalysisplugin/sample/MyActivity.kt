package com.novoda.staticanalysisplugin.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.novoda.buildpropertiesplugin.sample.R
import kotlinx.android.synthetic.main.activity_my.*

class MyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        val another: AnotherCoreClass

        button.setOnClickListener({
            LOOK_ANOTHER_CRAZY_LONG_LONG_LONG_METHOD_NAME_FOR_NO_APPARENT_REASON_WHY_IS_THIS_METHOD_NAMED_LIKE_THIS_WHY_IT_LITERALLY_MAKES_NO_SENSE_WHATSOEVER(1)
            startActivity(Intent(this, SomeOtherActivity::class.java))
        })
    }

    private fun LOOK_ANOTHER_CRAZY_LONG_LONG_LONG_METHOD_NAME_FOR_NO_APPARENT_REASON_WHY_IS_THIS_METHOD_NAMED_LIKE_THIS_WHY_IT_LITERALLY_MAKES_NO_SENSE_WHATSOEVER(duration: Int) = Toast.makeText(this, "some useless message", duration).show()

}
