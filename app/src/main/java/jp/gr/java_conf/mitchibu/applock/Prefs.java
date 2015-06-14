package jp.gr.java_conf.mitchibu.applock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Map;

public class Prefs {
	private static final String KEY_PASSCODE = Prefs.class.getName() + ".key.PASSCODE";
	private static final String KEY_ALLOWED_PACKAGE_NAME = Prefs.class.getName() + ".key.ALLOWED_PACKAGE_NAME";

	public static String getPasscode(Context context) {
		String key = context.getString(R.string.key_password);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "0000");
	}

	@SuppressLint("CommitPrefEdits")
	public static void setAllowedPackageName(Context context, String packageName) {
		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		if(TextUtils.isEmpty(packageName)) editor.remove(KEY_ALLOWED_PACKAGE_NAME);
		else editor.putString(KEY_ALLOWED_PACKAGE_NAME, packageName);
		editor.commit();
	}

	public static String getAllowedPackageName(Context context) {
		return getSharedPreferences(context).getString(KEY_ALLOWED_PACKAGE_NAME, null);
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
