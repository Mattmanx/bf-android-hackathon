package com.bluefletch.internal.feed.service;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by blakebyrnes on 6/5/14.
 */
public class BusProvider {
    static Bus mBus;
    static Object gate = new Object();

    public static Bus getInstance(){
        if (mBus == null) {
            synchronized (gate) {
                if (mBus == null) mBus = new Bus(ThreadEnforcer.ANY);
            }
        }
        return mBus;
    }
}
