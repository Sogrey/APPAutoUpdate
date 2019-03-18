# Android App 增量更新



## 原理

自从Android 4.1开始，Google Play引入了[应用程序的增量更新功能](http://developer.android.com/about/versions/jelly-bean.html)，App使用该升级方式，可节省约2/3的流量。

> ### Smart App Updates
>
> Smart app updates is a new feature of Google Play that introduces a better way of delivering **app updates** to devices. When developers publish an update, Google Play now delivers only the **bits that have changed** to devices, rather than the entire APK. This makes the updates much lighter-weight in most cases, so they are faster to download, save the device’s battery, and conserve bandwidth usage on users’ mobile data plan. On average, a smart app update is about **1/3 the size** of a full APK update.
>
> 智能应用更新是Google Play的一项新功能，它引入了向设备提供应用更新的更好方式。当开发者发布更新时，Google Play现在只提供已更改为设备的位，而不是整个APK。这使得更新在大多数情况下更轻，因此可以更快地下载，节省设备的电池，并节省用户移动数据计划的带宽使用。平均而言，智能应用更新大约是完整APK更新大小的1/3。

现在国内主流的应用市场也都支持应用的增量更新了。

> 增量更新的原理，就是将手机上已安装APK与服务器端最新APK进行二进制对比，得到差分包，用户更新程序时，只需要下载差分包，并在本地使用差分包与已安装的apk，合成新版APK。

比方，当前设备中已经安装了一应用v1.0为20M大小，现在这个应用发布了新的版本v2.0大小为30M,按照原来的整包下载的升级方案，每个用户需要下载新包花费30M流量，假设有1000个用户，那么每次升级服务器流量在30 * 1000 = 30000M≈29.3G,按照阿里云流量价格 11G/￥280，那么29.3G需要￥745.8，而实际用户量可能远远大于1000，总的流量费用会更高。如果采用增量更新，用户不需要下载完整的apk文件，而只需要下载差分包，并在本地使用差分包与已安装的apk，合成新版APK安装。从旧的应用20M升级到新的版本30M差分包需要的流量大约在10M左右，为什么说左右而不是确定的10M,等你做过差分包就知道了，增量的大小并不一定刚好等于 30M-20M =10M，往往会比这个值更小，9M或是8M,那么就按照10M来计算升级1000个用户需要10 * 1000=10000M≈9.7G,需要的流量费用￥248.58，费用仅为完整升级方案的1/3。

apk文件的差分，合成，可以通过[开源的二进制比较工具bsdiff](http://www.daemonology.net/bsdiff/)来实现，又因为bsdiff依赖bzip2，所以我们还需要用到[bzip2](http://www.bzip.org/downloads.html)

bsdiff中，`bsdiff.c`用于生成差分包，`bspatch.c`用于合成文件。

原理理清之后，我们想实现增量更新，共需要做3件事：

- 在服务器端，生成新旧两个版本的apk的差分包;
- 在手机客户端，使用已安装的APK与这个差分包进行合成，得到新版的apk文件;
- 校验新合成的APK文件是否完整，MD5或SHA1是否正确，如正确，则引导用户安装;

``` flow
st0=>start: 开始
st=>operation: 服务器端，生成新旧两个版本的apk的差分包
op0=>operation: 将差分包放在服务器上保证app可以通过外链下载到
op1=>operation: 手机客户端，检测更新并下载到差分包
op2=>operation: 将下载到的差分包与已安装的旧应用apk合并得到新的apk包
cond=>condition: 项目实用yes/测试no?
cond2=>condition: 检查APK文件是否完整？
op4=>operation: 检查并修正生成差分包项目
end=>end: 手机客户端，安装新合并生成的apk

st0->st->cond(no,right)->cond2(no,right)->op4(right)->st
cond2(yes)->op0
cond(yes)->op0->op1->op2->end

```

## 生成差分包

理清流程先来生成差分包吧，前面说过需要准备两个工具[bsdiff](http://www.daemonology.net/bsdiff/)和[bzip2](http://www.bzip.org/downloads.html)。

#### 下载[`bsdiff`](http://www.daemonology.net/bsdiff/)

当前版本为4.3了，点击`here`下载。

![下载bsdiff](https://sogrey.github.io\pics\download_bsdiff.jpg)



#### 下载[`bzip2`](http://www.bzip.org/downloads.html)

![下载bzip2](https://sogrey.github.io\pics\download_bzip2.jpg)

下载地址上并没有直接给出下载链接，但是有一句提示，懂点英语的都能看懂，提示我们可以到` SourceForge`找到最新版，先不管`SourceForge`到底是什么东东，百度一下你就知道，

![下载bzip2](https://sogrey.github.io\pics\download_bzip2_2.jpg)

看结果就应该是第一条了，点进去[搜索一下](https://sourceforge.net/directory/os:windows/?q=bzip2)

![下载bzip2](https://sogrey.github.io\pics\download_bzip2_3.jpg)

应该是它，点击[See Project](https://sourceforge.net/projects/bzip2/)

![下载bzip2](https://sogrey.github.io\pics\download_bzip2_4.jpg)

看见[Download](https://sourceforge.net/projects/bzip2/files/latest/download)了吧，下载就是了。

经过两步下载，我们得到了两个压缩文件`bsdiff-4.3.tar.gz`和`bzip2-1.0.6.tar.gz`，我们只需要其中的`.h`、`.c`和`.cpp`文件，拷出来备用。

``` bash
cpp目录
├─bsdiff_4_3
│      bsdiff.c #用于生成差分包
│      bspatch.c #用于合成文件
└─bzip2_1_0_6
        blocksort.c
        bzip2.c
        bzip2recover.c
        bzlib.c
        bzlib.h
        bzlib_private.h
        compress.c
        crctable.c
        decompress.c
        dlltest.c
        huffman.c
        mk251.c
        randtable.c
        spewG.c
        unzcrash.c
```

可以先简单的看下查分以及合并的c源码。

差分`bsdiff.c`的`main()`方法：

![bspatch main](https://sogrey.github.io\pics\bsdiff_main.jpg)

可以看到做差分的`bsdiff.c`的`main()`有一行代码：

```c
int main(int argc,char *argv[])
{
	//省略部分代码...
	if(argc!=4) errx(1,"usage: %s oldfile newfile patchfile\n",argv[0]);    
    //省略部分代码...
}
```

参数`argc`不等于4就会报异常，那么确定argc只能等于4；后面报的异常信息里表示的意思是，`argv`参数应该是`%s oldfile newfile patchfile`，其中`%s`指代的是`argv[0]`，只做打印信息输出用，那么`argv`也能确定下来：["任意字段，作为打印信息输出，像android里Log的参数tag","oldfile 旧的文件","newfile 新的文件","patchfile 差分文件"]长度为4。

再看合并`bspatch.c`的`main()`方法（是不是神似，那就不多说了。）：

![bspatch main](https://sogrey.github.io\pics\bspatch_main.jpg)

下面就是开心又兴奋的撸码环节。

## 新建Android项目，引入`bsdiff`和`bzip2`

新建android项目过程就不说了，**记得勾上c++支持**，将我们刚刚拷贝出来的`bsdiff`和`bzip`的c原文件以及头文件添加到`cpp`目录下：

![编译异常](https://sogrey.github.io\pics\app_update3.jpg)

我们知道`bsdiff`是依赖于`bzip2`的，在`bsdiff.c`与`bspatch.c`引入的头文件里找到有这么一句：

``` c
#include <bzlib.h>
```

而现在我们这两个项目的相对路径发生变化，只需要修改为：

``` c
#include "../bzip2_1_0_6/bzlib.h"
```

> 相对路径你可以根据自己放置文件的位置来配置。

在java目录下添加一个工具类：

``` java
package top.sogrey.appautoupdate.demo.utils;

/**
 * 描述：拆分，合并
 * Created by Sogrey on 2018/11/18.
 */

public class BsDiffUtils {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("bsdiff");//`bsdiff`是我们命名的so库名
    }
    public static native String stringFromJNI();
    public static native void bspatch(String tag,String oldfile,String newfile,String patchfile);
}

```

我们把要生成的.so库名命名为`bsdiff`(这个你可以随便命名，但必须和`CMakeList.txt`里保持一致)。

在`Terminal`里cd到`BsDiffUtils.java`所在目录执行：

```bash
javah {包名}.BsDiffUtils
```

生成头文件`top_sogrey_appautoupdate_demo_utils_BsDiffUtils.h`拷贝到cpp目录下，在cpp目录下再新建`top_sogrey_appautoupdate_demo_utils_BsDiffUtils.cpp`将`top_sogrey_appautoupdate_demo_utils_BsDiffUtils.h`里的方法拷贝过去实现它：

``` cpp
#include <string>
#include "top_sogrey_appautoupdate_demo_utils_BsDiffUtils.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_top_sogrey_appautoupdate_demo_utils_BsDiffUtils_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
/*
 * Class:     top_sogrey_appautoupdate_demo_utils_BsDiffUtils
 * Method:    bspatch
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
 extern "C"
JNIEXPORT void JNICALL
Java_top_sogrey_appautoupdate_demo_utils_BsDiffUtils_bspatch
  (JNIEnv *, jclass, jstring tag, jstring oldFile, jstring newFile, jstring patchfile){
//    一会在实现;
}
```

下面修改`CMakeList.txt`:

``` bash
#最低版本为3.4.1
cmake_minimum_required(VERSION 3.4.1)
#file() 三个参数:
#GLOB 固定，globe全局
#第二个参数 变量名，可以随便起
#第三个参数 相对路径下某一类文件，*.c 表示该目录下所有后缀为`.c`的文件
file(GLOB mainutils src/main/cpp/*.cpp)
file(GLOB bsdiff src/main/cpp/bsdiff_4_3/*.c)
file(GLOB bzip2 src/main/cpp/bzip2_1_0_6/*.c)
#导入新添加的目录，cpp根目录不需要
include_directories(src/main/cpp/bsdiff_4_3)
include_directories(src/main/cpp/bzip2_1_0_6)
#第一个参数 是我们命名的so库名
#第二个参数 固定写法
#第3+参数  需要引入的c和cpp文件，我们上面用变量表示了，可直接写 ${变量名} ,这种写法支持有多个c和cpp文件时简便写法，不用一个一个文件名去添加
add_library( # Sets the name of the library.
             bsdiff
             # Sets the library as a shared library.
             SHARED
             # Provides a relative path to your source file(s).
             ${mainutils}
             ${bsdiff}
             ${bzip2})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
#第一个参数 是我们命名的so库名
target_link_libraries( # Specifies the target library.
                       bsdiff

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
```

修改`MainActivity.kt`：

``` kotlin
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = BsDiffUtils.stringFromJNI()
```

运行一下，看看能不能成功在TextView上显示文字。

稍稍等待之后，报一异常：

![编译异常](https://sogrey.github.io\pics\app_update1.jpg)

看不懂是何原因，我们切换到`Gradle Console`视窗，发现类似如下报错：

![发现异常原因](https://sogrey.github.io\pics\app_update2.jpg)

意思就是在相关文件里发现多处`main()`方法，我们知道`main()`方法是程序入口，只能有一个，检查`.c`和`.cpp`文件把所有的`main()`方法重命名为文件名，再次运行：

![发现异常原因](https://sogrey.github.io\pics\app_update4.jpg)

成功运行没有报错，c环境算是正常了。

搞了这么多，真正的撸码才刚刚开始...



