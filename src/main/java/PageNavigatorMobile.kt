import com.codeborne.selenide.Selenide.sleep
import enums.ContextMenu
import enums.Position
import enums.Screen
import enums.SearchType
import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileElement
import io.appium.java_client.touch.offset.PointOption
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.OutputType
import support.CommonUtills
import support.User
import java.awt.Point
import java.io.File
import java.time.Duration
import java.time.ZonedDateTime

abstract class PageNavigatorMobile(private val mobDriver: AppiumDriver<MobileElement>, private val user: User) {

    private val utils: CommonUtills = CommonUtills()

    open fun startSession(){
    }

    fun waitRequiredElement(query: String, searchType: SearchType): Boolean {
            return waitUntil(query, searchType, 60);
    }

    fun clickUntil(query: String, searchType: SearchType, timeOut: Long) {
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(timeOut)
        var duration = Duration.between(now, dtOnFuture)
        while (!duration.isNegative) {
            sleep(1000)
            try {
                when (searchType){
                    SearchType.byID -> mobDriver.findElementById(query).click()
                    SearchType.byXPath -> mobDriver.findElementByXPath(query).click()
                    SearchType.byAccessibilityId -> mobDriver.findElementByAccessibilityId(query).click()
                    SearchType.byClassName -> mobDriver.findElementByClassName(query).click()
                }
                break
            } catch (e: NoSuchElementException) { }
            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }
    }

    fun waitUntil(query: String, searchType: SearchType, timeOut: Long): Boolean {
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(timeOut)
        var duration = Duration.between(now, dtOnFuture)
        while (!duration.isNegative) {
            sleep(1000)
            try {
                when (searchType){
                    SearchType.byID -> mobDriver.findElementById(query) as MobileElement
                    SearchType.byXPath -> mobDriver.findElementByXPath(query) as MobileElement
                    SearchType.byAccessibilityId -> mobDriver.findElementByAccessibilityId(query) as MobileElement
                    SearchType.byClassName -> mobDriver.findElementByClassName(query) as MobileElement
                }
                return true
            } catch (e: NoSuchElementException) { }
            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }
        return false
    }

    fun waitUntil(rootElement: MobileElement, query: String, searchType: SearchType, timeOut: Long): Boolean {
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(timeOut)
        var duration = Duration.between(now, dtOnFuture)
        while (!duration.isNegative) {
            sleep(1000)
            try {
                when (searchType){
                    SearchType.byID -> rootElement.findElementById(query) as MobileElement
                    SearchType.byXPath -> rootElement.findElementByXPath(query) as MobileElement
                    SearchType.byAccessibilityId -> rootElement.findElementByAccessibilityId(query) as MobileElement
                    SearchType.byClassName -> rootElement.findElementByClassName(query) as MobileElement
                }
                return true
            } catch (e: NoSuchElementException) { }
            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }
        return false
    }

    fun waitRequiredElement(rootElement: MobileElement, query: String, searchType: SearchType): Boolean {
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(60)
        var duration = Duration.between(now, dtOnFuture)
        while (!duration.isNegative) {
            sleep(1000)
            try {
                when (searchType) {
                    SearchType.byID -> rootElement.findElementById(query) as MobileElement
                    SearchType.byXPath -> rootElement.findElementByXPath(query) as MobileElement
                    SearchType.byAccessibilityId -> rootElement.findElementByAccessibilityId(query) as MobileElement
                    SearchType.byClassName -> rootElement.findElementByClassName(query) as MobileElement
                }
                return true
            } catch (e: Exception) { }

            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }
        return false
    }

    fun swipeTo(element: MobileElement, position: Position, shift: Int) {
        val firstPointToTouchY = element.rect.y + element.rect.height / 2
        val firstPointToTouchX = element.rect.x + element.rect.width / 2
        val  pointToPress = PointOption.point(firstPointToTouchX, firstPointToTouchY)

        val pointToMove = when (position) {
            Position.RIGHT -> PointOption.point(firstPointToTouchX + shift, firstPointToTouchY)
            Position.LEFT -> PointOption.point(firstPointToTouchX - shift, firstPointToTouchY)
            Position.TOP -> PointOption.point(firstPointToTouchX, firstPointToTouchY - shift)
            Position.BOTTOM -> PointOption.point(firstPointToTouchX, firstPointToTouchY + shift)
            else -> null
        }

        PlatformTouchAction(mobDriver)
                .press(pointToPress)
                .moveTo(pointToMove)
                .release()
                .perform()
    }
    fun swipeTo(point: Point, position: Position, shift: Int) {
        val firstPointToTouchY = point.y
        val firstPointToTouchX = point.x
        val pointToPress = PointOption.point(firstPointToTouchX, firstPointToTouchY)

        val pointToMove = when (position) {
            Position.RIGHT -> PointOption.point(firstPointToTouchX + shift, firstPointToTouchY)
            Position.LEFT -> PointOption.point(firstPointToTouchX - shift, firstPointToTouchY)
            Position.TOP -> PointOption.point(firstPointToTouchX, firstPointToTouchY - shift)
            Position.BOTTOM -> PointOption.point(firstPointToTouchX, firstPointToTouchY + shift)
            else -> null
        }

        PlatformTouchAction(mobDriver)
                .longPress(pointToPress)
                .moveTo(pointToMove)
                .release()
                .perform()
    }
    fun tapTo(element: MobileElement, position: Position) {

        val firstPointToTouchX = when (position) {
            Position.TOP -> element.rect.width / 2
            Position.CENTER -> element.rect.width / 2
            Position.LEFT -> 50
            Position.RIGHT -> element.rect.width - 50
            else -> element.rect.width / 2
        }

        val firstPointToTouchY = when (position) {
            Position.TOP -> 50
            else -> element.rect.y + element.rect.height / 2
        }

        val pointToPress = PointOption.point(firstPointToTouchX, firstPointToTouchY)

        PlatformTouchAction(mobDriver)
                .press(pointToPress)
                .release()
                .perform()
    }

    fun tapTo(x: Int, y: Int) {
        val pointToPress = PointOption.point(x, y)
        PlatformTouchAction(mobDriver)
                .press(pointToPress)
                .release()
                .perform()
    }
    open fun checkScreen(screen: Screen): Boolean {
        return true
    }
    open fun checkPostsFromHomeScreen(postTitle: String): Boolean {
        return false
    }
    open fun resetApp() {
        mobDriver.resetApp()
    }
    open fun findPostId(globalPostTitle: String): String {
        return ""
    }
    abstract fun passStartScreen()  //Pavel suggested to use abstract methods
    open fun passWelcomeTour(){}
    open fun passWelcomeTourBySwipe(){}
    open fun startConversation() {}
    open fun enterTextToPostInput(globalPostTitle: String) {}
    open fun returnToHomeScreenByIcon() {}
    open fun setPhone(phone: String) {}
    open fun setPinCode(pinCode: String) {}
    open fun passOnboarding(stopScreen: String): String {
        return ""
    }
    open fun isElementPresent(query: String, searchType: SearchType): Boolean {
        //TODO(): Return element instead of boolean or null. !Suggestion from Pavel!
        return try {
            when (searchType){
                SearchType.byID -> mobDriver.findElementById(query).isEnabled
                SearchType.byXPath -> mobDriver.findElementByXPath(query).isEnabled
                SearchType.byAccessibilityId -> mobDriver.findElementByAccessibilityId(query).isEnabled
                SearchType.byClassName -> mobDriver.findElementByClassName(query).isEnabled
                SearchType.byCssSelector -> mobDriver.findElementByCssSelector(query).isEnabled
            }
        } catch (e: Exception) {
            println(e.message)
            false
        }
    }

    open fun isElementPresent(element: MobileElement, query: String, searchType: SearchType): Boolean {
        return try {
            when (searchType) {
                SearchType.byID -> element.findElementById(query).isEnabled
                SearchType.byXPath -> element.findElementByXPath(query).isEnabled
                SearchType.byAccessibilityId -> element.findElementByAccessibilityId(query).isEnabled
                SearchType.byClassName -> element.findElementByClassName(query).isEnabled
                SearchType.byCssSelector -> element.findElementByCssSelector(query).isEnabled
            }
        } catch (e: Exception) { false }
    }

    open fun checkUserName(): String = ""
    open fun checkBadge(): Boolean = false
    open fun checkFriendsBadge(friendsName: String): Boolean {
        return false
    }
    open fun checkHomeScreenTooltip(): Boolean {
        return false
    }
    open fun joinToPost() {}
    open fun replyToDM() {}
    open fun checkPostSubTitle(): String {
        return ""
    }
    open fun checkPostSubTitleOnHomeScreen(globalPostTitle: String): String {
        return ""
    }
    open fun openPostByID(postID: String) {}
    open fun createSharePost(): String {
        return ""
    }
    open fun checkTextFromLastComment(): String {
        return ""
    }
    open fun deleteOwnMessage() {}
    open fun cancelDeleteOwnMessage(){}
    open fun checkPostTitleFromPost(postTitle: String): Boolean {
        return false
    }
    open fun navigateFromDMChatToParent(): String {
        return ""
    }
    open fun selectOptionInContextmenu(context: ContextMenu) {}
    open fun leaveAPost() {}
    open fun gotoHomeScreen() {}
    open fun clickToMoreButton() {}
    open fun ignoreConversation() {}
    open fun unblockUserFromAccountScreen() {}
    open fun closeWelcomeTooltip() {}
    open fun reportToMessage() {}
    open fun checkTextFromAlertBar(): String {
        return ""
    }
    open fun checkErrors(): String {
        return ""
    }
    open fun navigateBack() {}
    open fun checkHiddenApp(): Boolean {
        return false
    }
    open fun startToCheckNotification(){}

    open fun checkNotification(text: String): Boolean {
        val fileName = mobDriver.getScreenshotAs(OutputType.FILE)
        val result = utils.textRecognize(fileName)
        if (result.replace("\n", " ").contains(text))
            return true

        return false
    }

    open fun logout() {}
    open fun checkMessageTimeCreation() : String{ return ""}
    open fun checkPostUnreadBadges(globalPostTitle: String) :String {return ""}
    open fun checkSuspensionWarning() : Boolean {return false}
    open fun checkSuspenseReasonFromComment(text: String, reason: String) : String {return ""}
    open fun checkSuspensionStatus() : String {return ""}
    open fun tryToEnterTextToPostInput() : String {return ""}
    open fun checkAlert() : Boolean { return  false}
    open fun deleteFriendMessage() : Boolean { return false}
    open fun turnOFFInternetConnection(user: User) {}
    open fun checkInternetConnectionFromPost(): String{return "No internet connection"}
    open fun enterTextToPostInputButNotSend(text: String) {}
    open fun checkTextInInputPost() : String {return ""}
    open fun closeApp(){
    }
    open fun clearPinCode(){
    }
    open fun showApp(){
    }

    open fun hideApp(){}

    open fun checkHidenApp(): Boolean {return true}

    open fun checkAbsentenseOfAwaitingPost(text: String) : String {return ""}

}