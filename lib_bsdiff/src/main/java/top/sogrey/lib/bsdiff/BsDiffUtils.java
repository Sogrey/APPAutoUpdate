package top.sogrey.lib.bsdiff;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

/**
 * 描述：增量更新工具包
 * Created by Sogrey on 2018/11/25.
 */

public class BsDiffUtils {
    static {
        System.loadLibrary("bsdiff_lib");
    }

    public synchronized static native boolean patch(String oldFilePath, String newFilePath, String patchPath);

    public synchronized static native boolean diff(String oldFilePath, String newFilePath, String patchPath);

    /**
     * 本应用文件合并
     *
     * @param context    上下文
     * @param newApkPath 新文件路径
     * @param patchPath  差分包路径
     * @return true:成功，else false 失败
     */
    public static boolean patch(Context context, String newApkPath, String patchPath) {
        File newFile = new File(newApkPath);
        File patchFile = new File(patchPath);
        if (newFile.exists()) newFile.delete();
        if (!patchFile.exists()) {
            Toast.makeText(context, "差分包找不到：" + patchPath, Toast.LENGTH_LONG).show();
            return false;
        }
        return patch(context.getPackageResourcePath(), newApkPath, patchPath) && new File(newApkPath).exists();
    }

    /**
     * 文件合并
     *
     * @param context    上下文
     * @param oldApkPath 旧文件
     * @param newApkPath 新文件路径
     * @param patchPath  差分包路径
     * @return true:成功，else false 失败
     */
    public static boolean patch(Context context, String oldApkPath, String newApkPath, String patchPath) {
        File oldFile = new File(oldApkPath);
        File newFile = new File(newApkPath);
        File patchFile = new File(patchPath);
        if (newFile.exists()) newFile.delete();
        if (!oldFile.exists()) {
            Toast.makeText(context, "旧文件找不到：" + patchPath, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!patchFile.exists()) {
            Toast.makeText(context, "差分包找不到：" + patchPath, Toast.LENGTH_LONG).show();
            return false;
        }
        return patch(oldApkPath, newApkPath, patchPath) && new File(newApkPath).exists();
    }

    /**
     * 本应用文件差分
     *
     * @param context    上下文
     * @param newApkPath 新文件路径
     * @param patchPath  差分包路径
     * @return true:成功，else false 失败
     */
    public static boolean diff(Context context, String newApkPath, String patchPath) {
        File newFile = new File(newApkPath);
        File patchFile = new File(patchPath);
        if (!newFile.exists()) {
            Toast.makeText(context, "newApk.apk找不到：" + newApkPath, Toast.LENGTH_LONG).show();
            return false;
        }
        if (patchFile.exists()) patchFile.delete();

        return diff(context.getPackageResourcePath(), newApkPath, patchPath) && new File(patchPath).exists();
    }

    /**
     * 文件差分
     *
     * @param context    上下文
     * @param oldApkPath 旧文件
     * @param newApkPath 新文件路径
     * @param patchPath  差分包路径
     * @return true:成功，else false 失败
     */
    public static boolean diff(Context context, String oldApkPath, String newApkPath, String patchPath) {
        File oldFile = new File(oldApkPath);
        File newFile = new File(newApkPath);
        File patchFile = new File(patchPath);
        if (!oldFile.exists()) {
            Toast.makeText(context, "旧文件找不到：" + patchPath, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!newFile.exists()) {
            Toast.makeText(context, "newApk.apk找不到：" + newApkPath, Toast.LENGTH_LONG).show();
            return false;
        }
        if (patchFile.exists()) patchFile.delete();

        return diff(oldApkPath, newApkPath, patchPath) && new File(patchPath).exists();
    }
}