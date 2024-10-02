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
                        Object kD = XposedHelpers.getObjectField(param.thisObject, "kD");
                        if (kD != null) {
                            XposedHelpers.callMethod(kD, "clear"); // 清空列表，防止重复添加
                            for (int i = 0; i < 8; i++) {
                                if (i == 3) {
                                    continue; // 跳过 ads 分类
                                }

                                // 使用反射创建 DefaultCategoryObject 实例
                                Class<?> defaultCategoryClass = XposedHelpers.findClass("com.picacomic.fregata.models.DefaultCategoryObject", lpparam.classLoader);
                                String title = (String) XposedHelpers.callMethod(param.thisObject, "getString", getStringResourceId(i, lpparam.classLoader)); // 获取对应字符串资源ID
                                Object defaultCategoryObject = XposedHelpers.newInstance(defaultCategoryClass, "", title, title, getDrawableResourceId(i, lpparam.classLoader));
                                
                                XposedHelpers.callMethod(kD, "add", defaultCategoryObject); // 使用反射添加
                            }
                        }
                    }

                    private int getStringResourceId(int index, ClassLoader classLoader) {
                        try {
                            return (int) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.picacomic.fregata.R$string", classLoader), "category_title_" + (index == 0 ? "support" : index == 1 ? "leaderboard" : index == 2 ? "game" : index == 4 ? "love_pica" : index == 5 ? "pica_forum" : index == 6 ? "latest" : index == 7 ? "random" : ""));
                        } catch (Throwable t) {
                            return 0; // 返回默认值
                        }
                    }

                    private int getDrawableResourceId(int index, ClassLoader classLoader) {
                        try {
                            return (int) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.picacomic.fregata.R$drawable", classLoader), "cat_" + (index == 0 ? "support" : index == 1 ? "leaderboard" : index == 2 ? "game" : index == 4 ? "love_pica" : index == 5 ? "forum" : index == 6 ? "latest" : index == 7 ? "random" : ""));
                        } catch (Throwable t) {
                            return 0; // 返回默认值
                        }
                    }
                });
    }
}
