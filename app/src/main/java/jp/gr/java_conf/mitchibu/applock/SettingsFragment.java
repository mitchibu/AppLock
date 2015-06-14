package jp.gr.java_conf.mitchibu.applock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.machinarius.preferencefragment.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.action_settings);
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
