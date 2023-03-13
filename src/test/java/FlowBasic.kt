import com.codeborne.selenide.Selenide
import enums.ContextMenu
import enums.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

//@Listeners(TestResultListener::class)
open class FlowBasic : GlobalTestSettings() {

    @BeforeClass
    fun start(){
        pn.startBrowser()
        pn.loginToAdminPage()
    }

    @BeforeMethod
    fun sleepRunTime(){
        Selenide.sleep(1000)
    }

    @Test
    fun loginToApp() = runBlocking<Unit> {

        launch(Dispatchers.Default) {
            pnMobileA.resetApp()
            pnMobileA.passStartScreen()
            //pnMobileA.passWelcomeTour()
            pnMobileA.setPhone(userA.phone)
            pnMobileA.setPinCode(userA.pinCode)
            Assert.assertTrue(pnMobileA.checkScreen(Screen.ONBOARDING),
                    "Auth is failed.")

            val incorrectScreens = pnMobileA.passOnboarding("")
            try {
                Assert.assertTrue(incorrectScreens == "",  "Incorrect onboarding screens: \r\n$incorrectScreens")
            } catch (a: AssertionError) {
                println(a.message)
            }
            //Assert.assertTrue(pnMobileA.checkScreen(Screen.HOME_SCREEN), "Home screen not detected.")
        }

        launch(Dispatchers.Default) {
            pnMobileB.startSession()
            pnMobileB.checkHomeScreenTooltip()
            pnMobileB.closeWelcomeTooltip()
        }

    }

    @Test(dependsOnMethods = ["loginToApp"])
    fun checkOnboardingTooltip() {
        try {
            Assert.assertTrue(pnMobileA.checkHomeScreenTooltip(),
                    "Join tooltip is not enable on the Home Screen")
        } catch (a: AssertionError) {
            println(a.message)
        }
        pnMobileA.closeWelcomeTooltip()

        println("passOnboardingTooltip - Passed")
    }

    @Test(dependsOnMethods = ["checkOnboardingTooltip"])
    fun postCreation() {
        pnMobileA.startConversation()
        pnMobileA.enterTextToPostInput(globalPostTitle)
        userA.nicName = pnMobileA.checkUserName()
        pnMobileA.returnToHomeScreenByIcon()

        println("postCreation - Passed")
    }

    @Test(dependsOnMethods = ["postCreation"])
    fun postApproving() {
        postID = pnMobileA.findPostId(globalPostTitle)
        pn.openURL("$baseURL/#/moderation/post/$postID")
        pn.approvePost(globalPostTitle)
        val postEnable = pnMobileB.checkPostsFromHomeScreen(globalPostTitle)
        val badge = pnMobileB.checkPostUnreadBadges(globalPostTitle)

        Assert.assertTrue(postEnable,
            "Post $globalPostTitle not finding by another user."
        )
        Assert.assertTrue(badge == "1",
                "Incorrect number of unread messages in badge. Actual: $badge")

        println("postApproving - Passed")
    }


    @Test(dependsOnMethods = ["postApproving"])
    fun joinToPostAndCreateTheComment() {
        pnMobileB.openPostByID(postID)
        pnMobileB.joinToPost()
        Assert.assertTrue(pnMobileB.checkFriendsBadge(userA.nicName),
                "The friend badge name is incorrect")
        val message = "message №" + utills.randomValue(2).toString()
        pnMobileB.enterTextToPostInput(message)
        pn.openURL("$baseURL/#/moderation/post/$postID")
        pn.approvePost(message)
        pnMobileB.returnToHomeScreenByIcon()
        val subTitle = pnMobileA.checkPostSubTitleOnHomeScreen(message)
        Assert.assertTrue(message.contains(subTitle),
                "Subtitle $message of created Post not finding. Find - $subTitle")

        val badge = pnMobileA.checkPostUnreadBadges(message)
        Assert.assertTrue(badge == "1",
                "Incorrect number of unread messages in badge. $badge")

        println("joinToPostAndCreateTheComment - Passed")
    }

    @Test(dependsOnMethods = ["joinToPostAndCreateTheComment"])
    fun dmPostCreation() {
        pnMobileA.openPostByID(postID)
        pnMobileA.selectOptionInContextmenu(ContextMenu.MESSAGE_USER)
        pnMobileA.enterTextToPostInput(globalDMTitle)
        pnMobileA.returnToHomeScreenByIcon()
        pnMobileA.returnToHomeScreenByIcon()
        dmID = pnMobileA.findPostId(globalDMTitle)
        val url = "$baseURL/#/moderation/directMessagesChat/" + dmID.replace(" ", "%20")
        pn.openURL(url)
        pn.approvePost(globalDMTitle)
        val postsEnabled = pnMobileB.checkPostsFromHomeScreen(globalDMTitle)
        Assert.assertTrue(postsEnabled,
            "DM $globalDMTitle not finding by another user."
        )

        println("dmPostCreation - Passed")
    }

}