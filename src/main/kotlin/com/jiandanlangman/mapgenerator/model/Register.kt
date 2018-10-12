package com.jiandanlangman.mapgenerator.model

data class Register(val errorCode: Int = -1, val errorMsg: String = "", val data: Data = Data()) {

    data class Data(val success: Boolean = false, val nonceStr: String = "", val controllers: ArrayList<Controller> = ArrayList()) {

        data class Controller(val type: Int = 0, val status: Int = 0, val extendInfo: String = "")

    }

}