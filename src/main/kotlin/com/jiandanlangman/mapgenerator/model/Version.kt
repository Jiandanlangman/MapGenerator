package com.jiandanlangman.mapgenerator.model

data class Version(val errorCode: Int = -1, val errorMsg: String = "", val data: Data = Data()) {

    data class Data(val versionCode: Int = 0, val url: String = "")
}