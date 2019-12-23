package com.example.thoughtful

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.WriteMode
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Async task to upload a file to a directory
 */
internal class UploadFileTask(private val mDbxClient: DbxClientV2, private val mCallback: Callback) : AsyncTask<String?, Void?, FileMetadata?>() {
    private var mException: Exception? = null

    interface Callback {
        fun onUploadComplete(result: FileMetadata?)
        fun onError(e: Exception?)
    }

    override fun onPostExecute(result: FileMetadata?) {
        super.onPostExecute(result)
        when {
            (mException != null) -> mCallback.onError(mException)
            (result == null) -> mCallback.onError(null)
            else -> mCallback.onUploadComplete(result)
        }
    }


    private fun createFileName(title: String): String {
        var titleAppendix = ""

        if(title.isNotBlank()) {
            titleAppendix = "-" + title.replace(" ","_" )
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)
        val date = dateFormat.format(Date())

        Log.i("date", date)


        return "/$date$titleAppendix.txt"

    }


    override fun doInBackground(vararg params: String?): FileMetadata? {
        val title = params[0]
        val content = params[1]

        val remoteFileName = createFileName(title!!)
        Log.i("date", remoteFileName)

        try {

            val stream = content!!.byteInputStream()

            stream.use { inputStream ->
                    return mDbxClient.files().uploadBuilder(remoteFileName)
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(inputStream)
            }

        } catch (e: DbxException) {
            mException = e
        } catch (e: IOException) {
            mException = e
        }
        return null
    }

}