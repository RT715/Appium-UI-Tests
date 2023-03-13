import org.testng.Assert
import org.testng.annotations.Test

open class CommentScreen : FlowBasic() {

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun disabilityToSubmitLinks(){
        pnMobileA.openPostByID(postID)
        pnMobileA.enterTextToPostInput("www.google.com")
        val flag = pnMobileA.checkAlert()
        pnMobileA.navigateBack()
        Assert.assertTrue(flag, "When trying to send links, the alert did not appear")

        println("disabilityToSubmitLinks - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun disabilityToSendBlankMessage(){
        pnMobileA.openPostByID(postID)
        pnMobileA.enterTextToPostInput("")
        val text = pnMobileA.checkTextFromLastComment()
        pnMobileA.navigateBack()
        Assert.assertTrue(text != "", "Blank message can be send")

        println("disabilityToSendBlankMessage - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun cancelDeleteOwnMessage(){
        pnMobileA.openPostByID(postID)
        val text = pnMobileA.checkTextFromLastComment()
        pnMobileA.cancelDeleteOwnMessage()
        pnMobileA.navigateBack()

        Assert.assertTrue(!pn.checkPostText(text, false),
                "Own message is deleted. Text - $text")

        println("cancelDeleteOwnMessage - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun disabilityOfDeletingOtherUserMessages(){
        pnMobileA.openPostByID(postID)
        val flag= pnMobileA.deleteFriendMessage()

        pnMobileA.navigateBack()
        pnMobileA.navigateBack()

        Assert.assertTrue(!flag,
                "You may can delete friend message")
        println("disabilityOfDeletingOtherUserMessages - Passed")
    }

    @Test(priority = 1)
    fun noInternetConnectionMessage(){
        pnMobileA.openPostByID(postID)
        pnMobileA.turnOFFInternetConnection(userA)
        val text =  pnMobileA.checkInternetConnectionFromPost()
        pnMobileA.navigateBack()
        Assert.assertTrue(text == "No internet connection",
                "After switchOFF internet connection correct alert is not appears")

        println("noInternetConnectionMessage - Passed")
    }

    @Test(priority = 2)
    fun checkMessageSubmissionWithoutInternetConnection(){
        val message = "no internet connection test"
        val reason = "Couldn't send message. Tap to try again."
        pnMobileA.openPostByID(postID)
        pnMobileA.turnOFFInternetConnection(userA)
        pnMobileA.enterTextToPostInput(message)
        val warning = pnMobileA.checkSuspenseReasonFromComment(message, reason)
        pnMobileA.turnOFFInternetConnection(userA)
        pnMobileA.navigateBack()

        Assert.assertTrue(reason.contains(warning),
                "After switchOFF internet connection correct message is not appears")

        println("checkMessageSubmissionWithoutInternetConnection - Passed")
    }

    @Test(dependsOnMethods = ["dmPostCreation"])
    fun draftedMessageVerification(){
        val test = "test"
        pnMobileA.openPostByID(postID)
        pnMobileA.enterTextToPostInputButNotSend(test)
        pnMobileA.navigateBack()
        pnMobileA.openPostByID(postID)
        val text = pnMobileA.checkTextInInputPost()
        pnMobileA.navigateBack()

        Assert.assertTrue(text == test,
                "Drafted message is not saved")

        println("draftedMessageVerification - Passed")
    }
}
