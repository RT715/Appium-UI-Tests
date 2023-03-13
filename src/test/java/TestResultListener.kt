import com.gurock.testrail.APIClient
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.testng.ITestResult
import org.testng.TestListenerAdapter

class TestResultListener() : TestListenerAdapter() {
    private val RESULT_SUCCESS = 1
    private val RESULT_FAIL = 5

    fun testPlanID(iTestResult: ITestResult): HashMap<*, *> {
        val obj = object: GlobalTestSettings(){}
        val testRunID = obj.testRunID
        val client = obj.client
        val result = HashMap<String, Any>()
        val projectName = iTestResult.instanceName
        if (projectName == "BasicFlow") {
            result["testRunID"] = testRunID
            result["client"] = client
        } else if (projectName == "CommonFlow") {
            result["testRunID"] = testRunID
            result["client"] = client
        }
        return result
    }

    fun testRailResultPublish(iTestResult: ITestResult, result: Int, err: String) {
        val id = testPlanID(iTestResult)["testRunID"].toString()
        val client = testPlanID(iTestResult)["client"] as APIClient
        try {
            val casesID = client.sendGet("get_tests/$id") as JSONArray
            for (o in casesID) {
                val obj = o as JSONObject
                val id2 = obj["id"].toString()
                val methodName = obj["custom_automethod"] ?: continue
                if (methodName.toString() != iTestResult.method.methodName) continue
                val data: HashMap<String, Any> = hashMapOf()
                data["status_id"] = result
                data["case_id"] = id
                data["comment"] = err
                client.sendPost("add_result/$id2", data)
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }


    override fun onTestFailure(iTestResult: ITestResult) {
        val err = when {
            iTestResult.toString().toLowerCase().contains("assert") -> "Assert: ${iTestResult.throwable.message}"
            else -> {
                var base = "Exception: ${iTestResult.throwable.message.toString().trimIndent()}"
                val r = iTestResult.throwable.stackTrace
                for (s in r) base += "${s.className} (${s.lineNumber})"
                base
            }
        }

        testRailResultPublish(iTestResult, RESULT_FAIL, err)
        //todo pnMobileA = null
        //todo pnMobileB = null
        super.onTestFailure(iTestResult)
    }

    override fun onTestSuccess(iTestResult: ITestResult) {
        testRailResultPublish(iTestResult, RESULT_SUCCESS, "Test OK")
        super.onTestSuccess(iTestResult)
    }
}


