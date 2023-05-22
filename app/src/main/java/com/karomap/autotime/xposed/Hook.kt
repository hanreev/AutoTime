package com.karomap.autotime.xposed

import android.content.ContentResolver
import android.provider.Settings
import android.provider.Settings.Global.AUTO_TIME
import android.provider.Settings.Global.AUTO_TIME_ZONE
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.karomap.autotime.BuildConfig

@Keep
class Hook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName.startsWith("android") || lpparam.packageName.startsWith(
                "com.android"
            )
        ) {
            return
        }

        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(
                "com.karomap.autotime.xposed.ModuleStatusKt",
                lpparam.classLoader,
                "isModuleActive",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                })
        }

        val preferences = XSharedPreferences(BuildConfig.APPLICATION_ID)

        XposedHelpers.findAndHookMethod(
            Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    applyBeforeHookedMethod(preferences, param)
                }
            })

        XposedHelpers.findAndHookMethod(
            Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    applyBeforeHookedMethod(preferences, param)
                }
            })
    }

    private fun applyBeforeHookedMethod(
        preferences: XSharedPreferences,
        param: XC_MethodHook.MethodHookParam
    ) {
        preferences.reload()
        when {
            preferences.getBoolean(
                AUTO_TIME,
                true
            ) && param.args[1] == AUTO_TIME -> {
                param.result = 1
            }

            preferences.getBoolean(
                AUTO_TIME_ZONE,
                true
            ) && param.args[1] == AUTO_TIME_ZONE -> {
                param.result = 1
            }
        }

    }
}