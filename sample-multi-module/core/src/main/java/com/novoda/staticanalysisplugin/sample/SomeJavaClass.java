package com.novoda.staticanalysisplugin.sample;

public class SomeJavaClass {

    private void THIS_IS_A_VERY_VERY_VERY_LONG_NAME_FOR_A_METHOD_IT_IS_IN_FACT_VERY_LONG_INDEED_NO_NEED_TO_COUNT_THE_NUMBER_OF_CHARACTERS_YOU_CAN_CLEARLY_SEE_THIS_IS_WAY_LONGER_THAN_IT_SHOULD(int duration) {
        // no-op
    }

    public static class Internal {

        public void impossibleCast() {
            final Object doubleValue = Double.valueOf(1.0);
            final Long value = (Long) doubleValue;
            System.out.println("   - " + value);
        }

    }
}
