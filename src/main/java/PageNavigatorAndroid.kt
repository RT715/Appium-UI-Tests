import com.codeborne.selenide.Selenide.sleep
import enums.*
import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileElement
import org.openqa.selenium.By
import org.openqa.selenium.interactions.Actions
import support.CommonUtills
import support.User
import java.awt.Point
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*


class PageNavigatorAndroid (private val mobDriver: AppiumDriver<MobileElement>, private val user: User) : PageNavigatorMobile(mobDriver, user)   {
    private val utils: CommonUtills = CommonUtills()
    private val screensFromRemoteConfig: MutableList<String> = utils.parseDataFromRemoteConfigJSON()

    override fun startSession() {
        mobDriver.resetApp()
        passStartScreen()
        //passWelcomeTour()
        setPhone(user.phone)
        setPinCode(user.pinCode)
        passOnboarding("")
    }

    override fun checkScreen(screen: Screen): Boolean {
        when (screen) {
            Screen.ONBOARDING -> return waitRequiredElement("co.letsopen.open.stage:id/host_fragment", SearchType.byID)
            Screen.PHONE_SCREEN -> return waitRequiredElement("co.letsopen.open.stage:id/action_bar_root", SearchType.byID)
            Screen.HOME_SCREEN -> return waitRequiredElement("co.letsopen.open.stage:id/root", SearchType.byID)
        }
        return false
    }

    override fun checkHomeScreenTooltip(): Boolean {
        if (waitRequiredElement("co.letsopen.open.stage:id/chatCardHolder", SearchType.byID)) {
            val button: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/okButton") as MobileElement
            button.click()
            sleep(2000)
            returnToHomeScreenByIcon()
            if (isElementPresent("co.letsopen.open.stage:id/closeTooltipJoinButton", SearchType.byID)) {
                val button2: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/closeTooltipJoinButton")
                button2.click()
            }
            return true
        }
        return false
    }

    override fun closeWelcomeTooltip() {
        val flag = waitUntil("co.letsopen.open.stage:id/tooltip", SearchType.byID, 5)
        if (flag) {
            val button: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/closeTooltipButton")
            button.click()
        }
    }

    override fun checkPostsFromHomeScreen(globalPostTitle: String): Boolean {
        findPost(globalPostTitle)

        return true
    }

    override fun findPostId(globalPostTitle: String): String {
        val post = findPost(globalPostTitle)

        return post!!.getAttribute("content-desc")
    }


    override fun checkPostSubTitleOnHomeScreen(message: String): String {
        val post = findPost(message)

        return post!!.findElementById("co.letsopen.open.stage:id/textSecondary").text
    }

    override fun checkPostUnreadBadges(globalPostTitle: String): String {
        val post = findPost(globalPostTitle)

        return post!!.findElement(By.id("co.letsopen.open.stage:id/indicatorText")).text
    }

    private fun findPost(globalPostTitle: String): MobileElement? {
        awaitForRootCollectionForming()
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(60)
        var duration = Duration.between(now, dtOnFuture)
        while(!duration.isNegative) {
            val posts = awaitForCollectionForming("co.letsopen.open.stage:id/conversationRoot")
            for (post in posts) {
                if (!isElementPresent(post, "co.letsopen.open.stage:id/textSecondary", SearchType.byID))
                    continue

                try {
                    val postTitle = post.findElementById("co.letsopen.open.stage:id/textSecondary").text
                    if (globalPostTitle.contains(postTitle))
                        return post
                }
                catch (e: Exception) {
                    continue
                }
            }

            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }

        return null
    }

    override fun checkAbsentenseOfAwaitingPost(globalPostTitle: String) : String{
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(30)
        var duration = Duration.between(now, dtOnFuture)
        while (!duration.isNegative) {
            val posts = awaitForCollectionForming("co.letsopen.open.stage:id/conversationRoot")
            for (post in posts) {
                if (!isElementPresent(post, "co.letsopen.open.stage:id/textSecondary", SearchType.byID))
                    continue

                val postTitle = post.findElementById("co.letsopen.open.stage:id/textSecondary").text
                if (globalPostTitle.contains(postTitle)) {
                    return globalPostTitle
                }
            }

            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }
        return ""
    }

    private fun awaitForCollectionForming(query: String) : List<MobileElement>  {
        var resultCollection : List<MobileElement>
        waitUntil(query, SearchType.byID, 5)
        while(true) {
            resultCollection = mobDriver.findElementsById(query)
            val length = resultCollection.size;
            sleep(1000)
            resultCollection = mobDriver.findElementsById(query)
            val lenght2 = resultCollection.size;

            if (length == lenght2 && lenght2 != 0)
                break
        }

        var l = resultCollection.size
        if (l > 5) l = 5
        return resultCollection.subList(0, l)
    }

    override fun startConversation() {
        passFirstConversationGuide()

        waitRequiredElement("co.letsopen.open.stage:id/fab", SearchType.byID)
        val startConvButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/fab")
        startConvButton.click()
    }

    override fun enterTextToPostInput(globalPostTitle: String) {
        waitRequiredElement("co.letsopen.open.stage:id/enterCommentView", SearchType.byID)
        val convoEditBox: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/enterCommentView")
        convoEditBox.sendKeys(globalPostTitle)
        val postButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/sendCommentButton")
        postButton.click()
    }

    override fun enterTextToPostInputButNotSend(text: String) {
        waitRequiredElement("co.letsopen.open.stage:id/enterCommentView", SearchType.byID)
        val convoEditBox: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/enterCommentView")
        convoEditBox.sendKeys(text)
    }

    override fun tryToEnterTextToPostInput(): String {
        try {
            if (isElementPresent("co.letsopen.open.stage:id/enterCommentView", SearchType.byID)) {
                val convoEditBox: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/enterCommentView")
                convoEditBox.sendKeys("test")
            }

            if (isElementPresent("co.letsopen.open.stage:id/sendCommentButton", SearchType.byID)) {
                val postButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/sendCommentButton")
                postButton.click()
                return "enterCommentView and sendCommentButton are visible"
            }

        } catch (e: Exception) {
            return e.toString()
        }

        return ""
    }

    override fun returnToHomeScreenByIcon() {
        waitRequiredElement("co.letsopen.open.stage:id/toolbar", SearchType.byID)
        val toolBar: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/toolbar")
        waitUntil(toolBar, "android.widget.ImageButton", SearchType.byClassName, 5);
        val returnToHomeScreenButton = toolBar.findElement(By.className("android.widget.ImageButton"))
        returnToHomeScreenButton.click()
    }

    override fun passStartScreen() {
        waitRequiredElement("co.letsopen.open.stage:id/getStartedButton", SearchType.byID)
        val start: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/getStartedButton")
        start.click()
    }

    override fun passWelcomeTour() {
        waitRequiredElement("co.letsopen.open.stage:id/nextButton", SearchType.byID)
        val nextButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/nextButton")
        for (button in 0..2)
            nextButton.click()
    }

    override fun passWelcomeTourBySwipe() {
        waitRequiredElement("co.letsopen.open.stage:id/controlsView", SearchType.byID)
        val element = mobDriver.findElementById("co.letsopen.open.stage:id/controlsView")

        swipeTo(element, Position.LEFT, -300)      // From right to left
        swipeTo(element, Position.RIGHT, +300)      // From left to right

        for (swipe in 0..2)
            swipeTo(element, Position.LEFT, -300)

    }

    override fun setPhone(phones: String) {
        val phoneElement = phones.split(" ").toTypedArray()
        val area = phoneElement[0]
        val phone = phoneElement[1]

        waitRequiredElement("co.letsopen.open.stage:id/countryListSpinner", SearchType.byID)
        val countryListSpinner: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/countryListSpinner")
        countryListSpinner.click()
        waitRequiredElement(
            "/hierarchy/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.ListView/android.widget.TextView",
            SearchType.byXPath
        )
        val areas =
            mobDriver.findElements(By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.ListView/android.widget.TextView")) as List<MobileElement>
        for (a in areas) {
            if (a.text.contains(area)) {
                a.click()
                break
            }
        }
        waitRequiredElement("co.letsopen.open.stage:id/numberInput", SearchType.byID)
        val phoneInput: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/numberInput")
        phoneInput.sendKeys(phone)
        val nextButton: MobileElement = if (isElementPresent("co.letsopen.open.stage:id/nextButton", SearchType.byID))
            mobDriver.findElementById("co.letsopen.open.stage:id/nextButton")
        else
            mobDriver.findElementById("co.letsopen.open.stage:id/nextButtonV2")
        nextButton.click()
    }

    override fun setPinCode(pin: String) {
        waitRequiredElement("co.letsopen.open.stage:id/digit1", SearchType.byID)
        for (i in 1..6) {
            val e: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/digit$i")
            e.sendKeys(pin.split(" ").toTypedArray()[i - 1])
        }
    }

    override fun passOnboarding(stopScreen: String): String {
        var incorrectScreen = ""
        checkScreen(Screen.ONBOARDING)

        for (s in screensFromRemoteConfig) {
            val screenElements = s.split("@")
            val screen = screenElements[0]
            val headerText = screenElements[1]
            val buttonText = screenElements[2]

            if (screen == stopScreen)
                break

            var mobileHeader: MobileElement?
            var mobileButton: MobileElement? = null
            var tempHeader: String

            when (screen) {
                "Rules" -> {
                    if (waitRequiredElement("co.letsopen.open.stage:id/primaryText", SearchType.byID)) {
                        mobileHeader = mobDriver.findElementById("co.letsopen.open.stage:id/primaryText")
                        tempHeader = mobileHeader.text

                        if (mobileHeader == null) {
                            incorrectScreen = "$incorrectScreen  Absent screen: $screen; text: $headerText\r\n"
                            continue

                        } else if (tempHeader.trim { it <= ' ' } != headerText.trim { it <= ' ' }) {
                            incorrectScreen = "$incorrectScreen  Incorrect screen: $screen; text: $headerText\r\n"
                        }
                        if (isElementPresent("co.letsopen.open.stage:id/iAgreeButton", SearchType.byID))
                            mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/iAgreeButton")
                        else if (isElementPresent("co.letsopen.open.stage:id/iAgreeButtonV2", SearchType.byID))
                            mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/iAgreeButtonV2")

                        mobileButton!!.click()
                    }
                }

                "Age" -> try {
                    if (waitUntil("co.letsopen.open.stage:id/ageInput", SearchType.byID, 5)) {
                        val ageInput: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/ageInput")
                        ageInput.sendKeys("18")
                        navigateBack()

                        when {
                            isElementPresent("co.letsopen.open.stage:id/button_done", SearchType.byID) ->
                                mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/button_done")
                            isElementPresent("co.letsopen.open.stage:id/button_next_v2", SearchType.byID) ->
                                mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/button_next_v2")
                        }

                        mobileButton!!.click()
                    }
                } catch (e: Exception) {
                }

                "defaultConversationPrompt" -> {
                    if (waitUntil("co.letsopen.open.stage:id/headerTextView", SearchType.byID, 10)) {
                        mobileHeader = mobDriver.findElementById("co.letsopen.open.stage:id/headerTextView")
                        tempHeader = mobileHeader.text

                        if (mobileHeader == null) {
                            incorrectScreen = "$incorrectScreen  Absent screen: $screen; text: $headerText\r\n"
                            continue

                        } else if (tempHeader.trim { it <= ' ' } != headerText.trim { it <= ' ' }) {
                            incorrectScreen = "$incorrectScreen  Incorrect screen: $screen; text: $headerText\r\n"
                        }

                        if (isElementPresent("co.letsopen.open.stage:id/skipButton", SearchType.byID)) {
                            mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/skipButton")
                            if (mobileButton.text != buttonText)
                                incorrectScreen += "Incorrect button name: $screen; text: ${mobileButton.text}\n" // Edit everywhere
                        }
                        mobileButton?.click()
                    }
                }

                "defaultAllowNotifications" -> continue

                "defaultConnectContacts" -> {
                    if (waitUntil("co.letsopen.open.stage:id/header", SearchType.byID, 5)) {
                        mobileHeader = mobDriver.findElementById("co.letsopen.open.stage:id/header")
                        tempHeader = mobileHeader.text

                        if (mobileHeader == null) {
                            incorrectScreen = "$incorrectScreen   Absent screen: $screen; text: $headerText\r\n"
                            continue
                        } else if (tempHeader.trim { it <= ' ' } != headerText.trim { it <= ' ' }) {
                            incorrectScreen = "$incorrectScreen  Incorrect screen: $screen; text: $headerText\r\n"
                        }

                        if (!isElementPresent("co.letsopen.open.stage:id/handIcon", SearchType.byID))
                            incorrectScreen = "$incorrectScreen HandIcon is absent is the $screen"

                        mobileButton = when {
                            isElementPresent("co.letsopen.open.stage:id/mainButton", SearchType.byID)
                            -> mobDriver.findElementById("co.letsopen.open.stage:id/mainButton")
                            isElementPresent("co.letsopen.open.stage:id/allowButton", SearchType.byID)
                            -> mobDriver.findElementById("co.letsopen.open.stage:id/allowButton")
                            isElementPresent("co.letsopen.open.stage:id/connectContactsButton", SearchType.byID)
                            -> mobDriver.findElementById("co.letsopen.open.stage:id/connectContactsButton")
                            isElementPresent("co.letsopen.open.stage:id/connectContactsButtonV2", SearchType.byID)
                            -> mobDriver.findElementById("co.letsopen.open.stage:id/connectContactsButtonV2")
                            else -> null
                        }

                        mobileButton?.click() ?: throw Exception("mobileButton is not found for the screen: $screen")

                        val id: String = when (user.customisationAndroid) {
                            Customization.NOKIA -> "com.android.permissioncontroller:id/permission_allow_button"
                            Customization.SAMSUNG -> "com.android.packageinstaller:id/permission_allow_button"
                            else -> ""
                        }

                        if (waitRequiredElement(id, SearchType.byID)) {
                            val button: MobileElement = mobDriver.findElementById(id)
                            button.click()
                        }
                    }
                }

                "defaultInvites" -> {
                    if (waitUntil("co.letsopen.open.stage:id/header", SearchType.byID, 5)) {
                        mobileHeader = mobDriver.findElementById("co.letsopen.open.stage:id/header")
                        tempHeader = mobileHeader.text

                        if (mobileHeader == null) {
                            incorrectScreen = "$incorrectScreen  Absent screen: $screen; text: $headerText\r\n"
                            continue
                        } else if (tempHeader.trim { it <= ' ' } != headerText.trim { it <= ' ' })
                            incorrectScreen = "$incorrectScreen  Incorrect screen: $screen; text: $headerText\r\n"

                        if (!isElementPresent("co.letsopen.open.stage:id/handIcon", SearchType.byID))
                            incorrectScreen = "$incorrectScreen  HandIcon is absent is the $screen"


                        when {
                            isElementPresent("co.letsopen.open.stage:id/allowButton", SearchType.byID) ->
                                mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/allowButton")
                            isElementPresent("co.letsopen.open.stage:id/inviteAllButtonV2", SearchType.byID) ->
                                mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/inviteAllButtonV2")
                            isElementPresent("co.letsopen.open.stage:id/mainButton", SearchType.byID) ->
                                mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/mainButton")
                        }

                        mobileButton!!.click()

                        sleep(1000)
                        // Verify if dialog screen appeared
                        if (isElementPresent("co.letsopen.open.stage:id/parentPanel", SearchType.byID)) {
                            val verifyButton = when {
                                isElementPresent(
                                    "co.letsopen.open.stage:id/allowButton",
                                    SearchType.byID
                                ) -> mobDriver.findElementById("co.letsopen.open.stage:id/allowButton")
                                isElementPresent(
                                    "android:id/button1",
                                    SearchType.byID
                                ) -> mobDriver.findElementById("android:id/button1")
                                else -> null
                            }

                            verifyButton!!.click()
                        }

                    }
                }
            }
            sleep(500)
        }
        return incorrectScreen
    }

    override fun joinToPost() {
        waitRequiredElement("co.letsopen.open.stage:id/joinButton", SearchType.byID)
        val joinButton = mobDriver.findElementById("co.letsopen.open.stage:id/joinButton") as MobileElement
        joinButton.click()
    }

    override fun replyToDM() {
        waitRequiredElement("co.letsopen.open.stage:id/replyButton", SearchType.byID)
        val replyButton = mobDriver.findElementById("co.letsopen.open.stage:id/replyButton") as MobileElement
        replyButton.click()
    }

    override fun clickToMoreButton() {
        waitRequiredElement("co.letsopen.open.stage:id/moreButton", SearchType.byID)
        val moreButton = mobDriver.findElementById("co.letsopen.open.stage:id/moreButton") as MobileElement
        moreButton.click()
    }

    override fun ignoreConversation() {
        waitRequiredElement("co.letsopen.open.stage:id/ignoreConversationButton", SearchType.byID)
        val ignoreConversationButton: MobileElement =
            mobDriver.findElementById("co.letsopen.open.stage:id/ignoreConversationButton")
        ignoreConversationButton.click()
    }


    override fun openPostByID(postID: String) {
        awaitForRootCollectionForming()
        val post: MobileElement = mobDriver.findElementByXPath("//android.widget.LinearLayout[@content-desc=\"$postID\"]/android.view.ViewGroup/android.widget.LinearLayout/android.widget.TextView[1]")
        post.click()
    }

    override fun selectOptionInContextmenu(context: ContextMenu) {
        waitRequiredElement("co.letsopen.open.stage:id/avatar", SearchType.byID)
        val avatar: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/avatar")
        avatar.click()
        sleep(500)
        var messageButton: MobileElement
        when (context) {
            ContextMenu.MESSAGE_USER -> {
                messageButton = mobDriver.findElementById("co.letsopen.open.stage:id/messageButton")
                messageButton.click()
            }
            ContextMenu.BLOCK_USER -> {
                waitUntil("co.letsopen.open.stage:id/blockUserButton", SearchType.byID, 5)
                messageButton = mobDriver.findElementById("co.letsopen.open.stage:id/blockUserButton")
                messageButton.click()
                waitRequiredElement("android:id/button1", SearchType.byID)
                val approve = mobDriver.findElementById("android:id/button1") as MobileElement
                approve.click()
            }
            ContextMenu.UNBLOCK_USER -> {
                waitUntil("co.letsopen.open.stage:id/blockUserButton", SearchType.byID, 5)
                messageButton = mobDriver.findElementById("co.letsopen.open.stage:id/blockUserButton")
                messageButton.click()
            }
        }
    }

    override fun checkPostSubTitle(): String {
        return ""
    }

    override fun logout() {
        waitRequiredElement("co.letsopen.open.stage:id/profileButton", SearchType.byID)
        val profileButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/profileButton")
        profileButton.click()
        val logOutButton: MobileElement =
            mobDriver.findElementByXPath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.RelativeLayout/android.view.ViewGroup/android.widget.FrameLayout/android.view.ViewGroup/android.widget.RelativeLayout/android.widget.LinearLayout/androidx.recyclerview.widget.RecyclerView/android.widget.FrameLayout[10]/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView")
        logOutButton.click()
        waitUntil("android:id/button1", SearchType.byID, 5)
        val exitDialogButton: MobileElement = mobDriver.findElementById("android:id/button1")
        exitDialogButton.click()
    }

    override fun checkUserName(): String {
        val textField = mobDriver.findElementById("co.letsopen.open.stage:id/enterCommentView") as MobileElement
        val userName = textField.text
        val a = userName.split(" ").toTypedArray()
        return a[2] + " " + a[3].replace("...", "")
    }

    override fun checkBadge(): Boolean {
        waitRequiredElement("co.letsopen.open.stage:id/avatarView", SearchType.byID)
        val rootAvatar = mobDriver.findElementById("co.letsopen.open.stage:id/avatarView") as MobileElement
        val textField = rootAvatar.findElementById("co.letsopen.open.stage:id/userNameView")
        val badgetext = textField.text
        val shotUserName = user.nicName.split(" ").toTypedArray()[0].substring(0, 1) + user.nicName.split(" ")
            .toTypedArray()[1].substring(0, 1)
        return shotUserName == badgetext.toLowerCase()
    }

    override fun checkFriendsBadge(friendsName: String): Boolean {
        waitRequiredElement("co.letsopen.open.stage:id/avatar", SearchType.byID)
        val rootAvatar = mobDriver.findElementById("co.letsopen.open.stage:id/avatar") as MobileElement
        val textField = rootAvatar.findElementById("co.letsopen.open.stage:id/userNameView")
        val badgetext = textField.text
        val shotUserName = friendsName.split(" ").toTypedArray()[0].substring(0, 1) + friendsName.split(" ")
            .toTypedArray()[1].substring(0, 1)
        return shotUserName == badgetext.toLowerCase()
    }

    override fun createSharePost(): String {
        passStartScreen()
        //passWelcomeTour()
        setPhone(user.phone)
        setPinCode(user.pinCode)
        passOnboarding("defaultConversationPrompt")

        val tempPostTitle = "An autotest SHARE post №" + utils.randomValue(3).toString()
        waitRequiredElement("co.letsopen.open.stage:id/postEditView", SearchType.byID)
        val postTitle: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/postEditView")
        postTitle.sendKeys(tempPostTitle)
        navigateBack()
        var mobileButton: MobileElement? = null
        if (isElementPresent("co.letsopen.open.stage:id/shareButton", SearchType.byID))
            mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/shareButton")
        else if (isElementPresent("co.letsopen.open.stage:id/shareButtonV2", SearchType.byID))
            mobileButton = mobDriver.findElementById("co.letsopen.open.stage:id/shareButtonV2")
        mobileButton!!.click()
        sleep(1000)
        return tempPostTitle
    }

    override fun leaveAPost() {
        waitRequiredElement("co.letsopen.open.stage:id/moreButton", SearchType.byID)
        val moreButton = mobDriver.findElementById("co.letsopen.open.stage:id/moreButton") as MobileElement
        moreButton.click()
        waitRequiredElement("co.letsopen.open.stage:id/joinAndLeaveButton", SearchType.byID)
        val joinAndLeaveButton =
            mobDriver.findElementById("co.letsopen.open.stage:id/joinAndLeaveButton") as MobileElement
        joinAndLeaveButton.click()
    }

    override fun navigateFromDMChatToParent(): String {
        waitRequiredElement("co.letsopen.open.stage:id/parentChatTitleView", SearchType.byID)
        val parentChatTitleView =
            mobDriver.findElementById("co.letsopen.open.stage:id/parentChatTitleView") as MobileElement
        val postTitle = parentChatTitleView.text
        parentChatTitleView.click()
        return postTitle
    }

    override fun checkPostTitleFromPost(postTitle: String): Boolean {
        waitRequiredElement("co.letsopen.open.stage:id/toolbarTitle", SearchType.byID)
        val toolbarTitle = mobDriver.findElementById("co.letsopen.open.stage:id/toolbarTitle")
        if (toolbarTitle.text.equals(postTitle))
            return true

        return false
    }

    override fun gotoHomeScreen() {
        while (true) {
            sleep(2000)
            if (isElementPresent("co.letsopen.open.stage:id/profileButton", SearchType.byID))
                break
            else
                navigateBack()
        }
    }

    override fun unblockUserFromAccountScreen() {
        waitRequiredElement("co.letsopen.open.stage:id/profileButton", SearchType.byID)
        val profileButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/profileButton")
        profileButton.click()
        val xpath =
            "/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.RelativeLayout/android.view.ViewGroup/android.widget.FrameLayout/android.view.ViewGroup/android.widget.RelativeLayout/android.widget.LinearLayout/androidx.recyclerview.widget.RecyclerView/android.widget.FrameLayout[7]/android.widget.LinearLayout"
        waitRequiredElement(xpath, SearchType.byXPath)
        val frame: MobileElement = mobDriver.findElementByXPath(xpath)
        frame.click()
        waitRequiredElement("co.letsopen.open.stage:id/unblockButton", SearchType.byID)
        val unblockButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/unblockButton")
        unblockButton.click()
    }

    override fun reportToMessage() {
        waitRequiredElement("co.letsopen.open.stage:id/commentNotMy", SearchType.byID)
        val commentNotMy: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/commentNotMy")
        val action = Actions(mobDriver)
        action.clickAndHold(commentNotMy)
        action.perform()
        waitRequiredElement("co.letsopen.open.stage:id/reportButton", SearchType.byID)
        val reportButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/reportButton")
        reportButton.click()
        waitRequiredElement("co.letsopen.open.stage:id/title", SearchType.byID)
        val reports: List<MobileElement> = mobDriver.findElementsById("co.letsopen.open.stage:id/title")
        reports[Random().nextInt(2)].click()
    }

    override fun deleteFriendMessage(): Boolean {
        waitRequiredElement("co.letsopen.open.stage:id/commentNotMy", SearchType.byID)
        val commentNotMy: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/commentNotMy")
        val action = Actions(mobDriver)
        action.clickAndHold(commentNotMy)
        action.perform()
        waitRequiredElement("co.letsopen.open.stage:id/actionsList", SearchType.byID)
        val textView: List<MobileElement> = mobDriver.findElementById("co.letsopen.open.stage:id/actionsList")
            .findElementsByClassName("android.widget.TextView")
        for (text in textView) {
            if (text.text.toLowerCase().contains("delete"))
                return true
        }

        return false
    }

    override fun checkTextFromAlertBar(): String {
        if (waitRequiredElement("co.letsopen.open.stage:id/snackbar_text", SearchType.byID)) {
            val e = mobDriver.findElementById("co.letsopen.open.stage:id/snackbar_text") as MobileElement
            return e.text
        }
        return "No snackbar enabled"
    }

    override fun checkTextFromLastComment(): String {
        waitRequiredElement("co.letsopen.open.stage:id/commentMy", SearchType.byID)
        val commentNotMy: List<MobileElement> = mobDriver.findElementsById("co.letsopen.open.stage:id/commentMy")
        val e = commentNotMy[commentNotMy.size - 1]
        return e.findElement(By.className("android.widget.TextView")).text
    }

    override fun deleteOwnMessage() {
        waitRequiredElement("co.letsopen.open.stage:id/commentMy", SearchType.byID)
        val commentNotMy: List<MobileElement> = mobDriver.findElementsById("co.letsopen.open.stage:id/commentMy")
        val e = commentNotMy[commentNotMy.size - 1]
        val action = Actions(mobDriver)
        action.clickAndHold(e)
        action.perform()
        waitRequiredElement("co.letsopen.open.stage:id/deleteButton", SearchType.byID)
        val deleteButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/deleteButton")
        deleteButton.click()
        val button1: MobileElement = mobDriver.findElementById("android:id/button1")
        button1.click()
    }

    override fun cancelDeleteOwnMessage() {
        waitRequiredElement("co.letsopen.open.stage:id/commentMy", SearchType.byID)
        val commentNotMy: List<MobileElement> = mobDriver.findElementsById("co.letsopen.open.stage:id/commentMy")
        val e = commentNotMy[commentNotMy.size - 1]
        val action = Actions(mobDriver)
        action.clickAndHold(e)
        action.perform()
        val deleteButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/deleteButton")
        deleteButton.click()
        val button1: MobileElement = mobDriver.findElementById("android:id/button2")
        button1.click()
    }

    override fun checkErrors(): String {
        return try {
            val errorText = mobDriver.findElementById("co.letsopen.open.stage:id/errorText") as MobileElement
            errorText.text
        } catch (e: Exception) {
            "No error was found"
        }
    }

    override fun checkHiddenApp(): Boolean {
        val errorText = mobDriver.findElementsById("co.letsopen.open.stage:id/getStartedButton") as List<MobileElement>
        return errorText.isEmpty()
    }

    override fun navigateBack() {
        mobDriver.navigate().back()
    }

    private fun awaitForRootCollectionForming() {
        passFirstConversationGuide()
        passSevenDaysGuide()

        var updated = "update in progress"
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(60)
        var duration = Duration.between(now, dtOnFuture)
        while (updated == "update in progress" && !duration.isNegative) {
            sleep(1000)
            if (isElementPresent("co.letsopen.open.stage:id/list", SearchType.byID)) {
                val postUpdate: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/list")
                if (postUpdate.getAttribute("content-desc") == null)
                    continue
                updated = postUpdate.getAttribute("content-desc")
            }
            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }
    }

    override fun checkSuspensionWarning(): Boolean {
        if (!waitRequiredElement("co.letsopen.open.stage:id/iUnderstandButton", SearchType.byID))
            return false

        val warningButton: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/iUnderstandButton")
        warningButton.click()
        return true
    }

    override fun checkSuspenseReasonFromComment(text: String, reason: String) : String {
        sleep(2000)
        val comments : List<MobileElement> = mobDriver.findElementsById("co.letsopen.open.stage:id/commentMy")
        for (comment in comments) {

            val textViews: List<MobileElement> = comment.findElementsByClassName("android.widget.TextView")
            for (textView in textViews) {
                if (textView.text.trim() == text) {
                    if (!isElementPresent(comment, "co.letsopen.open.stage:id/additional_text", SearchType.byID))
                        return ""

                    val suspendReason: MobileElement =
                        comment.findElementById("co.letsopen.open.stage:id/additional_text")
                    return suspendReason.text
                } else
                    continue
            }
        }

        return ""
    }

    override fun checkSuspensionStatus(): String {
        if (waitRequiredElement("co.letsopen.open.stage:id/suspendTimeCounter", SearchType.byID)) {
            val status: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/suspendTimeCounter")
            return status.text
        }

        return ""
    }

    override fun checkAlert(): Boolean {
        val b = isElementPresent("co.letsopen.open.stage:id/action_bar_root", SearchType.byID)
        val e: MobileElement = mobDriver.findElementById("android:id/button1")
        e.click()
        return b
    }

    override fun turnOFFInternetConnection(user: User) {
        val shiftY = mobDriver.manage().window().size.height - 50
        swipeTo(Point(10, 10), Position.BOTTOM, shiftY)
        val swithches: List<MobileElement> = mobDriver.findElementById("com.android.systemui:id/quick_qs_panel")
            .findElementsByClassName("android.widget.Switch")

        when (user.customisationAndroid) {
            Customization.NOKIA -> {
                for (s in swithches) {
                    if (s.getAttribute("content-desc").contains("Wi-Fi"))
                        s.click()
                    else if (s.getAttribute("content-desc").contains("Моб. Интернет"))
                        s.click()
                }
            }

            Customization.SAMSUNG -> {
                val flight: MobileElement = mobDriver.findElementByAccessibilityId("Авиа,режим,Отключено,Кнопка")
                flight.click()
            }
        }
        swipeTo(Point(10, -shiftY), Position.TOP, 10)
    }

    override fun checkInternetConnectionFromPost(): String {
        val message: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/noConnectionText")
        return message.text
    }

    override fun checkTextInInputPost(): String {
        waitRequiredElement("co.letsopen.open.stage:id/enterCommentView", SearchType.byID)
        val convoEditBox: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/enterCommentView")
        return convoEditBox.text
    }

    override fun clearPinCode() {
        waitRequiredElement("co.letsopen.open.stage:id/digit1", SearchType.byID)
        for (i in 6..1) {
            val e: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/digit$i")
            e.clear()
        }

    }

    override fun hideApp(){
        mobDriver.runAppInBackground(Duration.ofSeconds(30))
    }

    override fun startToCheckNotification(){
        val p = Point(2 * mobDriver.manage().window().size.width / 3, 50)
        swipeTo(p, Position.BOTTOM,  mobDriver.manage().window().size.height / 2)
    }

    override fun showApp() {
        var element = when (user.customisationAndroid) {
            Customization.SAMSUNG -> mobDriver.findElementById("com.android.systemui:id/content") as  MobileElement

            else -> null
        }
        val y = element!!.rect.y + 10;
        val p = Point(mobDriver.manage().window().size.width / 3, y)
        swipeTo(p, Position.TOP,  y - 50)
       // mobDriver.activateApp("co.letsopen.open.sections.mainscreen.activity.MainScreenActivity")
    }

    private fun passSevenDaysGuide(){
        if(waitUntil("co.letsopen.open.stage:id/sevenDaysGuide", SearchType.byID, 1)){
            val frame: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/sevenDaysGuide")
            val screenHeight = mobDriver.manage().window().size.height;

            if (frame.rect.y < screenHeight / 2)
                tapTo(frame, Position.CENTER)
            else if (frame.rect.y < 2 * screenHeight / 3){
                tapTo(frame, Position.TOP)
                sleep(500)
                tapTo(frame, Position.CENTER)
            }
        }
    }

    private fun passFirstConversationGuide(){
        if (waitUntil("co.letsopen.open.stage:id/topView", SearchType.byID, 1)){
            val button: MobileElement = mobDriver.findElementById("co.letsopen.open.stage:id/button")
            button.click()
        }

    }

}