/*
 *  Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://mindorks.com/license/apache-v2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package com.mualab.org.user.application;

/**
 * Created by amitshekhar on 07/07/17.
 */

public final class AppConstants {

    public static final int API_STATUS_CODE_LOCAL_ERROR = 0;

    public static final String DB_NAME = "koobi_social.db";

    public static final long NULL_INDEX = -1L;

    public static final String PREF_NAME = "koobi_pref";

    public static final String STATUS_CODE_FAILED = "failed";

    public static final String STATUS_CODE_SUCCESS = "success";

    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    private AppConstants() {
        // This utility class is not publicly instantiable
    }
}
