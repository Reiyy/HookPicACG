package com.yareiy.hookmypica;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Dialog;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.MalformedURLException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import org.json.JSONObject;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

@SuppressWarnings("RedundantThrows")
public class MainHook implements IXposedHookLoadPackage {


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.picacomic.picacg.picacg")) return;
        XposedBridge.log("handleLoadPackage: " + lpparam.processName + ", " + lpparam.processName);


        // 定义 URL 替换映射表
        final Map<String, String> urlMap = new HashMap<>();
        urlMap.put("https://cloudflare-dns.com", "https://picaapi.reiyy.com:2333");
        urlMap.put("https://picaapi.picacomic.com", "https://picaapi.reiyy.com:2333");

        // Hook URL.openConnection() 方法
        XposedHelpers.findAndHookMethod("java.net.URL", lpparam.classLoader, "openConnection",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        URL oldUrl = (URL) param.thisObject;
                        String oldUrlString = oldUrl.toString();
                        String newUrlString = oldUrlString;

                        // 遍历替换规则
                        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
                            if (oldUrlString.startsWith(entry.getKey())) {
                                newUrlString = oldUrlString.replace(entry.getKey(), entry.getValue());
                                break; // 找到匹配项后跳出
                            }
                        }

                        // 如果 URL 发生了变化，则替换
                        if (!oldUrlString.equals(newUrlString)) {
                            URL newUrl = new URL(newUrlString);
                            param.setResult(newUrl.openConnection());
                            XposedBridge.log("Modified URL: " + oldUrlString + " -> " + newUrlString);
                        }
                    } catch (Exception e) {
                        XposedBridge.log("Error modifying URL: " + e.getMessage());
                    }
                }
            });



 
    }

 
}


    

