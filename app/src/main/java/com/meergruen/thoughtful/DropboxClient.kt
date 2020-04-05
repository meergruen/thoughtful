package com.meergruen.thoughtful

import android.util.Log
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.http.OkHttp3Requestor
import com.dropbox.core.v2.DbxClientV2

/**
 * Singleton instance of [DbxClientV2] and friends
 */
object DropboxClient {

    private val tag = "DropboxClient"

    private var sDbxClient: DbxClientV2? = null
    private var sDbxRequestConfig: DbxRequestConfig? = null


    fun init(accessToken: String?) {
        if (sDbxClient == null) {

            if (sDbxRequestConfig == null) {
                sDbxRequestConfig = DbxRequestConfig.newBuilder("thoughtful")
                    .withHttpRequestor(OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build()
            }

            sDbxClient = DbxClientV2(sDbxRequestConfig, accessToken)
        }
    }

    fun get(): DbxClientV2? {
        if (sDbxClient == null) {
            Log.i(tag, "Client not inititialized.")
        }
        return sDbxClient
    }

}
