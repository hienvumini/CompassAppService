package com.nextsol.digitalcompass.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FormatDate {
    public static String simpleformat1 = "EEE, d MMM yyyy HH:mm z";

    public static String formatDate(String Paten, Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(simpleformat1);
        return simpleDateFormat.format(calendar.getTime());


    }
}
