package social.entourage.android.authentication.login.register;

/**
 * Created by mihaiionescu on 17/06/16.
 */
public interface OnRegisterUserListener {

    void registerShowSignIn();
    void registerSavePhoneNumber(String phoneNumber);
    void registerCheckCode(String smsCode);
    void registerResendCode();
}
