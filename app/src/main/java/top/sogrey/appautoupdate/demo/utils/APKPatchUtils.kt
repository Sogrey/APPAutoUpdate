package top.sogrey.appautoupdate.demo.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File

/**
 * 描述：
 * Created by Sogrey on 2018/11/18.
 */
val PATCH_FILE = "apk.patch"
val URL_PATCH_DOWNLOAD = "http://192.168.1.15:8080/" + PATCH_FILE
val PACKAGE_NAME = "top.sogrey.appautoupdate.demo"
val SD_CARD = Environment.getExternalStorageDirectory().absolutePath + File.separator
val NEW_APK_PATH = SD_CARD + "newApk.apk"
val PATCH_FILE_PATH = SD_CARD + PATCH_FILE
/**
 * 为Context扩展方法获取应用版本号
 */
fun Context.getVersionCode(): Int {
    val packageManager = this.packageManager;
    val packageInfo = packageManager.getPackageInfo(this.packageName, 0)
    Log.d("Context.getVersionCode", "versionCode={${packageInfo.versionCode}}")
    return packageInfo.versionCode
}

/**
 * 获取已安装应用apk原文件所在
 */
fun Context.getApkSourceDir(packageName:String):String{
    return this.packageManager.getApplicationInfo(packageName,0).sourceDir
}

fun Context.getUpdateDir():String{
    return this.getExternalFilesDir("update").absolutePath
}

/**
 * 安装apk
 */
fun Context.installApk(apkPath:String){
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(Uri.parse("file://"+apkPath),"application/vnd.android.package-archive")
    this.startActivity(intent)
}