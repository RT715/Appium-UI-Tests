import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.Selenide.sleep
import enums.ContextMenu
import org.testng.Assert
import org.testng.annotations.BeforeMethod
//import org.testng.annotations.Listeners
import org.testng.annotations.Test

//@Listeners(TestResultListener::class)
class SmokeTest : FlowBasic() {

    @BeforeMethod
    fun sleepRunTimeSmoke(){
        sleep(1000)
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun sharePostCreating() {
        pnMobileA.logout()
        val postTitle = pnMobileA.createSharePost()
        pnMobileA.passOnboarding("")
        pnMobileA.checkHomeScreenTooltip()
        pnMobileA.closeWelcomeTooltip()

        val postShareID = pnMobileA.findPostId(postTitle)
        pn.openURL("$baseURL/#/moderation/post/$postShareID")
        Assert.assertTrue(pn.checkPostText(postTitle, true),
                "Share post $postTitle not finding on Home Screen.")
        pn.approvePost(postTitle)
        val postsEnabled = pnMobileB.checkPostsFromHomeScreen(postTitle)
        Assert.assertTrue(postsEnabled,
            "Post $globalPostTitle not finding by another user."
        )

        println("sharePostCreating - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun createCommentWithEmoji() {
        pnMobileA.gotoHomeScreen()
        val comment = "🙂"
        pnMobileA.openPostByID(postID)
        pnMobileA.enterTextToPostInput(comment)
        pn.openURL("$baseURL/#/moderation/post/$postID")
        pn.approvePost(comment)
        pnMobileA.gotoHomeScreen()
        Assert.assertTrue(pn.checkPostText(comment, true),
            "Subtitle $comment for Post  $globalPostTitle not finding."
        )

        println("createCommentWithEmoji - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun createCommentWith300Symbols() {
        pnMobileA.gotoHomeScreen()
        val comment = "Three hundred characters. Three hundred characters. Three hundred characters. " +
                "Three hundred characters. Three hundred characters. Three hundred characters. " +
                "Three hundred characters. Three hundred characters. Three hundred characters. " +
                "Three hundred characters. Three hundred characters. Three hundr..."
        pnMobileA.openPostByID(postID)
        pnMobileA.enterTextToPostInput(comment)
        pn.openURL("$baseURL/#/moderation/post/$postID")
        pn.approvePost(comment)
        pnMobileA.gotoHomeScreen()
        //String subTitle = pnMobile.checkPostSubTitleOnHomeScreen(globalPostTitle);
        Assert.assertTrue(pn.checkPostText(comment, true),
            "Subtitle $comment for Post  $globalPostTitle not finding."
        )

        println("createCommentWith300Symbols - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun checkPossibleToLeaveOwnConversation() {
        pnMobileA.gotoHomeScreen()
        pnMobileA.openPostByID(postID)
        pnMobileA.leaveAPost()
        pnMobileA.returnToHomeScreenByIcon()
        pnMobileA.openPostByID(postID)
        pnMobileA.joinToPost()
        val flag = pnMobileA.checkBadge()

        pnMobileA.returnToHomeScreenByIcon()

        Assert.assertTrue(flag,
            "The badge name is incorrect")

        println("checkPossibleToLeaveOwnConversation - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun checkPossibleToLeaveFriendConversation() {
        pnMobileB.openPostByID(postID)
        pnMobileB.leaveAPost()
        pnMobileB.returnToHomeScreenByIcon()
        pnMobileB.openPostByID(postID)
        pnMobileB.joinToPost()
        val flag = pnMobileB.checkFriendsBadge(userA.nicName)

        pnMobileB.gotoHomeScreen()

        Assert.assertTrue(flag,
            "The badge name is incorrect")

        println("checkPossibleToLeaveFriendConversation - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun navigationFromDMToParentPost() {
        pnMobileA.gotoHomeScreen()
        pnMobileA.openPostByID(dmID)
        val parentPostTitleLink = pnMobileA.navigateFromDMChatToParent()
        val flag = pnMobileA.checkPostTitleFromPost(globalPostTitle)

        pnMobileA.gotoHomeScreen()

        Assert.assertTrue(parentPostTitleLink == globalPostTitle,
            "Incorrect parent post link name in DM chat")

        Assert.assertTrue(flag,
            "Navigation from DM chat to Parent post is not work")
        println("navigationFromDMToParentPost - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun sendingAMessageAfterLeavingAJoiningDM() {
        pnMobileB.gotoHomeScreen()
        pnMobileB.openPostByID(dmID)
        pnMobileB.replyToDM()
        pnMobileB.clickToMoreButton()
        pnMobileB.ignoreConversation()
        pnMobileB.replyToDM()
        val message = "DM message №" + utills.randomValue(2).toString()
        pnMobileB.enterTextToPostInput(message)
        val url = "$baseURL/#/moderation/directMessagesChat/" + dmID.replace(" ", "%20")
        pn.openURL(url)

        pnMobileB.gotoHomeScreen()

        Assert.assertTrue(pn.checkPostText(message, true),
            "Subtitle $message of created Post not finding.")

        println("sendingAMessageAfterLeavingAJoiningDM - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun sendingAMessageAfterLeavingAJoiningParentPost() {
        pnMobileA.gotoHomeScreen()
        pnMobileA.openPostByID(postID)
        pnMobileA.leaveAPost()
        pnMobileA.joinToPost()
        val message = "message №" + utills.randomValue(2).toString()
        pnMobileA.enterTextToPostInput(message)
        val url = "$baseURL/#/moderation/post/" + postID.replace(" ", "%20")
        pn.openURL(url)
        pn.approvePost(message)
        pnMobileA.gotoHomeScreen()

        Assert.assertTrue(pn.checkPostText(message, true),
            "Subtitle $message of created Post not finding.")

        println("sendingAMessageAfterLeavingAJoiningParentPost - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun checkThatNicNamePreservationAfterLeavingAJoiningParentPost() {
        pnMobileA.openPostByID(postID)
        pnMobileA.leaveAPost()
        pnMobileA.joinToPost()
        val flag = pnMobileA.checkBadge()

        pnMobileA.returnToHomeScreenByIcon()

        Assert.assertTrue(flag,
            "The badge name is incorrect")

        println("checkThatNicNamePreservationAfterLeavingAJoiningParentPost - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun checkForReportAbility() {
        pnMobileA.openPostByID(postID)
        pnMobileA.reportToMessage()
        val text = pnMobileA.checkTextFromAlertBar()
        Assert.assertTrue(text.contains("Thank you"),
                "Error while reporting message - $text")

        pnMobileA.gotoHomeScreen()

        println("checkForReportAbility - Passed")
    }

    @Test(dependsOnMethods = ["checkForReportAbility"])
    fun disabilityOfReportingTheSameMessageTwice() {
        pnMobileA.openPostByID(postID)
        pnMobileA.reportToMessage()
        val text = pnMobileA.checkTextFromAlertBar()
        pnMobileA.gotoHomeScreen()
        Assert.assertTrue(!text.contains("Oops! Something went wrong."),
                "Error while reporting message - $text")

        println("disabilityOfReportingTheSameMessageTwice - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun checkDeleteOwnMessage() {
        pnMobileA.openPostByID(postID)

        val message = "message №" + utills.randomValue(2).toString()
        pnMobileA.enterTextToPostInput(message)
        //val url = "https://admin-dev.letsopen.co/#/moderation/post/" + postID.replace(" ", "%20")
        //pn.openURL(url)
        //pn.approvePost(message)

        val text = pnMobileA.checkTextFromLastComment()
        pnMobileA.deleteOwnMessage()
        open(("$baseURL/#/moderation/post/$postID"))

        pnMobileA.returnToHomeScreenByIcon()

        Assert.assertTrue(!pn.checkPostText(text, false),
            "Own message is not deleted. Text - $text")

        println("checkDeleteOwnMessage - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun abilityToBlockUser() {
        pnMobileA.openPostByID(postID)
        pnMobileA.selectOptionInContextmenu(ContextMenu.BLOCK_USER)
        pnMobileA.returnToHomeScreenByIcon()

        pnMobileB.startConversation()
        val tempGlobalPostTitle = "An autotest post №" + utills.randomValue(5).toString()
        pnMobileB.enterTextToPostInput(tempGlobalPostTitle)
        pnMobileB.returnToHomeScreenByIcon()

        var tempPostID = pnMobileB.findPostId(tempGlobalPostTitle)
        pn.openURL("$baseURL/#/moderation/post/$tempPostID")
        pn.approvePost(tempGlobalPostTitle)

        tempPostID = pnMobileA.checkAbsentenseOfAwaitingPost(tempGlobalPostTitle)
        Assert.assertTrue(tempPostID == "",
                "Users blocking does not work. Post found: $tempPostID")

        println("abilityToBlockUser - Passed")
    }

    @Test(dependsOnMethods = ["abilityToBlockUser"])
    fun abilityToUNBlockUser() {
        pnMobileB.startConversation()
        val tempGlobalPostTitle = "An autotest post №" + utills.randomValue(5).toString()
        pnMobileB.enterTextToPostInput(tempGlobalPostTitle)
        pnMobileB.returnToHomeScreenByIcon()
        val tempPostID = pnMobileB.findPostId(tempGlobalPostTitle)

        pnMobileA.openPostByID(postID)
        pnMobileA.selectOptionInContextmenu(ContextMenu.UNBLOCK_USER)
        pnMobileA.returnToHomeScreenByIcon()

        pn.openURL("$baseURL/#/moderation/post/$tempPostID")
        pn.approvePost(tempGlobalPostTitle)
        sleep(6000)
        val tempPostID2 = pnMobileA.findPostId(tempGlobalPostTitle)

        Assert.assertTrue(tempPostID2 == tempPostID,
                "Users unblocking is not work")

        println("abilityToUNBlockUser - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun subtitleVerificationFromRejectedMessage() {
        pn.unSuspenseUser(userA.phone)
        val message = "Some Text To Reject"
        val reason = "Removed for being mean"
        pnMobileA.openPostByID(postID)
        pnMobileA.enterTextToPostInput(message)
        pn.openURL("$baseURL/#/moderation/post/$postID")
        pn.rejectMessage(message)

        val enabledWarning = pnMobileA.checkSuspensionWarning()
        val warning = pnMobileA.checkSuspenseReasonFromComment(message, reason)
        pnMobileA.navigateBack()

        Assert.assertTrue(enabledWarning,
                "Suspense warning is not appears. Warning enabled - $enabledWarning")
        Assert.assertTrue(warning == reason,
                "Suspense reason is not shown or incorrect. Reason is - $warning")

        println("subtitleVerificationFromRejectedMessage - Passed")
    }

    @Test(dependsOnMethods = ["subtitleVerificationFromRejectedMessage"])
    fun suspendedUser() {
        val message = "Some Text To Reject2"
        pnMobileA.openPostByID(postID)
        pnMobileA.enterTextToPostInput(message)
        pn.openURL("$baseURL/#/moderation/post/$postID")
        pn.rejectMessage(message)

        val text = pnMobileA.checkSuspensionStatus()
        pnMobileA.navigateBack()

        Assert.assertTrue(text.contains("Your account has been suspended"),
                "Suspense message is not shown or incorrect")
        println("suspendedUser - Passed")
    }

    @Test(dependsOnMethods = ["suspendedUser"])
    fun checkDisabilityOfSuspendedUserCreateAMessage() {
        pnMobileA.openPostByID(postID)
        val text = pnMobileA.tryToEnterTextToPostInput()
        pnMobileA.navigateBack()

        Assert.assertTrue(text == "",
                "Suspense message is not shown. Reason: $text")

        println("checkDisabilityOfSuspendedUserCreateAMessage - Passed")
    }

    @Test(dependsOnMethods = ["checkDisabilityOfSuspendedUserCreateAMessage"])
    fun unSuspendUser() {
        pn.unSuspenseUser(userA.phone)
        pnMobileA.openPostByID(postID)
        val text = pnMobileA.tryToEnterTextToPostInput()
        pnMobileA.navigateBack()

        Assert.assertTrue(text != "",
                "User unsuspense process not work)")

        println("unSuspendUser - Passed")
    }


}

