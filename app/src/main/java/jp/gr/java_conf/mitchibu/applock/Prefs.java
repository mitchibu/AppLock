package jp.gr.java_conf.mitchibu.applock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Map;

public class Prefs {
	private static final String KEY_LAST_PACKAGE_NAME = Prefs.class.getName() + ".key.LAST_PACKAGE_NAME";

	@SuppressLint("CommitPrefEdits")
	public static void setLastPackageName(Context context, String packageName) {
		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		if(TextUtils.isEmpty(packageName)) editor.remove(KEY_LAST_PACKAGE_NAME);
		else editor.putString(KEY_LAST_PACKAGE_NAME, packageName);
		editor.commit();
	}

	public static String getLastPackageName(Context context) {
		return getSharedPreferences(context).getString(KEY_LAST_PACKAGE_NAME, null);
	}

	public static boolean hasLockedPackage(Context context) {
		Map<String, ?> map = getSharedPreferences(context).getAll();
		return map.containsValue(true);
	}

	public static Boolean isLockedPackage(Context context, String packageName) {
		SharedPreferences sp = getSharedPreferences(context);
		if(!sp.contains(packageName)) return null;
		return sp.getBoolean(packageName, false);
	}

	public static void setLockedPackage(Context context, String packageName, boolean locked) {
		getSharedPreferences(context).edit().putBoolean(packageName, locked).commit();
	}

	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
	}
}
