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
}
