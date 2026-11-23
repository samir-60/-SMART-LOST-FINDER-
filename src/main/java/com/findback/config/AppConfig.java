package com.findback.config;

import java.io.File;

/**
 * Global Application Configuration and Constants.
 */
public class AppConfig {

    public static final String APP_NAME = "FindBack";
    public static final String APP_TITLE = "FindBack – Smart Lost & Found Portal";
    public static final String APP_SUBTITLE = "Smart Lost & Found Portal";
    public static final String APP_VERSION = "v1.0.0";
    public static final String UNIVERSITY_NAME = "Metropolitan University, Sylhet";
    public static final String CAMPUS_PROCTOR_OFFICE = "Proctor & Security Office, Ground Floor";

    public static final String UPLOAD_DIR = "uploads" + File.separator + "items" + File.separator;
    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    static {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
