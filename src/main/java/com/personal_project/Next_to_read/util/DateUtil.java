package com.personal_project.Next_to_read.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    // convert timestamp to YYYY-MM-DD
    public static String formatDate(Timestamp timestamp) {
        // prevent error cause by null value to be converted
        if (timestamp == null) {
            return null;
        }

        ZonedDateTime zonedDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault());

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        return sdf.format(timestamp);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return zonedDateTime.format(formatter);
    }
}
