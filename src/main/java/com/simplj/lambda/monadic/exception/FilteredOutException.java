package com.simplj.lambda.monadic.exception;

public class FilteredOutException extends Exception {
    public FilteredOutException(Object val) {
        super(trim(String.valueOf(val)) + " failed to pass filter condition!");
    }

    private static String trim(String val) {
        return val.length() > 30 ? val.substring(0, 27) + "..." : val;
    }
}
