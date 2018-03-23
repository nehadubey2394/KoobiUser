package com.mualab.org.user.constants;

/**
 * Created by dharmraj on 21/12/17.
 */

public class Constant {
    public static final String FILE_PROVIDER_AUTHORITY = "com.app.mualab.fileprovider";
    // key for run time permissions
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int REQUEST_CHECK_SETTINGS = 505;
    public static final int LOCATION_SETTINGS_REQUEST  = 1;
    public static final int REQUEST_CODE_PICK_CONTACTS = 100;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    public static final int MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY = 103;
    public static final int PERMISSION_REQUEST_CONTACT = 104;
    public static final int PERMISSION_READ_PHONE_STATE = 105;
    public static final int REQUEST_CODE_CHOOSE = 106;
    public static final int GALLERY_KITKAT_INTENT_CALLED = 91;
    public static final int REQUEST_VIDEO_CAPTURE = 89;
    public static final int GALLERY_INTENT_CALLED = 90;
    public static final int ACTIVITY_COMMENT = 107;
    public static final int OTP_VERIFICATION = 108;
    public static final int ADD_STORY = 465;

    public static final int POST_FEED_DATA = 1039;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1001;
    public static final int CAMERA_REQUEST = 234;
    public static final int REQUEST_CODE_PICK = 235;
    public static final int SELECT_FILE = 1;

    public static int CALENDAR_DAY = 0;
    public static int CALENDAR_DAY_PAST = 1;

    /* All key*/
    public static final String isLoginReminder = "isLoginReminder";
    public static final String USER_ID = "USER_ID";
    public static final String SOCIAL_ID = "socialId";
    public static final String EMAIL_ID = "emailId";
    public static final String USER_PASSWORD = "USER_PASSWORD";

    public static final String USER = "user";
    public static final String registrationType = "registrationType";
    public static final int BUSINESS = 0;
    public static final int INDEPENDENT = 1;
    public static final int TEXT_STATE = 0, IMAGE_STATE = 1, VIDEO_STATE = 2, FEED_STATE = 3;

    /*Geo coder */
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
}
