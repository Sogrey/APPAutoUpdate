package top.sogrey.appautoupdate.demo

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.async
import top.sogrey.appautoupdate.demo.utils.*
import top.sogrey.lib.bsdiff.BsDiffUtils
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = ""
        BsDiffUtils.merge("oldFile", "newFile", "patchFile")


        if (getVersionCode() < 2) {
            doBspatchTask();
        }
    }

    private fun doBspatchTask() {
        async {
            val bytes = URL(URL_PATCH_DOWNLOAD).readBytes()
            val patchFile = File(Environment.getExternalStorageDirectory(), PATCH_FILE)
            if (patchFile.exists()) {
                patchFile.delete()
            }
            patchFile.writeBytes(bytes);

            val oldPath = this@MainActivity.getApkSourceDir(packageName)
            var newPath = NEW_APK_PATH
            val patchPath = patchFile.absolutePath
            BsDiffUtils.merge( oldPath, newPath, patchPath)

            runOnUiThread {
                this@MainActivity.installApk(newPath)
            }
        }
    }
}
