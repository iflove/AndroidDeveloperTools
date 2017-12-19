package com.android.developer.tools.interfaces;

public interface Consumer<T> {
    /**
     * Consume the given value.
     *
     * @param t the value
     * @throws Exception on error
     */
    void accept(T t) throws Exception;
}
