import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public static final TimeZone UTCTimeZone = TimeZone.getTimeZone("UTC");

    public static Date getUtc(String utc) {
        SimpleDateFormat format = getUtcDateFormat();

        try {
            return format.parse(utc);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static SimpleDateFormat getUtcDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(UTCTimeZone);

        return format;
    }

    public static String getUtc(Date date) {
        SimpleDateFormat format = getUtcDateFormat();

        return format.format(date);
    }

//    private static String format(String input) {
//        int toMilis = input.lastIndexOf('.');
//
//        if (toMilis < 0) {
//            // no milliseconds passed
//            return input.substring(0, input.length() - 1) + ".000";
//        }
//
//        String formated = input.substring(0, toMilis);
//
//        for (int i = toMilis; i < toMilis + 4; ++i) {
//            if (i < input.length() - 1) {
//                formated += input.charAt(i);
//            } else {
//                formated += "0";
//            }
//        }
//
//        return formated;
//    }
}
