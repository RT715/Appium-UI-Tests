import com.codeborne.selenide.Selenide.sleep
import enums.*
import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileElement
import io.appium.java_client.PerformsTouchActions
import io.appium.java_client.TouchAction
import io.appium.java_client.touch.WaitOptions
import io.appium.java_client.touch.offset.ElementOption
import io.appium.java_client.touch.offset.PointOption
import org.openqa.selenium.By
import support.CommonUtills
import support.User
import java.awt.Point
import java.time.Duration
import java.time.ZonedDateTime

class PlatformTouchAction(performsTouchActions: PerformsTouchActions) :
        TouchAction<PlatformTouchAction>(performsTouchActions)

class PageNavigatorIOS(private val mobDriver: AppiumDriver<MobileElement>, private val user: User) : PageNavigatorMobile(mobDriver, user)  {
    private val utills: CommonUtills = CommonUtills()
    private val screensFromRemoteConfig: MutableList<String> = utills.parseDataFromRemoteConfigJSON()

    override fun startSession() {
        resetApp()
        passStartScreen()
        //passWelcomeTour()
        setPhone(user.phone)
        setPinCode(user.pinCode)
        passOnboarding("")
    }

    override fun resetApp() {
        while (true) {
            when {
                isElementPresent("Back", SearchType.byAccessibilityId) -> {
                    val back = mobDriver.findElementByAccessibilityId("Back") as MobileElement
                    back.click()
                }
                isElementPresent("Done", SearchType.byAccessibilityId) -> {
                    val back = mobDriver.findElementByAccessibilityId("Done") as MobileElement
                    back.click()
                }
                isElementPresent("Close", SearchType.byAccessibilityId) -> {
                    val back = mobDriver.findElementByAccessibilityId("Close") as MobileElement
                    back.click()
                }
            }

            if (checkScreen(Screen.POST)) gotoHomeScreen()
            else if (checkScreen(Screen.START_SCREEN)) return
            else if (checkScreen(Screen.ONBOARDING)) passOnboarding("")
            else if (checkScreen(Screen.HOME_SCREEN)) break
        }
        logout()
    }

    private fun navigationBarValue(screen: Screen): Boolean {
        return when (screen) {
            Screen.POST -> isElementPresent("Back", SearchType.byAccessibilityId)
            Screen.HOME_SCREEN -> isElementPresent("Conversations", SearchType.byAccessibilityId)
            Screen.ONBOARDING -> isElementPresent("XCUIElementTypeNavigationBar", SearchType.byClassName)
            Screen.START_SCREEN -> isElementPresent("openlogo", SearchType.byAccessibilityId)
            else -> false
        }
    }

    override fun checkScreen(screen: Screen): Boolean {
        return navigationBarValue(screen)
    }

    override fun checkPostsFromHomeScreen(globalPostTitle: String): Boolean {
        findPost(globalPostTitle)

        return true
    }

    override fun findPostId(globalPostTitle: String): String {
        val post = findPost(globalPostTitle)

        return post!!.getAttribute("name")
    }


    override fun checkPostSubTitleOnHomeScreen(message: String): String {
        val post = findPost(message)

        val text = post!!.findElementsByClassName("XCUIElementTypeStaticText")[2].text
        return if (text == message)
            return text
        else
            return post.findElementsByClassName("XCUIElementTypeStaticText")[0].text

        return ""
    }

    override fun checkPostUnreadBadges(globalPostTitle: String): String {
        val post = findPost(globalPostTitle)
        val elementsInPost = post!!.findElementsByClassName("XCUIElementTypeStaticText")
        for (element in elementsInPost){
            val badge = element.text.toIntOrNull()
            if (badge != null) {
                return badge.toString()
            }
        }
        return ""
    }

    private fun findPost(globalPostTitle: String): MobileElement? {
        while(true) {
            val posts = awaitForCollectionForming("XCUIElementTypeCell")
            for (post in posts) {
                try {
                    val postTitle0 = post.findElementsByClassName("XCUIElementTypeStaticText")[0].text
                    val postTitle1 = post.findElementsByClassName("XCUIElementTypeStaticText")[1].text
                    val postTitle2 = post.findElementsByClassName("XCUIElementTypeStaticText")[2].text
                    if (globalPostTitle.contains(postTitle0) ||
                            globalPostTitle.contains(postTitle1) ||
                            globalPostTitle.contains(postTitle2))
                        return post
                }
                catch (e: Exception) {
                    println(e.message)
                    continue
                }
            }
        }

        return null
    }

    override fun checkAbsentenseOfAwaitingPost(globalPostTitle: String) : String{
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(30)
        var duration = Duration.between(now, dtOnFuture)
        while (!duration.isNegative) {
            sleep(1000)
            val posts = awaitForCollectionForming("XCUIElementTypeCell")
            for (post in posts) {
                try {
                    val postTitle0 = post.findElementsByClassName("XCUIElementTypeStaticText")[0].text
                    val postTitle1 = post.findElementsByClassName("XCUIElementTypeStaticText")[1].text
                    val postTitle2 = post.findElementsByClassName("XCUIElementTypeStaticText")[2].text
                    if (globalPostTitle.contains(postTitle0) ||
                            globalPostTitle.contains(postTitle1) ||
                            globalPostTitle.contains(postTitle2))
                        return globalPostTitle
                }
                catch (e: Exception) {
                    println(e.message)
                    continue
                }
            }

            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }
        return ""
    }

    private fun awaitForCollectionForming(query: String) : List<MobileElement>  {
        var resultCollection : List<MobileElement>
        if (!waitRequiredElement(query, SearchType.byClassName)){
            mobDriver.resetApp()
            sleep(5000)
        }
        while(true) {
            resultCollection = mobDriver.findElementsByClassName(query)
            var length = resultCollection.size;
            sleep(1000)
            resultCollection = mobDriver.findElementsByClassName(query)
            var lenght2 = resultCollection.size;

            if (length == lenght2 && lenght2 != 0)
                break
        }

        var l = resultCollection.size
        if (l > 5) l = 5
        return resultCollection.subList(0 , l);
    }


    override fun passStartScreen() {
        waitRequiredElement("//XCUIElementTypeButton[@name=\"Get Started\"]", SearchType.byXPath)
        val getStartedButton: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Get Started\"]")
        getStartedButton.click()
    }

    override fun passWelcomeTour(){
        val nextButton: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeStaticText[@name=\"Next\"]")
        for(button in 0..2)
            nextButton.click()
    }

    override fun passWelcomeTourBySwipe(){
        waitRequiredElement("\t\n" +
                "//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther", SearchType.byXPath)
        val element = mobDriver.findElementByXPath("\t\n" +
                "//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther")

        swipeTo(element, Position.LEFT, -300)      // From right to left
        swipeTo(element, Position.RIGHT, +300)      // From left to right

        for(swipe in 0..2)
            swipeTo(element, Position.LEFT, -300)

        waitRequiredElement("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeScrollView/XCUIElementTypeOther[1]/XCUIElementTypeOther[2]/XCUIElementTypeOther/XCUIElementTypeTextField[2]", SearchType.byXPath)
    }

    override fun setPhone(phone: String) {
        phone.split(" ").toTypedArray()
        phone[0]
        phone[1]
        val xPathTo = "//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeTextField[1]"
        waitUntil(xPathTo, SearchType.byXPath, 5)
        val changeArea = mobDriver.findElementByXPath(xPathTo) as MobileElement
        changeArea.click()
        val countrySelector = mobDriver.findElementByXPath("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[3]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypePicker/XCUIElementTypePickerWheel") as MobileElement
        swipeTo(countrySelector, Position.TOP, 67)
        val doneButtonArea = mobDriver.findElementByXPath("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[3]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther[1]") as MobileElement
        tapTo(doneButtonArea, Position.RIGHT)
        sleep(200)
        val phoneInput = mobDriver.findElementByXPath("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeScrollView/XCUIElementTypeOther[1]/XCUIElementTypeOther[2]") as MobileElement
        phoneInput.sendKeys(phone)
        val nextPhoneButton = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Next\"]") as MobileElement
        nextPhoneButton.click()
    }



    override fun setPinCode(pincode: String) {
        val xPathTo = "//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeScrollView/XCUIElementTypeOther[1]/XCUIElementTypeOther[1]/XCUIElementTypeTextField"
        waitRequiredElement(xPathTo, SearchType.byXPath)
        val pinInput = mobDriver.findElementByXPath(xPathTo) as MobileElement
        pinInput.sendKeys(pincode.replace(" ", ""))
        val nextButton = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Next\"]") as MobileElement
        nextButton.click()
        checkScreen(Screen.ONBOARDING)
    }

    override fun passOnboarding(stopScreen: String): String {
        var incorrectScreen = ""
        sleep(2000)
        for (s in screensFromRemoteConfig) {
            val screenElements = s.split("@")
            val screen = screenElements[0]
            val headerText = screenElements[1]
            val buttonText = screenElements[2]

            if (screen == stopScreen)
                break

            if (!isElementPresent(headerText, SearchType.byAccessibilityId))
                incorrectScreen = "$incorrectScreen@$headerText"

            when (screen) {
                "Rules" -> {
                    if (waitRequiredElement("//XCUIElementTypeButton[@name=\"I agree\"]", SearchType.byXPath)) {
                        val agree: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"I agree\"]")
                        agree.click()
                    }
                }

                "Age" -> {
                    if (waitUntil("//XCUIElementTypeButton[@name=\"Next\"]", SearchType.byXPath, 5)) {
                        val age: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeTextField")
                        age.sendKeys("18")
                        val nextButton: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Next\"]")
                        nextButton.click()
                    }
                }

                "defaultConversationPrompt" -> {
                    if (waitUntil("Skip", SearchType.byAccessibilityId, 5)) {
                        val agree: MobileElement = mobDriver.findElementByAccessibilityId("Skip")
                        agree.click()
                    }
                }

                "defaultAllowNotifications" -> {
                    if (waitUntil( "//XCUIElementTypeButton[@name=\"Allow\"]", SearchType.byXPath, 5)) {
                        val mobileButton: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Allow\"]")
                        mobileButton.click()

                        if (waitUntil("Разрешить", SearchType.byAccessibilityId, 5)) {
                            val button: MobileElement = mobDriver.findElementByAccessibilityId("Разрешить")
                            button.click()
                        }
                        if (waitUntil("Allow", SearchType.byAccessibilityId, 5)) {
                            val button: MobileElement = mobDriver.findElementByAccessibilityId("Allow")
                            button.click()
                        }
                    }
                }

                "defaultConnectContacts" -> {
                    if (waitUntil("//XCUIElementTypeButton[@name=\"OK\"]", SearchType.byXPath, 5)) {
                        val mobileButton: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"OK\"]")
                        mobileButton.click()

                        if (waitUntil("Разрешить", SearchType.byAccessibilityId, 5)) {
                            val button: MobileElement = mobDriver.findElementByAccessibilityId("Разрешить")
                            button.click()
                        }
                        if (waitUntil("OK", SearchType.byAccessibilityId, 5)) {

                            val button: MobileElement = mobDriver.findElementByAccessibilityId("OK")
                            button.click()
                        }
                    }
                }

                "defaultInvites" -> {
                    if (waitUntil("//XCUIElementTypeButton[@name=\"Invite All Contacts\"]", SearchType.byXPath, 2)) {
                        val invite: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Invite All Contacts\"]")
                        invite.click()
                    }

                    if (waitUntil("//XCUIElementTypeButton[@name=\"Invite All\"]", SearchType.byXPath, 2)){
                        val invite : MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Invite All\"]")
                        invite.click()
                    }

                    if (waitUntil("//XCUIElementTypeButton[@name=\"OK\"]", SearchType.byXPath, 2)){
                        val invite : MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"OK\"]")
                        invite.click()
                    }
                }
            }
        }

        return incorrectScreen
    }

    override fun checkHomeScreenTooltip(): Boolean {
        val flag = waitRequiredElement("//XCUIElementTypeButton[@name=\"View Conversation\"]", SearchType.byXPath)
        if (flag) {
            val convo = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"View Conversation\"]") as MobileElement
            convo.click()
            waitRequiredElement("Back", SearchType.byAccessibilityId)
            val back = mobDriver.findElementByAccessibilityId("Back") as MobileElement
            back.click()
            waitRequiredElement("dismissTooltip", SearchType.byAccessibilityId)
            isElementPresent("dismissTooltip", SearchType.byAccessibilityId)
            val dismissTooltip = mobDriver.findElementByAccessibilityId("dismissTooltip") as MobileElement
            dismissTooltip.click()
        }
        return flag
    }

    override fun startConversation() {
        waitRequiredElement("startConversationButton", SearchType.byAccessibilityId)
        val startConversationButton = mobDriver.findElementByAccessibilityId("startConversationButton") as MobileElement
        startConversationButton.click()
    }

    override fun enterTextToPostInput(globalPostTitle: String) {
        val xPath = "messageInputTextView"
        waitRequiredElement(xPath, SearchType.byAccessibilityId)
        val convoEditBox = mobDriver.findElementByAccessibilityId(xPath) as MobileElement
        convoEditBox.sendKeys(globalPostTitle)
        waitRequiredElement("enabledSend", SearchType.byAccessibilityId)
        val postButton = mobDriver.findElementByAccessibilityId("enabledSend") as MobileElement
        postButton.click()
    }

    override fun enterTextToPostInputButNotSend(text: String){
        val xPath = "messageInputTextView"
        waitRequiredElement(xPath, SearchType.byAccessibilityId)
        val convoEditBox: MobileElement = mobDriver.findElementByAccessibilityId(xPath)
        convoEditBox.sendKeys(text)
    }

    override fun checkTextInInputPost() : String{
        val xPath = "messageInputTextView"
        waitRequiredElement(xPath, SearchType.byAccessibilityId)
        val convoEditBox: MobileElement = mobDriver.findElementByAccessibilityId(xPath)
        return convoEditBox.text
    }

    override fun returnToHomeScreenByIcon() {
        while (true) {
            if (isElementPresent("Back", SearchType.byAccessibilityId)) {
                val back = mobDriver.findElementByAccessibilityId("Back") as MobileElement
                back.click()
            }
            if (isElementPresent("Done", SearchType.byAccessibilityId)) {
                val back = mobDriver.findElementByAccessibilityId("Done") as MobileElement
                back.click()
            }
            if (isElementPresent("Close", SearchType.byAccessibilityId)) {
                val back = mobDriver.findElementByAccessibilityId("Close") as MobileElement
                back.click()
            }
            if (isElementPresent("Cancel", SearchType.byAccessibilityId)) {
                val back = mobDriver.findElementByAccessibilityId("Cancel") as MobileElement
                back.click()
            }
            if (checkScreen(Screen.HOME_SCREEN)) break
        }
    }

    override fun gotoHomeScreen() {
        while (true) {
            if (checkScreen(Screen.HOME_SCREEN)) break
            mobDriver.navigate().back()

            if (isElementPresent("Cancel", SearchType.byAccessibilityId)) {
                val back = mobDriver.findElementByAccessibilityId("Cancel") as MobileElement
                back.click()
            }
            if(waitRequiredElement("closeConversationComposerButton", SearchType.byID)) {
                val close = mobDriver.findElementByAccessibilityId("closeConversationComposerButton") as MobileElement
                close.click()
            }
        }
    }

    private fun awaitForRootCollectionForming(){
        sleep(5000)
    }


    override fun joinToPost() {
        waitRequiredElement("//XCUIElementTypeButton[@name=\"Join Conversation\"]", SearchType.byXPath)
        val joinButton = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Join Conversation\"]") as MobileElement
        joinButton.click()
    }

    override fun openPostByID(postID: String) {
        waitRequiredElement(postID, SearchType.byAccessibilityId)
        val post: MobileElement = mobDriver.findElementByAccessibilityId(postID)
        post.click()
    }

    override fun selectOptionInContextmenu(context: ContextMenu) {
        waitRequiredElement("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeCollectionView/XCUIElementTypeCell[2]/XCUIElementTypeOther/XCUIElementTypeOther[1]/XCUIElementTypeOther", SearchType.byXPath)
        val contextTap = mobDriver.findElementByXPath("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeCollectionView/XCUIElementTypeCell[2]/XCUIElementTypeOther/XCUIElementTypeOther[1]/XCUIElementTypeOther") as MobileElement
        contextTap.click()
        when (context) {
            ContextMenu.MESSAGE_USER -> {
                val contextMenu = mobDriver.findElementByAccessibilityId("Message") as MobileElement
                contextMenu.click()
            }
            ContextMenu.BLOCK_USER -> {
                waitUntil("Block User", SearchType.byAccessibilityId, 5)
                var contextMenu: MobileElement = mobDriver.findElementByAccessibilityId("Block User")
                contextMenu.click()
                waitUntil("Block User", SearchType.byAccessibilityId, 5)
                contextMenu = mobDriver.findElementByAccessibilityId("Block User")
                contextMenu.click()
            }
            ContextMenu.UNBLOCK_USER -> {
                waitUntil("Unblock User", SearchType.byAccessibilityId, 5)
                var contextMenu: MobileElement = mobDriver.findElementByAccessibilityId("Unblock User")
                contextMenu.click()
            }
        }

    }

    override fun logout() {
        waitRequiredElement("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeNavigationBar/XCUIElementTypeButton", SearchType.byXPath)
        val profileButton = mobDriver.findElementByXPath("//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeNavigationBar/XCUIElementTypeButton") as MobileElement
        profileButton.click()
        val logOutButton : MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeStaticText[@name=\"Sign out\"]")
        logOutButton.click()
        waitUntil("Sign out", SearchType.byAccessibilityId, 5)
        val exitDialogButton: MobileElement = mobDriver.findElementByAccessibilityId("Sign out")
        exitDialogButton.click()
    }

    override fun createSharePost(): String {
        passStartScreen()
        setPhone(user.phone)
        setPinCode(user.pinCode)
        passOnboarding("defaultConversationPrompt")
        val tempPostTitle = "An autotest SHARE post №" + utills.randomValue(3).toString()
        val messageInputTextView: MobileElement = mobDriver.findElementByAccessibilityId("messageInputTextView")
        messageInputTextView.sendKeys(tempPostTitle)
        val share: MobileElement = mobDriver.findElementByXPath("//XCUIElementTypeButton[@name=\"Share\"]")
        share.click()
        sleep(1000)
        return tempPostTitle
    }

    override fun leaveAPost() {
        val xPath = "//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeNavigationBar/XCUIElementTypeButton[2]"
        clickUntil(xPath, SearchType.byXPath, 20)
        val el2: MobileElement = mobDriver.findElementByAccessibilityId("Leave Conversation")
        el2.click()
    }

    override fun navigateFromDMChatToParent(): String {
        val xPath = "//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeCollectionView/XCUIElementTypeOther[1]/XCUIElementTypeOther"
        waitRequiredElement(xPath, SearchType.byXPath)
        val link = mobDriver.findElementByXPath(xPath) as MobileElement
        val linkName = link.findElementByClassName("XCUIElementTypeStaticText")
        val postTitle = linkName.text
        link.click()
        return postTitle
    }

    override fun clickToMoreButton() {
        val xPath = "//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeNavigationBar/XCUIElementTypeButton[2]"
        waitRequiredElement(xPath, SearchType.byXPath)
        val moreButton = mobDriver.findElementByXPath(xPath) as MobileElement
        moreButton.click()
    }

    override fun ignoreConversation() {
        waitUntil("Ignore Conversation", SearchType.byAccessibilityId, 5)
        val ignoreConversationButton = mobDriver.findElementByAccessibilityId("Ignore Conversation") as MobileElement
        ignoreConversationButton.click()
    }

    override fun replyToDM() {
        val xPath = "//XCUIElementTypeStaticText[@name=\"Reply\"]"
        waitRequiredElement(xPath, SearchType.byXPath)
        val replyButton = mobDriver.findElementByXPath(xPath) as MobileElement
        replyButton.click()
    }

    override fun checkBadge(): Boolean {
        val shotUserName = user.nicName.split(" ").toTypedArray()[0].substring(0, 1) + user.nicName.split(" ").toTypedArray()[1].substring(0, 1)
        return waitRequiredElement(shotUserName.toUpperCase(), SearchType.byAccessibilityId)
    }

    override fun checkFriendsBadge(friendsName: String): Boolean {
        val shotUserName = friendsName.split(" ").toTypedArray()[0].substring(0, 1) + friendsName.split(" ").toTypedArray()[1].substring(0, 1)
        return waitRequiredElement(shotUserName.toUpperCase(), SearchType.byAccessibilityId)
    }

    override fun checkUserName(): String {
        val xpath = "//XCUIElementTypeApplication[@name=\"Open\"]/XCUIElementTypeWindow[2]/XCUIElementTypeOther/XCUIElementTypeOther[2]/XCUIElementTypeOther/XCUIElementTypeOther[3]/XCUIElementTypeOther"
        val textBox0 = mobDriver.findElementByXPath(xpath) as MobileElement
        val textField = textBox0.findElement(By.className("XCUIElementTypeStaticText"))
        val userName = textField.text
        val a = userName.split(" ").toTypedArray()
        return a[2] + " " + a[3].replace("...", "")
    }

    override fun closeWelcomeTooltip() {
        if (isElementPresent("dismissTooltip", SearchType.byAccessibilityId)) {
            val dismissTooltip = mobDriver.findElementByAccessibilityId("dismissTooltip") as MobileElement
            dismissTooltip.click()
        }
    }

    override fun checkPostTitleFromPost(postTitle: String): Boolean {
        waitRequiredElement("XCUIElementTypeNavigationBar", SearchType.byClassName)
        val elem : MobileElement = mobDriver.findElementByClassName("XCUIElementTypeNavigationBar")
        val col = elem.findElementsByClassName("XCUIElementTypeStaticText")
        for (e in col)
            if (e.text.equals(postTitle))
                return true

        return false
    }

    override fun reportToMessage() {
        waitRequiredElement("XCUIElementTypeCollectionView", SearchType.byClassName)
        //val cell: MobileElement = mobDriver.findElementByClassName("XCUIElementTypeCollectionView")
        //val c = cell.findElementsByClassName("XCUIElementTypeCell")
        sleep(5000)
        val text = (mobDriver
                .findElementsByClassName("XCUIElementTypeCell") as List<MobileElement>)[1]
                .findElementByClassName("XCUIElementTypeTextView")

        PlatformTouchAction(mobDriver)
                .press(ElementOption.element(text))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
                .release().perform()

        val report: MobileElement = mobDriver.findElementByAccessibilityId("Report Message")
        report.click()
        sleep(2000)
        val reason = mobDriver.findElementByAccessibilityId("Mean")
        reason.click()
        //val list: List<MobileElement> = mobDriver.findElementsByClassName("XCUIElementTypeCell")
        //list[2].click()
    }

    override fun checkTextFromAlertBar(): String {
        var text  = ""
        if (waitUntil("Dismiss", SearchType.byAccessibilityId, 5)) {
            val e: MobileElement = mobDriver.findElementByAccessibilityId("Thank you")
            text = e.text
            val dismiss: MobileElement = mobDriver.findElementByAccessibilityId("Dismiss")
            dismiss.click()
        } else {
            //XCUIElementTypeAlert[@name="Error"]
            val texts: List<MobileElement> = mobDriver.findElementsByClassName("XCUIElementTypeStaticText")
            text = texts[1].text
            if (isElementPresent("Ok", SearchType.byAccessibilityId)) {
                val ok: MobileElement = mobDriver.findElementByAccessibilityId("Ok")
                ok.click()
            }

        }
        return text
    }

    override fun checkTextFromLastComment(): String {
        waitRequiredElement("XCUIElementTypeCollectionView", SearchType.byClassName)
        val cell: MobileElement = mobDriver.findElementByClassName("XCUIElementTypeCollectionView")
        val c = cell.findElementsByClassName("XCUIElementTypeCell")
        sleep(2000)
        return c[c.size - 1].text
    }

    override fun deleteOwnMessage() {
        waitRequiredElement("XCUIElementTypeCollectionView", SearchType.byClassName)
        val cell: MobileElement = mobDriver.findElementByClassName("XCUIElementTypeCollectionView")
        val c = cell.findElementsByClassName("XCUIElementTypeCell")
        val context = c[c.size - 1]

        PlatformTouchAction(mobDriver)
                .press(ElementOption.element(context))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
                .release().perform()

        waitRequiredElement("Delete Message", SearchType.byAccessibilityId)
        val e: MobileElement = mobDriver.findElementByAccessibilityId("Delete Message")
        e.click()
    }

    override fun checkErrors(): String {
        waitRequiredElement("XCUIElementTypeStaticText", SearchType.byClassName)
        val list = mobDriver.findElementsByClassName("XCUIElementTypeStaticText") as List<MobileElement>
        for (e in list) {
            if (e.text.contains("incorrect")) {
                return e.text
            }
        }
        return ""
    }

    override fun checkHiddenApp(): Boolean {
        return isElementPresent("openlogo", SearchType.byAccessibilityId)
    }

    override fun navigateBack() {
        mobDriver.navigate().back()
    }


    override fun checkSuspensionWarning(): Boolean{
        if (waitUntil("//XCUIElementTypeStaticText[@name=\"I understand\"]", SearchType.byXPath, 10)){
            mobDriver.findElementByXPath("//XCUIElementTypeStaticText[@name=\"I understand\"]").click()
            return true
        }

        return false
    }

    override fun checkSuspenseReasonFromComment(text: String, reason: String): String{
        if (waitRequiredElement("//XCUIElementTypeStaticText[@name=\"$reason\"]", SearchType.byXPath)){
            return reason
        }
        return ""
    }

    override fun checkSuspensionStatus(): String{
        val suspendMessage = "Your account has been suspended for violating Open's Community Guidelines."
        val xPath = "//XCUIElementTypeStaticText[@name=\"$suspendMessage\"]"
        if (waitUntil(xPath, SearchType.byXPath, 10))
            return suspendMessage

        return ""
    }

    override fun tryToEnterTextToPostInput(): String{
        val xPath = "messageInputTextView"
        try {
            if (isElementPresent(xPath, SearchType.byXPath)) {
                val convoEditBox = mobDriver.findElementByAccessibilityId(xPath)
                convoEditBox.sendKeys("test")
            }

            if (isElementPresent("enabledSend", SearchType.byAccessibilityId)) {
                val postButton: MobileElement = mobDriver.findElementByAccessibilityId("enabledSend")
                postButton.click()
                return "messageInputTextView and enabledSend are visible"
            }

        } catch (e: Exception) {
            return e.toString()
        }

        return ""
    }


    override fun checkAlert() : Boolean{
        if(waitRequiredElement("//XCUIElementTypeAlert[@name=\"Unable to send message\"]", SearchType.byXPath))
            return true

        return false
    }

    override fun cancelDeleteOwnMessage() {

    }

    override fun deleteFriendMessage(): Boolean {
        waitRequiredElement("XCUIElementTypeCollectionView", SearchType.byClassName)
        mobDriver.findElementByClassName("XCUIElementTypeCollectionView")
        sleep(5000)
        val text = (mobDriver
                .findElementsByClassName("XCUIElementTypeCell") as List<MobileElement>)[1]
                .findElementByClassName("XCUIElementTypeTextView")

        PlatformTouchAction(mobDriver)
                .press(ElementOption.element(text))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
                .release().perform()

        if (isElementPresent("Delete Message", SearchType.byAccessibilityId))
            return false

        return true
    }

    override fun turnOFFInternetConnection(user: User) {
        var x = 0
        var y = 0
        val shiftY: Int

        when(user.customisationiOS) {
            Customization.IPHONE_XS -> {
                x = mobDriver.manage().window().size.width - 30
                y = 40
                shiftY = mobDriver.manage().window().size.height - 100
                swipeTo(Point(x, y), Position.BOTTOM, shiftY)
            }

            Customization.IPHONE_SE -> {
                x = 10
                y = mobDriver.manage().window().size.height - 50
                shiftY = 10
                swipeTo(Point(x, y), Position.TOP, shiftY)
            }
        }

        val cell: MobileElement = mobDriver.findElementByAccessibilityId("airplane-mode-button")
        cell.click()
        sleep(2000)

        PlatformTouchAction(mobDriver)
                .press(PointOption.point(x, y))
                .release()
                .perform()
    }

    override fun hideApp(){
        mobDriver.runAppInBackground(Duration.ofSeconds(30))
    }


    override fun startToCheckNotification(){
        val p = Point(mobDriver.manage().window().size.width / 6, 40)
        swipeTo(p, Position.BOTTOM,  2 * mobDriver.manage().window().size.height / 3)
    }

    override fun showApp() {
        val y = mobDriver.manage().window().size.height - 50;
        val p = Point(mobDriver.manage().window().size.width / 2,
                      y)

        swipeTo(p, Position.TOP,  y - 40)
        //mobDriver.activateApp("com.opentechnologies.openapp-dev")
    }

    override fun closeApp() {
        mobDriver.terminateApp("com.opentechnologies.openapp-dev")
    }


}
