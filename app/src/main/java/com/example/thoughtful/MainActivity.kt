package com.example.thoughtful

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

    private var mProgressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { uploadText() }

        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener { startOAuth2Authentication(this@MainActivity, getString(R.string.app_key)) }

        mProgressBar = findViewById(R.id.progressbar)
        mProgressBar!!.visibility = View.INVISIBLE

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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun uploadText() {
        val titleField: EditText = findViewById(R.id.edit_title)
        val contentField: EditText = findViewById(R.id.edit_content)

        uploadFile(titleField.text.toString(), contentField.text.toString())
    }

    override fun onResume() {
        super.onResume()
        if (hasToken()) {
            findViewById<View>(R.id.login_button).visibility = View.GONE
            findViewById<View>(R.id.email_text).visibility = View.VISIBLE
            findViewById<View>(R.id.name_text).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.login_button).visibility = View.VISIBLE
            findViewById<View>(R.id.email_text).visibility = View.GONE
            findViewById<View>(R.id.name_text).visibility = View.GONE
        }
    }

    override fun loadData() {
        GetCurrentAccountTask(DropboxClient.get()!!, object : GetCurrentAccountTask.Callback {
            override fun onComplete(result: FullAccount?) {
                (findViewById<View>(R.id.email_text) as TextView).text = result!!.email
                (findViewById<View>(R.id.name_text) as TextView).text = result.name.displayName
            }

            override fun onError(e: Exception?) {
                Log.e(javaClass.name, "Failed to get account details.", e)
            }
        }).execute()
    }


    private fun uploadFile(title: String, content: String) {
        mProgressBar!!.visibility = View.VISIBLE

        val obj = object : UploadFileTask.Callback {
            override fun onUploadComplete(result: FileMetadata?) {

                mProgressBar!!.visibility = View.INVISIBLE
                val message = result!!.name + " size " + result.size + " modified " +
                        DateFormat.getDateTimeInstance().format(result.clientModified)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onError(e: Exception?) {
                mProgressBar!!.visibility = View.INVISIBLE
                Log.i("bg", "${e!!.printStackTrace()}")


                Log.e(tag, "Failed to upload file.", e)
                Toast.makeText(this@MainActivity,
                    "An error has occurred",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }

        UploadFileTask( DropboxClient.get()!!, obj).execute(title, content)
    }

}
