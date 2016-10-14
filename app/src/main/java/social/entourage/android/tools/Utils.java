package social.entourage.android.tools;

import android.util.Patterns;

/**
 * Created by mihaiionescu on 27/07/16.
 */
public class Utils {

    // This class should not be instantiated
    private Utils() {

    }

    public static String checkPhoneNumberFormat(String phoneNumber) {

        if (phoneNumber.startsWith("0")) {
            phoneNumber = "+33" + phoneNumber.substring(1);
        } else if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + phoneNumber;
        }

        if(Patterns.PHONE.matcher(phoneNumber).matches())
            return phoneNumber;

        return null;
    }

    public static String checkEmailFormat(String email) {
        if (email != null && !email.equals("")) {
            return email;
        }
        return null;
    }
}
