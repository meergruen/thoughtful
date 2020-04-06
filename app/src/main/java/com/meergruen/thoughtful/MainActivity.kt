package com.meergruen.thoughtful

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.users.FullAccount

import kotlinx.android.synthetic.main.activity_main.*

import java.text.DateFormat

class MainActivity : DropboxActivity() {

    private val tag = MainActivity::class.java.name
    private var preferences: SharedPreferences? = null
    private var mProgressBar: ProgressBar? = null
    private var mContent: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        preferences = getSharedPreferences("thoughtful", Context.MODE_PRIVATE)
        mContent = findViewById(R.id.content)
        mProgressBar = findViewById(R.id.progressbar)
        mProgressBar!!.visibility = View.INVISIBLE

        send_ab.setOnClickListener { uploadText() }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_change_account -> {
                login(mContent!!)
                true
            }
            R.id.action_clear_on_send -> {
                toggleClearOnSend(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleClearOnSend(item: MenuItem) {

        val clearOnSend = preferences!!.getString("clear-on-send", "true")
        if (clearOnSend == "true") {
            preferences!!.edit().putString("clear-on-send", "false").apply()
            item.setTitle(R.string.clear_on_send)
        }
        else{
            preferences!!.edit().putString("clear-on-send", "true").apply()
            item.setTitle(R.string.no_clear_on_send)
        }

    }

    override fun onResume() {
        super.onResume()
        if (hasToken()) {
            findViewById<View>(R.id.login_button).visibility = View.GONE
            findViewById<View>(R.id.email_text).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.login_button).visibility = View.VISIBLE
            findViewById<View>(R.id.email_text).visibility = View.GONE
        }
    }


    // Button actions

    fun clearFields(v: View) {
        val titleField: EditText = findViewById(R.id.edit_title)
        val contentField: EditText = findViewById(R.id.edit_content)

        titleField.setText("")
        contentField.setText("")
    }

    fun login(v: View) {
        startOAuth2Authentication(this@MainActivity, getString(R.string.app_key))
    }


    // Other functions

    private fun uploadText() {
        val titleField: EditText = findViewById(R.id.edit_title)
        val contentField: EditText = findViewById(R.id.edit_content)

        uploadFile(titleField.text.toString(), contentField.text.toString())
    }

    override fun loadData() {
        GetCurrentAccountTask(DropboxClient.get()!!, object : GetCurrentAccountTask.Callback {
            override fun onComplete(result: FullAccount?) {
                (findViewById<View>(R.id.email_text) as TextView).text = result!!.email
            }

            override fun onError(e: Exception?) {
                Log.e(javaClass.name, "Failed to get account details.", e)
            }
        }).execute()
    }


    private fun uploadFile(title: String, content: String) {

        val thisView: View = findViewById(android.R.id.content)

        val uploadBehaviour = object : UploadFileTask.Callback {
            override fun onUploadComplete(result: FileMetadata?) {

                mProgressBar!!.visibility = View.INVISIBLE
                val message = result!!.name + " succesfully uploaded " +
                        DateFormat.getDateTimeInstance().format(result.clientModified)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
                    .show()
                val clearOnSend = preferences!!.getString("clear-on-send", "true")
                if (clearOnSend == "true") {
                    clearFields(thisView)
                }
            }

            override fun onError(e: Exception?) {
                mProgressBar!!.visibility = View.INVISIBLE
                Log.i("bg", "${e!!.printStackTrace()}")


                Log.e(tag, "Failed to upload file.", e)
                Toast.makeText(this@MainActivity, // Short status message to user, no popup
                    "An error has occurred",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val client = DropboxClient.get()
        if (client != null) {
            mProgressBar!!.visibility = View.VISIBLE
            UploadFileTask( client, uploadBehaviour).execute(title, content)
        }
        else {
            Dialog(this@MainActivity).showInformation("You are not logged in!", "Log in to Dropbox and try again.")
        }
    }

}
