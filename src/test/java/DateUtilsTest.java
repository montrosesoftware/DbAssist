import org.junit.Test;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DateUtilsTest {

    @Test
    public void convertUTCDateStringToLocalDateAndBack(){

        String dateUTCStr = "2016-06-12 14:54:15";
        Date dateLocal = DateUtils.getUtc(dateUTCStr);
        String dateUTCStrAfter = DateUtils.getUtc(dateLocal);
        assertEquals(dateUTCStr, dateUTCStrAfter);
    }

    @Test
    public void convertUTCDatWithMillisecondsToLocalDateAndBack(){

        String dateUTCStr = "2016-06-12 14:54:15.123";
        Date dateLocal = DateUtils.getUtc(dateUTCStr);
        String dateUTCStrAfter = DateUtils.getUtc(dateLocal);
        assertFalse(dateUTCStr.equals(dateUTCStrAfter));
    }

}
