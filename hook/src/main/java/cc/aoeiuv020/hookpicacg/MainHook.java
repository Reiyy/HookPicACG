package cc.aoeiuv020.hookpicacg;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("RedundantThrows")
public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("handleLoadPackage: " + lpparam.processName + ", " + lpparam.processName);
        XposedHelpers.findAndHookMethod(
                "com.picacomic.fregata.utils.views.PopupWebview",
                lpparam.classLoader,
                "init",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("beforeHookedMethod: PopupWebview.init(Context)");
                        param.setResult(null);
                    }
                });
        XposedHelpers.findAndHookMethod(
                "com.picacomic.fregata.utils.views.BannerWebview",
                lpparam.classLoader,
                "init",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("beforeHookedMethod: BannerWebview.init(Context)");
                        param.setResult(null);
                    }
                });
        XposedHelpers.findAndHookMethod(
                "com.picacomic.fregata.activities.MainActivity",
                lpparam.classLoader,
                "onCreate",
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("afterHookedMethod: MainActivity.onCreate(Bundle)");
                        View[] buttons_tabbar = (View[]) XposedHelpers.getObjectField(param.thisObject, "buttons_tabbar");
                        buttons_tabbar[2].setVisibility(View.GONE);
                    }
                });
        XposedHelpers.findAndHookMethod(
                "com.picacomic.fregata.adapters.ComicPageRecyclerViewAdapter",
                lpparam.classLoader,
                "onCreateViewHolder",
                ViewGroup.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("afterHookedMethod: ComicPageRecyclerViewAdapter.onCreateViewHolder");
                        Object result = param.getResult();
                        if (!TextUtils.equals(result.getClass().getName(), "com.picacomic.fregata.holders.AdvertisementListViewHolder")) {
                            return;
                        }
                        View webView_ads = (View) XposedHelpers.getObjectField(result, "itemView");
                        webView_ads.setVisibility(View.GONE);
                        Object lp = XposedHelpers.newInstance(XposedHelpers.findClass("android.support.v7.widget.RecyclerView$LayoutParams", lpparam.classLoader), 0, 0);
                        webView_ads.setLayoutParams((ViewGroup.LayoutParams) lp);
                    }
                });
        XposedHelpers.findAndHookMethod("com.picacomic.fregata.adapters.ComicListRecyclerViewAdapter", lpparam.classLoader, "onBindViewHolder", XposedHelpers.findClass("android.support.v7.widget.RecyclerView$ViewHolder", lpparam.classLoader), int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object viewHolder = param.args[0];
                if (viewHolder.getClass().getSimpleName().equals("AdvertisementListViewHolder")) {
                    param.setResult(null);
                    View itemView = (View) XposedHelpers.getObjectField(viewHolder, "itemView");
                    ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) itemView.getLayoutParams();
                    // 完全隐藏会影响分页加载的逻辑，所以保留一点，
                    lp.height = 1;
                    itemView.setLayoutParams(lp);
                }
            }
        });
        XposedHelpers.findAndHookMethod(
            "com.picacomic.fregata.fragments.CategoryFragment",
            lpparam.classLoader,
            "ci",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // 获取 kD 列表
                    Object kD = XposedHelpers.getObjectField(param.thisObject, "kD");

                    if (kD instanceof java.util.List) {
                        // 使用原始类型 List
                        java.util.List kDList = (java.util.List) kD;

                        // 清空列表以重新添加分类
                        kDList.clear();

                        // 遍历 0 到 7 的分类索引
                        for (int i = 0; i < 8; i++) {
                            // 跳过 case 3
                            if (i == 3) {
                                continue;
                            }

                            // 反射创建 DefaultCategoryObject
                            Class<?> defaultCategoryObjectClass = Class.forName("com.picacomic.fregata.objects.DefaultCategoryObject");
                            Object defaultCategoryObject = defaultCategoryObjectClass.getConstructor(String.class, String.class, String.class, int.class)
                                    .newInstance("", invokeGetString(param.thisObject, "category_title_" + getCategoryName(i)),
                                            invokeGetString(param.thisObject, "category_title_" + getCategoryName(i)),
                                            invokeGetDrawableId(i));

                            // 添加到 kD 列表
                            kDList.add(defaultCategoryObject);
                        }
                    }
                }

                private String getCategoryName(int i) {
                    switch (i) {
                        case 0: return "support";
                        case 1: return "leaderboard";
                        case 2: return "game";
                        // case 3 被跳过
                        case 4: return "love_pica";
                        case 5: return "pica_forum";
                        case 6: return "latest";
                        case 7: return "random";
                        default: return "";
                    }
                }

                private int invokeGetDrawableId(int i) throws Exception {
                    // 通过反射获取 Drawable ID
                    String drawableName = "cat_" + getCategoryName(i);
                    return (int) Class.forName("com.picacomic.fregata.R$drawable").getField(drawableName).get(null);
                }

                private String invokeGetString(Object context, String resName) throws Exception {
                    return (String) context.getClass().getMethod("getString", int.class)
                            .invoke(context, Class.forName("com.picacomic.fregata.R$string").getField(resName).getInt(null));
                }
            });
    }
}
