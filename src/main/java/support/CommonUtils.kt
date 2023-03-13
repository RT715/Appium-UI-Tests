package support

import com.codeborne.selenide.Selenide
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import net.sourceforge.tess4j.Tesseract
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.*
import javax.imageio.ImageIO
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.search.AndTerm
import javax.mail.search.BodyTerm
import javax.mail.search.FlagTerm
import javax.mail.search.SearchTerm
import kotlin.math.pow

class CommonUtills {

    fun randomValue(exp: Int): Int {
        val d = 10.0
        val r = Random()
        return r.nextInt(d.pow(exp).toInt())
    }

    //props.setProperty("mail.imap.sll.enable", "true");
    fun getLinkForAuth(): String {
        val PROTOCOL_PROP = "mail.store.protocol"
        val PROTOCOL_TYPE = "imaps"
        val pattern = "Sign in to project-236545729171"
        var messageContent: String
        var link = ""
        Selenide.sleep(10000)
        val props = Properties()
        props.setProperty(PROTOCOL_PROP, PROTOCOL_TYPE)
        //props.setProperty("mail.imap.sll.enable", "true");
        val session = Session.getInstance(props, null)
        try {
            val store = session.store
            store.connect("imap.gmail.com", "autotests@letsopen.co", "passW_3478")
            val inbox = store.getFolder("Inbox")
            inbox.open(Folder.READ_WRITE)
            val searchTerm: SearchTerm = BodyTerm(pattern)
            val flagSeen = FlagTerm(Flags(Flags.Flag.SEEN), false)
            val totalTerm: SearchTerm = AndTerm(flagSeen, searchTerm)
            val message = inbox.search(totalTerm)
            var i = 0
            val n = message.size
            while (i < n) {
                val contentType = message[i].contentType
                if (contentType.contains("multipart")) {
                    val multiPart = message[i].content as Multipart
                    val numberOfParts = multiPart.count
                    for (partCount in 0 until numberOfParts) {
                        val part = multiPart.getBodyPart(partCount) as MimeBodyPart
                        messageContent = part.content.toString()
                        if (messageContent.contains("href")) {
                            link = messageContent.split("'>Sign").toTypedArray()[0].split("href='").toTypedArray()[1].replace("amp;", "")
                        }
                    }
                }
                Selenide.sleep(3000)
                message[i].setFlag(Flags.Flag.DELETED, true)
                i++
            }
            inbox.close()
            store.close()
        } catch (e: Exception) {
            println(e.message)
        }
        return link
    }

    fun parseDataFromRemoteConfigJSON(): MutableList<String> {
        var remoteConfigJSON = ""
        try {
            val token = accessToken
            remoteConfigJSON = getRemoteConfig(token)
        } catch (e: Exception) {
            println(e.message)
        }
        return parseData(remoteConfigJSON)
    }

    private fun parseData(json: String): MutableList<String> {
        val obj = JSONObject(json)
        val parameterGroupsJSON = obj.getJSONObject("parameterGroups")
        val onboardingJSON = parameterGroupsJSON.getJSONObject("Default onboarding config")
        val parametersJSON = onboardingJSON.getJSONObject("parameters")
        val array = parametersJSON.toJSONArray(parametersJSON.names())
        val node = array.getJSONObject(0)
        var line = node.getJSONObject("defaultValue").getString("value")

        val screenSequences = onboardingScreenSequences(line)
        val map = mutableListOf<String>()
        val screens = onboardingJSON.getJSONObject("parameters").toMap()
        for (i in screenSequences.indices) {
            when {
                screens.containsKey(screenSequences[i]) -> {
                    line = screens[screenSequences[i]].toString()
                    var button = ""
                    val header = line.split("header\":\"").toTypedArray()[1].split("\",").toTypedArray()[0]
                    if (line.contains("mainButtonTitle"))
                        button = line.split("mainButtonTitle\":\"").toTypedArray()[1].split("\"").toTypedArray()[0]
                    if (button.contains("Share")) button = "Skip"
                    map.add(i, screenSequences[i] + "@" + header + "@" + button)
                }
                screenSequences[i].contains("Rules") -> {
                    map.add(i, screenSequences[i] +
                            "@" +
                            "Do you agree to follow these community principles?" +
                            "@" +
                            "I agree")
                }
                else -> {
                    map.add(i, screenSequences[i] +
                            "@" +
                            " " +
                            "@" +
                            " ")
                }
            }
        }
        return map
    }

    private fun onboardingScreenSequences(screenSequences: String): Array<String> {
        when (screenSequences.toLowerCase()) {
            "s4" -> return arrayOf("Rules", "defaultConversationPrompt", "defaultAllowNotifications", "defaultConnectContacts", "defaultInvites")
            "s7" -> return arrayOf("Age", "defaultAllowNotifications", "defaultConnectContacts", "defaultInvites", "Rules", "defaultConversationPrompt")
            "s8" -> return arrayOf("Age", "Rules", "defaultConversationPrompt", "defaultAllowNotifications", "defaultConnectContacts", "defaultInvites")
            "s9" -> return arrayOf("Age", "Rules", "defaultAllowNotifications", "defaultConnectContacts", "defaultInvites")
            "s10" -> return arrayOf("Age", "defaultAllowNotifications", "defaultConnectContacts", "defaultInvites", "Rules")
        }
        return arrayOf()
    }

    @get:Throws(Exception::class)
    private val accessToken: String
        get() {
            val accessToken: String
            val googleCredential = GoogleCredential
                    .fromStream(FileInputStream("src/main/resources/letsopen-stage.json"))
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.remoteconfig"))
            googleCredential.refreshToken()
            accessToken = googleCredential.accessToken
            return accessToken
        }

    @Throws(Exception::class)
    private fun getRemoteConfig(token: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url("https://firebaseremoteconfig.googleapis.com/v1/projects/letsopen-stage/remoteConfig")
                .header("Authorization", "Bearer $token")
                .build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }

    public fun textRecognize(fileNmae : File): String{
        var result = ""
        val imBuff: ByteArray = Files.readAllBytes(fileNmae.toPath())
        val bais = ByteArrayInputStream(imBuff)
        val bi = ImageIO.read(bais)

        try {
            val tesseract = Tesseract()
            tesseract.setTessVariable("user_defined_dpi", "300")
            tesseract.setDatapath("src/main/resources/tessdata")
            result = tesseract.doOCR(bi)
        } catch (e: java.lang.Exception) {
            println("Сan not recognize the image.")
        }

        return result
    }
}
