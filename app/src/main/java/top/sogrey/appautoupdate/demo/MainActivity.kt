package top.sogrey.appautoupdate.demo

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.async
import org.jetbrains.anko.toast
import top.sogrey.appautoupdate.demo.utils.*
import top.sogrey.lib.bsdiff.BsDiffUtils
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity() {

    var newApkName = "newApk.apk"
    var newApkName2 = "newApk2.apk"
    var patchFileName = "apk.patch"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Example of a call to a native method
        sample_text.text = "Hello world! - v1.0\n新的apk/patch文件存放路径：\n${getUpdateDir()}"
    }

    fun bsdiffFun(view: View) {
        async {
            var isSuccess = BsDiffUtils.diff(this@MainActivity, getUpdateDir() + File.separator + newApkName,
                    getUpdateDir() + File.separator + patchFileName)
            var msg = if (isSuccess) "差分完成" else "差分失败"
            runOnUiThread {
                this@MainActivity.toast(msg)
            }
        }
    }

    fun bspatchFun(view: View) {
        if (getVersionCode() >= 2) {
            toast("已是最新app")
            return
        }
        async {
            var isSuccess = BsDiffUtils.patch(this@MainActivity, getUpdateDir() + File.separator + newApkName2,
                    getUpdateDir() + File.separator + patchFileName)
            var msg = if (isSuccess) "合并完成" else "合并失败"
            runOnUiThread {
                this@MainActivity.toast(msg)
                this@MainActivity.installApk(getUpdateDir() + File.separator + newApkName2)
            }
        }
    }

    private fun doBspatchTask() {
        async {
            val bytes = URL(URL_PATCH_DOWNLOAD).readBytes()
            val patchFile = File(Environment.getExternalStorageDirectory(), PATCH_FILE)
            if (patchFile.exists()) {
                patchFile.delete()
            }
            patchFile.writeBytes(bytes)

            val oldPath = this@MainActivity.getApkSourceDir(packageName)
            var newPath = NEW_APK_PATH
            val patchPath = patchFile.absolutePath
            BsDiffUtils.patch(oldPath, newPath, patchPath)

            runOnUiThread {
                this@MainActivity.installApk(newPath)
            }
        }
    }
}
