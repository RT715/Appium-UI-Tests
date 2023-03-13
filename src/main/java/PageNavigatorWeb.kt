import com.codeborne.selenide.Condition
import com.codeborne.selenide.ElementsCollection
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.SelenideElement
import com.codeborne.selenide.WebDriverRunner
import org.openqa.selenium.chrome.ChromeDriver
import support.CommonUtills
import java.time.Duration
import java.time.ZonedDateTime

class PageNavigatorWeb {
    private val utills: CommonUtills = CommonUtills()
    private val webDriver = ChromeDriver()

    fun startBrowser() {
        WebDriverRunner.setWebDriver(webDriver)
        webDriver.manage().window().maximize()
    }

    fun close(){
        webDriver.close()
    }

    fun loginToAdminPage() {
        open("https://admin.letsopen.io/#/login")
        val emailEditBox = `$`("[id='firebaseui-auth-container'] [type='email']")
        emailEditBox.waitUntil(Condition.exist, 5000)
        emailEditBox.value = "autotests@letsopen.co"
        val button = `$`("[id='firebaseui-auth-container'] [type='submit']")
        button.click()
        val link: String = utills.getLinkForAuth()
        open(link)
        sleep(5000)
    }

    fun openURL(url: String) {
        open(url)
        refresh()
        while (true) {
            sleep(5000)
            var element = `$`("[class='card-text text-center']")
            if (!element.exists())
                return
            else {
                if (element.text().equals("No pending content."))
                    refresh()
                else
                    return
            }
        }
    }


    fun approvePost(userPostText :String) {

        val fields = `$$`("[class='row']")
        for (field in fields){
            if(!field.`$`("[class='card-body']").exists())
                continue

            val textFiled = field.`$`("[class='card-body']")
            val textElement = textFiled.`$`("[class='card-text']")
            if (textElement.text.trim() == userPostText){
                val editButton = textFiled.`$$`("button").last()

                val combo = `$$`("[class^='custom-select']").first()
                if (combo.`is`(Condition.visible)) {
                    combo.click()
                    val comboItem = combo.`$$`("option").find(Condition.attribute("value", "selfDisclosureEmotional"))
                    comboItem.click()
                }

                sleep(2000)
                if (!field.`$`("[id='decisionForm']").`is`(Condition.visible)) {
                    editButton.scrollTo().click()
                }

                val buttonsForm = field.`$`("[id='decisionForm']")
                val approveButton = buttonsForm.`$`("[id='decision_BV_option_0']")
                val saveButton = buttonsForm.`$`("[class='btn btn-link']")

                approveButton.scrollTo().parent().click()
                saveButton.scrollTo().click()
                break
            }
        }
    }

    fun rejectMessage(userPostText :String) {
        val fields = `$$`("[class='row']")
        for (field in fields){
            if(!field.`$`("[class='card-body']").exists())
                continue

            val textFiled = field.`$`("[class='card-body']")
            val textElement = textFiled.`$`("[class='card-text']")
            if (textElement.text().trim() == userPostText){
                val editButton = textFiled.`$$`("button").last()

                if (!field.`$`("[id='decisionForm']").exists())
                    editButton.scrollTo().click()

                val buttonsForm = field.`$`("[id='decisionForm']")
                val rejectButton = buttonsForm.`$`("[id='decision_BV_option_1']")
                val saveButton = buttonsForm.`$`("[class='btn btn-link']")

                rejectButton.scrollTo().parent().click()
                val combo = buttonsForm.`$`("select")
                combo.click()
                val comboItem = combo.`$$`("option").find(Condition.attribute("value", "Mean"))
                comboItem.click()

                saveButton.scrollTo().click()
                break
            }
        }
    }

    fun checkPostText(text: String, searchForDisplay: Boolean): Boolean {
        var now = ZonedDateTime.now()
        val dtOnFuture = now.plusSeconds(10)
        var duration = Duration.between(now, dtOnFuture)
        while (!duration.isNegative) {
            sleep(500)
            try {
                val c = `$$`("[class='card-text']")
                val e = c.find(Condition.text(text))
                if (e.isDisplayed && searchForDisplay) return true else if (!e.isDisplayed && searchForDisplay) return true
            } catch (ex: Exception) {
                continue
            }
            now = ZonedDateTime.now()
            duration = Duration.between(now, dtOnFuture)
        }
        return false
    }


    fun unSuspenseUser(phone: String){
        open("https://us-central1-letsopen-co.cloudfunctions.net/Https-Support_ReActivateUser?apiKey=002cXUXgJJVrVpuX5KDy&phoneNumberWithoutPlus=${phone.trim()}&karma=0")
    }
}