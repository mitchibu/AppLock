package jp.gr.java_conf.mitchibu.applock;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.socdm.d.adgeneration.ADG;
import com.socdm.d.adgeneration.ADGListener;

public class MainActivity extends AppCompatActivity implements GuardWindow.OnPasscodeListener, GuardWindow.OnCancelListener {
	private Vibrator vibrator;
	private GuardWindow guardWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

		guardWindow = new GuardWindow(getApplicationContext(), Prefs.getLockType(this));
		guardWindow.setType(WindowManager.LayoutParams.TYPE_PHONE);
		guardWindow.setOnPasscodeListener(this);
		guardWindow.setOnCancelListener(this);

		getSupportFragmentManager().beginTransaction().replace(R.id.frag, new MainFragment()).commit();

		final ViewGroup ad_container = (ViewGroup)findViewById(R.id.ad_container);
		ADG adg = new ADG(this);
		adg.setLocationId("10724");
		adg.setAdFrameSize(ADG.AdFrameSize.SP);
		adg.setAdListener(new ADGListener() {
			@Override
			public void onReceiveAd() {
				ad_container.setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailedToReceiveAd() {
				ad_container.setVisibility(View.GONE);
			}
		});
		ad_container.addView(adg);
	}

	@Override
	protected void onResume() {
		super.onResume();
		guardWindow.show();
		guardWindow.setPackageName(getPackageName());
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(guardWindow.isShowing()) guardWindow.dismiss();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		guardWindow.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			getSupportFragmentManager().popBackStack();
			return true;
		}
		return false;
	}

	@Override
	public void onCancel(GuardWindow window) {
		finish();
	}

	@Override
	public void onPasscode(GuardWindow window, String pass) {
		if(pass.equals(Prefs.getPasscode(this))) {
			guardWindow.dismiss();
		} else {
			vibrator.vibrate(100);
		}
	}
}
