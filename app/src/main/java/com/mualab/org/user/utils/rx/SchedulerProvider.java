package com.mualab.org.user.utils.rx;

import io.reactivex.Scheduler;

/**
 * Created by Dharmraj Acharya on 16/04/18.
 */

public interface SchedulerProvider {

    Scheduler computation();

    Scheduler io();

    Scheduler ui();
}
