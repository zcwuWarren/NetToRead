package com.personal_project.Next_to_read.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DateUtil {

    // convert timestamp to YYYY-MM-DD
    public static String formatDate(Timestamp timestamp) {
        // prevent error cause by null value to be converted
        if (timestamp == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(timestamp);
    }
}
