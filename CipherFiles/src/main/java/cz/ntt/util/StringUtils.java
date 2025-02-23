package cz.ntt.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtils {

    /**
     *
     * @param text Inserted text
     * @return Pulls the date from the text
     */
    public static String parseDateFromString(String text){
        Pattern regex = Pattern.compile("_([\\d\\-_]{19})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(text);
        if (matcher.find()){
            return matcher.group(1);
        }
        return "";
    }


}
