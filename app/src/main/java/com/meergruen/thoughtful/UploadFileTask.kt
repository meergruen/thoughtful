package com.meergruen.thoughtful

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

    private val tag = "UploadFileTask"

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

    override fun doInBackground(vararg params: String?): FileMetadata? {
        val title: String = params[0] ?: ""
        val content = params[1] ?: ""

        val remoteFileName = createFileName(title)
        Log.i(tag, remoteFileName)

        try {

            val stream = content.byteInputStream()

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

    private fun createFileName(title: String): String {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()) // Locale.GERMANY
        val date = dateFormat.format(Date())

        var titleAppendix = ""
        if(title.isNotBlank()) {
            titleAppendix = "_" + safeForFileName(title)
        }

        return "/$date$titleAppendix.txt"
    }

    private fun safeForFileName(string: String): String {
        return string
            .replace("[\\s]+".toRegex(),"_" )
            .replace("[^\\w-]+".toRegex(), "")
    }

}
