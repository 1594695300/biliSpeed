package com.veo.hook.bili.speed;

import android.os.Message;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    public final static String hookPackageWx = "com.tencent.mm";
    private final static XSharedPreferences prefs = new XSharedPreferences("com.veo.hook.bili.speed", "speed");

    private static float getSpeedConfig() {
        prefs.reload();
        return prefs.getFloat("speed", 1.5f);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!hookPackageWx.equals(lpparam.packageName)) return;
        if (!hookPackageWx.equals(lpparam.processName)) return;

        XposedHelpers.findAndHookMethod(
                "com.tencent.mm.plugin.finder.video.FinderThumbPlayerProxy",
                lpparam.classLoader,
                "setPlaySpeed",
                float.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        float speed = (float) param.args[0];
                        if (speed == 1.0f) {
                            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                            for (int i = 4; i <= 10 && i < stackTraceElements.length; i++) {
                                if ("com.tencent.mm.plugin.finder.video.FinderVideoLayout".equals(stackTraceElements[i].getClassName())) {
                                    param.args[0] = getSpeedConfig();
                                    XposedBridge.log("wx speed set");
                                    return;
                                }
                            }
                        }
                    }
                });
        XposedBridge.log("hooked wx setPlaySpeed");
    }
}
