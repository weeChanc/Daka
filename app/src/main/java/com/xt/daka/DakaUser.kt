package com.xt.daka

import android.graphics.Bitmap
import android.util.Log
import com.xt.daka.data.model.request.ParamsFaceAcquire
import com.xt.daka.data.model.request.ParamsLogin
import com.xt.daka.data.model.response.User
import com.xt.daka.data.source.remote.api.FaceApi
import com.xt.daka.network.RetrofitClient
import com.xt.daka.network.youtu.Youtu
import com.xt.daka.network.youtu.data.model.CompareResult
import com.xt.daka.ui.login.LoginException
import com.xt.daka.ui.sign.SignException
import com.xt.daka.util.helper.toast
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by steve on 17-10-20.
 */
object DakaUser {

    var user: User.UserDetial? = null

    val MIN_SIMILARITY = 80

    fun login(account: String, password: String) =
            RetrofitClient.faceClient.create(FaceApi::class.java)
                    .login(ParamsLogin(account, password))
                    .map { map ->
                        map.body()
                    }
                    .doOnNext { user ->
                        if(user.status != 3){
                            throw LoginException(user.status)
                        }else{
                            DakaUser.user = user.user
                        }
                    }

    fun signin(bm: Bitmap)  =
            RetrofitClient.faceClient.create(FaceApi::class.java)
                    .getface(ParamsFaceAcquire(user!!.username))
                    .flatMap { mapper ->
                        if (mapper.status == 1) {
                            Youtu.compareBase64(bm, mapper.imageStringData).doOnNext { result ->

                                if (result.errorCode != 0) {
                                    throw SignException(result.flag, result.errorMsg)
                                }
                            }
                        }
                        else {

                            Observable.create<CompareResult> { emitter ->
                                emitter.onError(SignException(-1,"无法找到人脸,请联系管理员"))
                            }

                        }

                    }
    fun getFace() = RetrofitClient.faceClient.create(FaceApi::class.java)
            .getface(ParamsFaceAcquire(user!!.username))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())



}