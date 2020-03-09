package com.smttcn.safebox.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.crypto.KeyUtil
import com.smttcn.commons.extensions.toBase64String
import com.smttcn.commons.helpers.Base64.encode
import com.smttcn.commons.helpers.Base64.decode
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.database.AppDatabase
import com.smttcn.safebox.database.DbItem
import com.smttcn.safebox.database.DbItemRepository
import com.smttcn.safebox.ui.main.MainActivity

class DebugViewModel(application: Application) : AndroidViewModel(application){

    lateinit private var repo: DbItemRepository
    lateinit var allDbItems: LiveData<List<DbItem>>
    var app : Application

    private val _text = MutableLiveData<String>().apply {
        value = getDebugText()
    }
    val text: LiveData<String> = _text

    init {
        app = application
        initViewModel(app)
    }

    private fun initViewModel(application : Application) {
        // Gets reference to DbItemDao from AppDatabase to construct
        // the correct DbItemRepository.
        if (AppDatabase.getDb(application, viewModelScope) != null) {
            val dbItemDao = AppDatabase.getDb(application, viewModelScope)!!.dbItemDao()
            repo = DbItemRepository(dbItemDao)
            allDbItems = repo.allDbItems
        } else {
            allDbItems = MutableLiveData()
        }
    }

    private fun getDebugText(): String {

        var result: StringBuilder = java.lang.StringBuilder()

        return result.toString()
    }

    private fun getDebugText5(): String {
        var result: StringBuilder = java.lang.StringBuilder()

        val files = FileManager.getFilesInDocumentRoot()

        files.forEach(){
            if (!FileManager.isEncryptedFile(it)) {
                val encFilename = FileManager.encryptFile(it, "111111".toCharArray())
                if (encFilename.length > 5) result.append(encFilename + "\n")
            }
        }

//        if (FileManager.isEncryptedFile(file)) {
//            result.append("Decrypting : " + file.name)
//            FileManager.decryptFile(file, "password".toCharArray())
//        } else {
//            result.append("Encrypted file not found!")
//        }

        return result.toString()
    }

    private fun getDebugText4(): String {
        var result: StringBuilder = java.lang.StringBuilder()

        val pwd = "111111"
        result.append(">" + KeyUtil().getAppDatabaseSecretWithAppPassword(pwd.toCharArray()) + "\n")

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
        val path = FileManager.documentDataRoot
        val items = FileManager.getFileDirItemsInDocumentRoot().joinToString("\n--\n")
        return path + "\n\n" + items + "\n-- Last line --"
    }
}