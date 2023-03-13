import org.testng.annotations.Test

@Test
class Experimental: GlobalTestSettings(){

    fun checkPassWelcomeTourBySwipe(){
        pnMobileB.resetApp()
        pnMobileB.passStartScreen()
        pnMobileB.passWelcomeTourBySwipe()
    }
}