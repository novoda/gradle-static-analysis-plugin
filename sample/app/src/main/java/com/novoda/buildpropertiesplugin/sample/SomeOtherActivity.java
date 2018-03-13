package com.novoda.buildpropertiesplugin.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class SomeOtherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_some_other);

        THIS_IS_A_VERY_VERY_VERY_LONG_NAME_FOR_A_METHOD_IT_IS_IN_FACT_VERY_LONG_INDEED_NO_NEED_TO_COUNT_THE_NUMBER_OF_CHARACTERS_YOU_CAN_CLEARLY_SEE_THIS_IS_WAY_LONGER_THAN_IT_SHOULD(0);

        String extra = getIntent().getStringExtra("nope");
        if (extra != null) {
            Log.d(SomeOtherActivity.class.getSimpleName(), extra);
        }
        int boom = extra.length();
    }

    private void THIS_IS_A_VERY_VERY_VERY_LONG_NAME_FOR_A_METHOD_IT_IS_IN_FACT_VERY_LONG_INDEED_NO_NEED_TO_COUNT_THE_NUMBER_OF_CHARACTERS_YOU_CAN_CLEARLY_SEE_THIS_IS_WAY_LONGER_THAN_IT_SHOULD(int duration) {
        Toast.makeText(this, "i have no idea what to write here", duration).show();
    }
}
