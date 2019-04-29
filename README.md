# APPAutoUpdate
App自动更新

[![CodeFactor](https://www.codefactor.io/repository/github/sogrey/appautoupdate/badge)](https://www.codefactor.io/repository/github/sogrey/appautoupdate)


查看文件md5：

windows:
``` bash
# 查看MD5值：
certutil -hashfile 文件名  MD5
# 查看 SHA1
certutil -hashfile 文件名  SHA1 
# 查看SHA256
certutil -hashfile 文件名  SHA256
```
例如：
``` bash
C:\Users\Administrator\Desktop\bsdiffApks>certutil -hashfile newApk.apk MD5
MD5 的 newApk.apk 哈希:
3e7cd10e3dfaedc13ff9c35dc7a823e3
CertUtil: -hashfile 命令成功完成。

C:\Users\Administrator\Desktop\bsdiffApks>certutil -hashfile newApk2.apk MD5
MD5 的 newApk2.apk 哈希:
3e7cd10e3dfaedc13ff9c35dc7a823e3
CertUtil: -hashfile 命令成功完成。
```
linux:
``` bash
md5sum 文件名
```

