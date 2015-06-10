package jp.gr.java_conf.mitchibu.applock;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<ResolveInfo>>, GuardWindow.OnPasscodeListener, GuardWindow.OnCancelListener {
	private final List<ResolveInfo> list = new ArrayList<>();
	private final Adapter adapter = new Adapter();

	private PackageManager pm;
	private GuardWindow guardWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pm = getPackageManager();

		guardWindow = new GuardWindow(this);
		guardWindow.setOnPasscodeListener(this);
		guardWindow.setOnCancelListener(this);
		guardWindow.show();
		guardWindow.setPackageName(getPackageName());

		RecyclerView recyclerView = (RecyclerView)findViewById(android.R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(adapter);

		getSupportLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(guardWindow.isShowing()) guardWindow.dismiss();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		guardWindow.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			return true;
		default:
			return false;
		}
	}

	@Override
	public Loader<List<ResolveInfo>> onCreateLoader(int id, Bundle args) {
		return new AsyncTaskLoader<List<ResolveInfo>>(this) {
			@Override
			public List<ResolveInfo> loadInBackground() {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
				Collections.sort(list, new Comparator<ResolveInfo>() {
					@Override
					public int compare(ResolveInfo arg0, ResolveInfo arg1) {
						return arg0.loadLabel(pm).toString().compareTo(arg1.loadLabel(pm).toString());
					}
				});
				return list;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<List<ResolveInfo>> loader, List<ResolveInfo> data) {
		list.clear();
		list.addAll(data);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<List<ResolveInfo>> loader) {
		list.clear();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onCancel(GuardWindow window) {
		finish();
	}

	@Override
	public boolean onPasscode(GuardWindow window, CharSequence pass) {
		boolean rc = pass.toString().equals(Prefs.getPasscode(this));
		if(rc) {
			guardWindow.dismiss();
		}
		return rc;
	}

	class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			return new ViewHolder(getLayoutInflater().inflate(R.layout.item_switch, viewGroup, false));
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, int position) {
			viewHolder.info = list.get(position);

			viewHolder.text.setText(viewHolder.info.loadLabel(pm));
			Drawable icon = viewHolder.info.loadIcon(pm);
			int size = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
			icon.setBounds(0, 0, size, size);
			viewHolder.text.setCompoundDrawables(icon, null, null, null);

			Boolean locked = Prefs.isLockedPackage(MainActivity.this, viewHolder.info.activityInfo.packageName);
			if(locked == null) locked = false;
			viewHolder.text.setChecked(locked);
		}

		@Override
		public int getItemCount() {
			return list.size();
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			public final SwitchCompat text;

			public ResolveInfo info;

			public ViewHolder(final View itemView) {
				super(itemView);
				text = (SwitchCompat)itemView.findViewById(android.R.id.text1);
				text.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Prefs.setLockedPackage(MainActivity.this, info.activityInfo.packageName, isChecked);
						if(Prefs.hasLockedPackage(MainActivity.this)) MainService.start(MainActivity.this);
						else MainService.stop(MainActivity.this);
					}
				});
			}
		}
	}
}
