package social.entourage.android.authentication.login.register

/**
 * Created by mihaiionescu on 17/06/16.
 */
interface OnRegisterUserListener {
    fun registerShowSignIn()
    fun registerStart(): Boolean
    fun registerSavePhoneNumber(phoneNumber: String)
    fun registerCheckCode(smsCode: String)
    fun registerResendCode()
    fun registerClosePop(isShowLogin: Boolean) //Hack en attendant la nouvelle version de l'onboarding
}