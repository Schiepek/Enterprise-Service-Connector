package models;

import com.google.gdata.data.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by Richard on 20.10.2014.
 */
public class EscDateTimeParser {

    public static String parseSfDateToString(String datestring) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("DE"));
        DateTime date = new DateTime(df.parse(datestring.substring(0, 10) + " " + datestring.substring(11, 19)));
        date.setTzShift(120);
        datestring = date.toString();
        return datestring.substring(0, 10) + " " + datestring.substring(11, 19);
    }
}
