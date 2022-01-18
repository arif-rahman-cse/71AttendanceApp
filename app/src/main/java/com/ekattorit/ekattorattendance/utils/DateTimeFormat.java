package com.ekattorit.ekattorattendance.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  SimpleDateFormat sdf= new SimpleDateFormat("dd MM yyyy");
 *  MM   - will display number of the Month.
 *  MMM  - will display Month Three character only(Ex: Jul)
 *  MMMM - will display full month(Ex: July)
 *
 *  yyyy - will display full year(2016)
 *  yy   - will display last two digits(16)
 *
 *  hh - will display hours
 *  mm -will display minutes
 *  ss - will display seconds
 *  a - will display AM or PM
 *
 *  Ex: if time is 12:09:10 PM means (hh:mm:ss a)
 *
 *  EEE- will display short week name(Ex: Wed)
 *  EEEE- will display full week name(Ex: Wednesday)

 */

public class DateTimeFormat {

    public static String timeFormat(String mTime) throws ParseException {

        // *** note that it's "yyyy-MM-dd " not "yyyy-mm-dd hh:mm:ss"
        SimpleDateFormat passingFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = passingFormat.parse(mTime);

        // *** same for the format String below output like Thu, Oct 28
        SimpleDateFormat mFormat = new SimpleDateFormat("hh:mm a");

        return  mFormat.format(date);

    }
}
