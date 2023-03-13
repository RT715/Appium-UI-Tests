import enums.Screen
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class StartScreen : GlobalTestSettings() {

    @BeforeMethod
    fun resetApp(){
        pnMobileA.resetApp()
    }

    @Test
    fun checkIncorrectPhoneNumber() {
        pnMobileA.passStartScreen()
        pnMobileA.passWelcomeTour()
        pnMobileA.setPhone(userA.phone + "0")
        val err = pnMobileA.checkErrors()
        Assert.assertTrue(err.contains("The number looks incorrect. Please check it and try again"),
                "Incorrect error text - $err")

        println("createCommentWithEmoji - Passed")
    }

    @Test
    fun checkHideApp() {
        pnMobileA.navigateBack()
        Assert.assertTrue(pnMobileA.checkHidenApp(),
                "App is not hiden")

        println("checkHideApp - Passed")
    }

    @Test
    fun authorisationWithIncorrectCode(){
        pnMobileA.passStartScreen()
        pnMobileA.passWelcomeTour()
        pnMobileA.setPhone(userA.phone)
        pnMobileA.setPinCode("0 0 0 0 0 0")
        Assert.assertTrue(pnMobileA.checkScreen(Screen.ONBOARDING),
                "Authorisation with incorrect phone is passed")

        println("authorisationWithIncorrectCode - Passed")

    }

    @Test
    fun codeReEntering(){
        pnMobileA.passStartScreen()
        pnMobileA.passWelcomeTour()
        pnMobileA.setPhone(userA.phone)
        pnMobileA.setPinCode("0 0 0 0 0 0")
        pnMobileA.clearPinCode();
        pnMobileA.setPinCode(userA.pinCode)
        Assert.assertTrue(pnMobileA.checkScreen(Screen.ONBOARDING),
                "Pincode reentering is incorrect")

        println("codeReEntering - Passed")
    }

    @Test
    fun incorrectCodeErrorMessage(){
        pnMobileA.passStartScreen()
        pnMobileA.passWelcomeTour()
        pnMobileA.setPhone(userA.phone)
        pnMobileA.setPinCode("0 0 0 0 0 0")
        val error = pnMobileA.checkTextFromAlertBar()
        Assert.assertTrue(error.contains("The code was incorrect. Please try again"),
                "No Error message when you try to auth with incorrect pincode")

        println("incorrectCodeErrorMessage - Passed")
    }

    @Test
    fun IncorrectPhoneNumberErrorMessage(){
        pnMobileA.passStartScreen()
        pnMobileA.passWelcomeTour()
        pnMobileA.setPhone(userA.phone + "0")
        val error = pnMobileA.checkErrors()
        Assert.assertTrue(error.contains("incorrect."),
                "No Error message when you try to auth with incorrect pincode")

        println("incorrectCodeErrorMessage - Passed")
    }

    @Test
    fun backToPhoneRegistrationScreen(){
        pnMobileA.passStartScreen()
        pnMobileA.passWelcomeTour()
        pnMobileA.setPhone(userA.phone)
        pnMobileA.returnToHomeScreenByIcon()
        Assert.assertTrue(pnMobileA.checkScreen(Screen.PHONE_SCREEN),
                "Return button from pincode screen not work")

        println("backToPhoneRegistrationScreen - Passed")
    }

}

