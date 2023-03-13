import enums.ContextMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import support.CommonUtills

class Notifications: GlobalTestSettings() {

    val u = CommonUtills()
    var newPostID: String = ""
    var newDmID: String = ""


    @BeforeClass
    fun start(){
        pn.startBrowser()
        pn.loginToAdminPage()
    }

    @Test
    fun loginToApp() = runBlocking<Unit> {

        launch(Dispatchers.Default) {
            pnMobileA.startSession()
            pnMobileA.checkHomeScreenTooltip()
            pnMobileA.closeWelcomeTooltip()
        }

        launch(Dispatchers.Default) {
            pnMobileB.startSession()
            pnMobileB.checkHomeScreenTooltip()
            pnMobileB.closeWelcomeTooltip()
        }

    }

    @Test(dependsOnMethods = ["loginToApp"])
    fun checkNotificationWhenNewPostCreated(){
        val text = "new post to check Notification ${u.randomValue(100)}"
        pnMobileB.startConversation()
        pnMobileB.enterTextToPostInput(text)
        pnMobileB.returnToHomeScreenByIcon()
        newPostID = pnMobileB.findPostId(text)

        pn.openURL("https://admin-dev.letsopen.co/#/moderation/post/" + newPostID)
        pn.approvePost(text)

        pnMobileA.hideApp()
        pnMobileA.startToCheckNotification()
        val flag = pnMobileA.checkNotification(text)
        pnMobileA.showApp()

        Assert.assertTrue(flag, "After creating the post, the notification did not come")

        println("checkNotificationWhenNewPostCreated - Passed")
    }

    @Test(dependsOnMethods = ["checkNotificationWhenNewPostCreated"])
    fun checkNotificationWhenNewPostCommentCreated(){

        pnMobileA.openPostByID(newPostID)
        pnMobileA.joinToPost()
        pnMobileA.enterTextToPostInput("answer")
        pnMobileA.returnToHomeScreenByIcon()
        pn.openURL("https://admin-dev.letsopen.co/#/moderation/post/" + newPostID)
        pn.approvePost("answer")


        val text = "new comment to check Notification ${u.randomValue(100)}"
        pnMobileB.openPostByID(newPostID)
        pnMobileB.enterTextToPostInput(text)
        pnMobileB.returnToHomeScreenByIcon()
        pn.openURL("https://admin-dev.letsopen.co/#/moderation/post/" + newPostID)
        pn.approvePost(text)

        pnMobileA.hideApp()
        pnMobileA.startToCheckNotification()
        val flag = pnMobileA.checkNotification(text)
        pnMobileA.showApp()

        Assert.assertTrue(flag, "After creating the post comment, the notification did not come")

        println("checkNotificationWhenNewPostCommentCreated - Passed")
    }

    @Test(dependsOnMethods = ["checkNotificationWhenNewPostCommentCreated"])
    fun checkNotificationWhenDMCreated(){
        val text = "new DM to check Notification ${u.randomValue(100)}"
        pnMobileB.openPostByID(newPostID)
        pnMobileB.selectOptionInContextmenu(ContextMenu.MESSAGE_USER)
        pnMobileB.enterTextToPostInput(text)
        pnMobileB.returnToHomeScreenByIcon()
        pnMobileB.returnToHomeScreenByIcon()
        newDmID = pnMobileB.findPostId(text)
        val url = "https://admin-dev.letsopen.co/#/moderation/directMessagesChat/" + newDmID.replace(" ", "%20")
        pn.openURL(url)
        pn.approvePost(text)

        pnMobileA.hideApp()
        pnMobileA.startToCheckNotification()
        val flag = pnMobileA.checkNotification(text)
        pnMobileA.showApp()

        Assert.assertTrue(flag, "After creating the DM, the notification did not come")

        println("checkNotificationWhenDMCreated - Passed")
    }

    @Test(dependsOnMethods = ["checkNotificationWhenDMCreated"])
    fun checkNotificationWhenNewDmCommentCreated(){
        val text = "new DM comment to check Notification ${u.randomValue(100)}"
        pnMobileA.openPostByID(newDmID)
        pnMobileA.replyToDM()
        pnMobileA.returnToHomeScreenByIcon()

        pnMobileA.hideApp()
        pnMobileA.startToCheckNotification()
        val flag = pnMobileA.checkNotification(text)
        pnMobileA.showApp()

        Assert.assertTrue(flag, "After creating the DM comment, the notification did not come")

        println("checkNotificationWhenNewDmCommentCreated - Passed")
    }



}