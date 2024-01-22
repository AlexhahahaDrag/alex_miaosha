package com.alex.mission.manager;

/**
 *description:  限流
 *author:       majf
 *createDate:   2022/7/8 17:10
 *version:      1.0.0
 */
public interface AccessLimitService {

    boolean tryAcquireToken();
}
