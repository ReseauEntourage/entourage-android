package social.entourage.android.authentication.login.register;

/**
 * Created by mihaiionescu on 17/06/16.
 */
public interface OnRegisterUserListener {

    void registerShowSignIn();
    boolean registerStart();
    void registerSavePhoneNumber(String phoneNumber);
    void registerCheckCode(String smsCode);
    void registerResendCode();
    void registerClosePop(Boolean isShowLogin);  //Hack en attendant la nouvelle version de l'onboarding
}
