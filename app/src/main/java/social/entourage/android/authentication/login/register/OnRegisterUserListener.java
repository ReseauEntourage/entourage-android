package social.entourage.android.authentication.login.register;

/**
 * Created by mihaiionescu on 17/06/16.
 */
public interface OnRegisterUserListener {

    public void registerShowSignIn();
    public void registerSavePhoneNumber(String phoneNumber);
    public void registerCheckCode(String smsCode);
    public void registerResendCode();

}
