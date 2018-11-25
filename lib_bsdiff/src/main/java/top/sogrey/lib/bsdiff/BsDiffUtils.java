package top.sogrey.lib.bsdiff;

import android.content.Context;

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


    public static boolean patch(Context context, String newApkPath, String patchPath) {
        return patch(context.getPackageResourcePath(), newApkPath, patchPath) && new File(newApkPath).exists();
    }

    public static boolean diff(Context context, String newApkPath, String patchPath) {
        return diff(context.getPackageResourcePath(), newApkPath, patchPath) && new File(patchPath).exists();
    }
}