import com.gurock.testrail.APIClient
import enums.Customization
import enums.OS
import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileElement
import io.appium.java_client.service.local.AppiumDriverLocalService
import io.appium.java_client.service.local.AppiumServiceBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.setMain
import org.joda.time.DateTime
import org.json.simple.JSONObject
import org.openqa.selenium.remote.DesiredCapabilities
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite
import support.CommonUtills
import support.User
import java.net.URL

abstract class GlobalTestSettings {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private lateinit var server: AppiumDriverLocalService

    var pnMobileA: PageNavigatorMobile
    var pnMobileB: PageNavigatorMobile
    val pn = PageNavigatorWeb()

    val userA: User = User(
            nicName = "",
            phone = "+1 6505559999",
            pinCode = "1 1 1 1 1 1",
            deviceNameAndroid = "Galaxy A50 qa test",
            deviceNameiOS = "00008020-00011DC20CC3002E",
            platformVersionAndroid = "9",
            platformVersioniOS = "14",
            customisationAndroid = Customization.SAMSUNG,
            customisationiOS = Customization.IPHONE_XS
    )

    val userB = User(
            nicName = "",
            phone = "+1 6505559998",
            pinCode = "1 1 1 1 1 1",
            deviceNameAndroid = "Galaxy A50 qa test",
            deviceNameiOS = "00008020-00011DC20CC3002E",
            platformVersionAndroid = "9",
            platformVersioniOS = "13.6",
            customisationAndroid = Customization.SAMSUNG,
            customisationiOS = Customization.IPHONE_XS
    )

    init {
        pnMobileA = PageNavigatorAndroid(
                mobDriver = setUpMobileDriver(os = OS.ANDROID, user = userA),
                user = userA
        )
        pnMobileB = PageNavigatorIOS(
                mobDriver = setUpMobileDriver(os = OS.IOS, user = userB),
                user = userB
        )
    }

    val utills = CommonUtills()
    val globalPostTitle = "An autotest post №" + utills.randomValue(5)
    val globalDMTitle = "An autotest DM №" + utills.randomValue(2)
    var postID = ""
    var dmID = ""
    val client = APIClient("https://opentech.testrail.io/")
    var testRunID = ""

    val baseURL = "https://admin.letsopen.io" //"https://admin-dev.letsopen.io"

    @BeforeSuite
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        //testRailIntegrity(OS.ANDROID)
    }

    @AfterSuite
    fun finish() {
        mainThreadSurrogate.close()
    }

    private fun createAppiumService() {
        val asb = AppiumServiceBuilder()
        asb.withIPAddress("127.0.0.1")
        asb.usingAnyFreePort()
        server = AppiumDriverLocalService.buildService(asb)
        server.start()
    }

    private fun setUpMobileDriver(os: OS, user: User): AppiumDriver<MobileElement> {
        var driver: AppiumDriver<MobileElement>
        createAppiumService()

        with(DesiredCapabilities()) {
            setCapability("path", "/wd/hub")
            setCapability("noReset", "true")
            setCapability("autoLaunch", "true")
            setCapability("launchTimeout", "600000")
            setCapability("newCommandTimeout", "600")
            setCapability("clearSystemFiles", true)

            when (os) {
                OS.ANDROID -> {
                    setCapability("app", "")
                    setCapability("port", "4723")
                    setCapability("appPackage", "co.letsopen.open.stage")
                    setCapability("appActivity", "co.letsopen.open.sections.mainscreen.activity.MainScreenActivity")
                    setCapability("platformName", os)
                    setCapability("platformVersion", user.platformVersionAndroid)
                    setCapability("deviceName", user.deviceNameAndroid)
                    setCapability("automationName", "uiautomator2")
                    setCapability("dontStopAppOnReset", true)
                    driver = AppiumDriver(URL("http://localhost:4723/wd/hub"), this)
                }
                OS.IOS -> {
                    setCapability("noReset", true)
                    setCapability("port", "4723")
                    setCapability("app", "com.opentechnologies.openapp-dev")
                    setCapability("platformName", "iOS")
                    setCapability("platformVersion", user.platformVersioniOS)
                    setCapability("deviceName", "iPhone Xs qa Test")
                    setCapability("platformName", os)
                    setCapability("automationName", "XCUITest")
                    setCapability("udid", user.deviceNameiOS)
                    setCapability("waitForQuiescence", false)
                    setCapability("xcodeOrgId", "PQBZQ8Z5B3")
                    setCapability("xcodeSigningId", "iPhone Developer")
                    //setCapability("nativeInstrumentsLib",  true);
                    driver = AppiumDriver(URL("http://localhost:4723/wd/hub"), this)
                }
            }
        }
        return driver
    }

    private fun testRailIntegrity(os: OS) {
        val testPlanID = when (os) {
            OS.ANDROID -> 1
            OS.IOS -> 2
        }
        client.user = "artem@letsopen.co"
        client.password = "Flatron_788"
        try {
            val data = HashMap<String, Any>()
            data["suite_id"] = testPlanID
            data["name"] = "${DateTime.now().toString("MM/dd/YY")} - ${os.name} Regression Dev"
            data["include_all"] = true
            val o = client.sendPost("add_run/$testPlanID", data) as JSONObject
            testRunID = o["id"].toString()
        } catch (e: Exception) {
            println(e.message)
        }
    }

}