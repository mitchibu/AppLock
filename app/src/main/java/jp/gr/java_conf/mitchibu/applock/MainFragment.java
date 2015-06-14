package jp.gr.java_conf.mitchibu.applock;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<ResolveInfo>> {
	private final List<ResolveInfo> list = new ArrayList<>();
	private final Adapter adapter = new Adapter();

	private PackageManager pm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		pm = getActivity().getPackageManager();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		RecyclerView recyclerView = (RecyclerView)view.findViewById(android.R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, getArguments(), this).forceLoad();
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.app_name);
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			getFragmentManager().beginTransaction().replace(getId(), new SettingsFragment()).addToBackStack(null).commit();
			return true;
		default:
			return false;
		}
	}

	@Override
	public Loader<List<ResolveInfo>> onCreateLoader(int id, Bundle args) {
		return new AsyncTaskLoader<List<ResolveInfo>>(getActivity()) {
			@Override
			public List<ResolveInfo> loadInBackground() {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
				for(ResolveInfo info : list) {
					if(info.activityInfo.packageName.equals(getActivity().getPackageName())) {
						list.remove(info);
					}
				}
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

	class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_switch, viewGroup, false));
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, int position) {
			viewHolder.info = list.get(position);

			viewHolder.text.setText(viewHolder.info.loadLabel(pm));
			Drawable icon = viewHolder.info.loadIcon(pm);
			int size = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
			icon.setBounds(0, 0, size, size);
			viewHolder.text.setCompoundDrawables(icon, null, null, null);

			Boolean locked = Prefs.isLockedPackage(getActivity(), viewHolder.info.activityInfo.packageName);
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
						Prefs.setLockedPackage(getActivity(), info.activityInfo.packageName, isChecked);
						if(Prefs.hasLockedPackage(getActivity())) MainService.start(getActivity());
						else MainService.stop(getActivity());
					}
				});
			}
		}
	}
}
