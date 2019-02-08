# Chapter22
今天我们尝试使用facebook redex库来优化我们的安装包。

不过个人感觉redex太过于复杂了，内部还耦合着很多Facebook特有的逻辑。

个人更加建议掌握原理之后，自己实现一套轻量简单的。

安装redex
=======
在 github checkout [redex](https://github.com/facebook/redex)的源码，

根据readme完成编译：
```
git clone https://github.com/facebook/redex.git
cd redex

autoreconf -ivf && ./configure && make -j4
sudo make install
```

编译RedexSample
=============
RedexSample通过引入一大堆开源库，尝试把dex数量变多一些。直接编译即可
```
assembleDebug
```

通过redex命令优化
===========

1. 为了可以更加清楚流程，可以输出redex的日志
```
export TRACE=2
```

2. 去除debuginfo的方法
```
redex --sign -s ReDexSample/keystore/debug.keystore -a androiddebugkey -p android -c redex-test/stripdebuginfo.config -P ReDexSample/proguard-rules.pro  -o redex-test/strip_output.apk ReDexSample/build/outputs/apk/debug/ReDexSample-debug.apk

```

可以看到这样的日志
```
Running StripDebugInfoPass...
matched on 105417 methods. Removed 0 dbg line entries, 103172 dbg local var entries, 0 dbg prologue start entries, 0 epilogue end entries and 14107 empty dbg tables.
        StripDebugInfoPass (run) completed in 0.1 seconds

```

再次安装查看是否堆栈行号还存在
```
2019-02-08 23:52:20.763 13117-13117/com.sample.redex E/test: java.lang.Throwable
        at com.sample.redex.MainActivity.onCreate(MainActivity.java:20)
        at android.app.Activity.performCreate(Activity.java:7436)
        at android.app.Activity.performCreate(Activity.java:7426)
        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1286)
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3279)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3484)
        at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:86)
        at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:108)
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:68)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2123)
        at android.os.Handler.dispatchMessage(Handler.java:109)
        at android.os.Looper.loop(Looper.java:207)
        at android.app.ActivityThread.main(ActivityThread.java:7470)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:524)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:958)

```

3. Dex重分包的方法
```
redex --sign -s ReDexSample/keystore/debug.keystore -a androiddebugkey -p android -c redex-test/interdex.config -P ReDexSample/proguard-rules.pro  -o redex-test/interdex_output.apk ReDexSample/build/outputs/apk/debug/ReDexSample-debug.apk

```

需要注意的是，redex不会处理第一个dex，因为第一个dex是需要maindex pattern来指定的。会有以下的日志：

```
Running InterDexPass...
[primary dex]: 0 out of 228 classes in primary dex from interdex list.
[primary dex]: 0 out of 228 classes in primary dex skipped from interdex list.
Writing out primary dex with 228 classes.
No interdex classes passed.
[dex ordering] Cross-dex-ref-minimizer active with method ref weight 100, field ref weight 90, type ref weight 100, string ref weight 90.
Writing out secondary dex number 1, which is not part of of coldstart, not part of of extended set, doesn't have scroll classes and has 6511 classes.
Writing out secondary dex number 2, which is not part of of coldstart, not part of of extended set, doesn't have scroll classes and has 7566 classes.
Writing out secondary dex number 3, which is not part of of coldstart, not part of of extended set, doesn't have scroll classes and has 19 classes.

```

然后查看原包与生成的interdex_output.apk的分包差异，个人认为redex的分包计算方法写的也是有那么一点问题，但是我们可以参考这个思路。