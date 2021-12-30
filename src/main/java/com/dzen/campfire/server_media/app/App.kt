package com.dzen.campfire.server_media.app

import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api.tools.server.ApiServer
import com.dzen.campfire.api.tools.server.RequestFactory
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.DatabasePool
import java.io.File
import java.nio.charset.Charset

object App {

    val test = (ToolsFiles.readLineOrNull(File("secrets/Config.txt"), 0)?:"")!="release"

    @JvmStatic
    fun main(args: Array<String>) {

        val patchPrefix = ToolsFiles.readLineOrNull(File("secrets/Config.txt"), 1)?:""
        val databaseLogin = ToolsFiles.readLineOrNull(File("secrets/Config.txt"), 6)?:""
        val databasePassword = ToolsFiles.readLineOrNull(File("secrets/Config.txt"), 7)?:""
        val databaseName = ToolsFiles.readLineOrNull(File("secrets/Config.txt"), 8)?:""
        val databaseAddress = ToolsFiles.readLineOrNull(File("secrets/Config.txt"), 9)?:""

        val keysFile = File("secrets/Keys.txt")
        val jksPassword = ToolsFiles.readLineOrNull(keysFile, 3)?:""

        val keyFileJKS = File("secrets/Certificate.jks")
        val keyFileBKS = File("secrets/Certificate.bks")
        val jarFile = "${patchPrefix}CampfireServerMedia.jar"

        val botTokensList = ToolsFiles.readListOrNull("secrets/BotsTokens.txt")?:ArrayList()

        try {
            info("Sayzen Studio")
            info(ToolsDate.getTimeZone(), "( " + ToolsDate.getTimeZoneHours() + " )")
            info("Charset: " + Charset.defaultCharset())
            info("API Version: " + APIMedia.VERSION)

            val apiServer = ApiServer(RequestFactory(jarFile, File("").absolutePath + "\\CampfireServerMedia\\src\\main\\java"),
                    AccountProviderImpl(),
                    ToolsFiles.readFile(keyFileJKS),
                    ToolsFiles.readFile(keyFileBKS),
                    jksPassword,
                    APIMedia.PORT_HTTPS,
                    APIMedia.PORT_HTTP,
                    APIMedia.PORT_CERTIFICATE,
                    botTokensList,
            )

            while (true) {
                try {
                    Database.setGlobal(DatabasePool(databaseLogin, databasePassword, databaseName, databaseAddress, if(test) 1 else 8) { key, time -> })
                    break
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    info("Database crash... try again at 5 sec")
                    ToolsThreads.sleep(5000)
                }

            }

            System.err.println("Media Server")
            System.err.println("------------ (\\/)._.(\\/) ------------")
            apiServer.startServer()

        } catch (th: Throwable) {
            err(th)
        }

    }

}
