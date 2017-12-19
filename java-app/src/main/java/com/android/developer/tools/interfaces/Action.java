package com.android.developer.tools.interfaces;

public interface Action {
    /**
     * Runs the action and optionally throws a checked exception.
     *
     * @throws Exception if the implementation wishes to throw a checked exception
     */
    void run() throws Exception;
}