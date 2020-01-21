package com.smttcn.safebox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.extensions.toBase64String
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.Base64.encode
import com.smttcn.commons.helpers.Base64.decode
import com.smttcn.safebox.MyApplication
import java.util.HashMap

class DebugViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = getDebugText()
    }
    val text: LiveData<String> = _text


    private fun getDebugText(): String {
        var result: StringBuilder = java.lang.StringBuilder()

        val pwd = "111111"
        //result.append(">" + Authenticator().generateAndSaveAppDatabaseSecret(pwd))
        result.append(">" + Authenticator().getAppDatabaseSecretWithAppPassword(pwd) + "\n")

        return result.toString()
    }

    private fun getDebugText3(): String {
        var result: StringBuilder = java.lang.StringBuilder()

        val pwd = "password"
        val enc = Encryption()
        val ba = enc.generateSecret()
        result.append(ba + "\n")
        val en_pwd = enc.encrypt(ba.toByteArray(Charsets.UTF_8), pwd.toCharArray())

        MyApplication.getBaseConfig().appDatabaseSecretHashMap = en_pwd
        val mp_pwd = MyApplication.getBaseConfig().appDatabaseSecretHashMap

        val dc_pwd = enc.decrypt(mp_pwd, pwd.toCharArray())
        result.append(String(dc_pwd!!, Charsets.UTF_8))

        return result.toString()
    }

    private fun getDebugText2(): String {
        var result: StringBuilder = java.lang.StringBuilder()


        val pwd = "password"
        val ba_pwd = pwd.toByteArray(Charsets.UTF_8)
        result.append(pwd + "\n")
        for (b in ba_pwd) {
            result.append(String.format("%02X", b))
        }
        result.append("\n\n")


        val b64 = encode(ba_pwd)
        val b64_ba = decode(b64)
        result.append(String(b64) + "\n")
        for (b in b64_ba) {
            result.append(String.format("%02X", b))
        }
        result.append("\n" + b64_ba.toString(Charsets.UTF_8) + "\n\n")


        val enc = Encryption()
        val en_pwd = enc.encrypt("1234567890".toByteArray(), pwd.toCharArray())
        val b64_pwd = en_pwd.toBase64String()
        result.append(b64_pwd + "\n\n")

        MyApplication.getBaseConfig().appDatabaseSecretHashMap = en_pwd

        val mp_pwd = MyApplication.getBaseConfig().appDatabaseSecretHashMap

        //val map = HashMap<String, ByteArray>().fromBase64String(b64_pwd)
        val dc_pwd = enc.decrypt(mp_pwd, pwd.toCharArray())
        result.append(String(dc_pwd!!, Charsets.UTF_8))

        return "\n\n" + result.toString()
    }


    private fun getDebugText1(): String {
        val path = FileManager.documentRoot
        val items = FileManager.getFileDirItemsInFolder().joinToString("\n--\n")
        return path + "\n\n" + items + "\n-- Last line --"
    }
}