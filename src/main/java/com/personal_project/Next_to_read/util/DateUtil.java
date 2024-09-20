package com.personal_project.Next_to_read.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DateUtil {

    // format timestamp to YYYY-MM-DD
    public static String formatDate(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(timestamp);
    }
}
