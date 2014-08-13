package com.globant.labs.swipper;

import java.util.ArrayList;
import java.util.List;

import com.globant.labs.swipper.comps.ArrayAdapterSearchView;
import com.globant.labs.swipper.comps.ConfirmationDialog;
import com.globant.labs.swipper.drawercomps.CustomDrawerAdapter;
import com.globant.labs.swipper.drawercomps.DrawerItem;
import com.globant.labs.swipper.fragments.sections.SectionsPagerAdapter;
import com.globant.labs.swipper.net.ImageLoader;
import com.globant.labs.swipper.utils.AppRater;
import com.globant.labs.swipper.utils.Utils;
import com.globant.labs.swipper.R;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class HomeActivity extends ActionBarCustomActivity implements
		ActionBar.TabListener {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CustomDrawerAdapter adapter;
	private List<DrawerItem> dataList;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	private String global_variable;

	protected Dialog currentDialog;

	private static HomeActivity instance;

	private ImageLoader imageLoaderService;
	private ArrayAdapterSearchView searchView;
	private MenuItem itemSearch;
	private boolean canShowSearch;

	public static HomeActivity getInstance() {
		return instance;
	}

	public HomeActivity() {
		super();
		instance = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		instance = this;

		setContentView(R.layout.activity_home);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();

		AppRater.app_launched(this);

		// initialize tthe image loader service
		// ImageLoader class instance
		imageLoaderService = ImageLoader.getInstance(getApplicationContext());

		// Initializing
		dataList = new ArrayList<DrawerItem>();

		// dataList.add(new DrawerItem("Menu")); // adding a header to the list
		dataList.add(new DrawerItem(getString(R.string.identificar),
				R.drawable.ic_action_target));
		dataList.add(new DrawerItem(getString(R.string.map),
				R.drawable.ic_action_map));
		dataList.add(new DrawerItem(getString(R.string.autores),
				R.drawable.ic_action_usr));
		dataList.add(new DrawerItem(getString(R.string.contacto_menu),
				R.drawable.ic_action_contacto));
		dataList.add(new DrawerItem(getString(R.string.about),
				R.drawable.ic_action_about));

		adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
				dataList);

		// mPlanetTitles = getResources().getStringArray(R.array.planets_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_action_settings, /*
										 * nav drawer image to replace 'Up'
										 * caret
										 */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu();
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			}

			public void onDrawerOpened(View drawerView) {
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		actionBar.setCustomView(R.layout.actionbar_custom_view_home);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(), this, imageLoaderService);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	public static void showHome(Context home) {
		Intent intent = new Intent(home, HomeActivity.class);
		home.startActivity(intent);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// la lupa de buscar
		menu.findItem(R.id.action_search).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	public ImageLoader getImageLoaderService() {
		return imageLoaderService;
	}

	public void setImageLoaderService(ImageLoader imageLoaderService) {
		this.imageLoaderService = imageLoaderService;
	}

	public ArrayAdapterSearchView getSearchView() {
		return searchView;
	}

	public boolean isCanShowSearch() {
		return canShowSearch;
	}

	public void setCanShowSearch(boolean canShowSearch) {
		this.canShowSearch = canShowSearch;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);

		// Create the search view
		searchView = new ArrayAdapterSearchView(getSupportActionBar()
				.getThemedContext());
		searchView.setQueryHint("Buscar Eventos");

		itemSearch = menu.add(Menu.NONE, Menu.NONE, 1, "Buscar");
		itemSearch.setVisible(false);
		itemSearch
				.setIcon(R.drawable.ic_search)
				.setActionView(searchView)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_ALWAYS
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		searchView
				.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View view,
							boolean queryTextFocused) {
						if (!queryTextFocused) {
							disableSearchView();
						}
					}
				});

		

		return true;
	}
	
	@Override
	public void onStop(){
		super.onStop();
	}

	
	public MenuItem getItemSearch() {
		return itemSearch;
	}


	public void disableSearchView() {
		itemSearch.collapseActionView();
		searchView.setQuery("", false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);
			return true;
		}

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		// To-DO ver aca
		if (id == R.id.action_search) {
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			String shareBody = "Buscala en el Market! http://goo.gl/yxLS47 Te copas? #codigociudadano #resistenciarte";
			sharingIntent
					.putExtra(android.content.Intent.EXTRA_SUBJECT,
							"La mejor forma de encontrar esculturas en Resistencia! Usa esta app!!");
			sharingIntent
					.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent,
					"Compartilo en..."));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		if (itemSearch != null) {
			itemSearch.setVisible(tab.getPosition() == 1 && canShowSearch);
			if (tab.getPosition() != 1) {
				disableSearchView();
			}
		}
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	public boolean checkOpenGL() {
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager
				.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
		if (supportsEs2) {
			return true;
		} else {
			Toast.makeText(this,
					"Tu cel no soporta OpenGl, Este modulo no funciona! :S",
					Toast.LENGTH_LONG).show();
			return false;
		}
	}

	private void selectItem(int position) {

		switch (position) {
		case 0:
			NearbyLocations.showHome(this);
			break;
		case 1:
			if (checkOpenGL()) {
				MapActivity.showHome(this);
			}
			break;
		case 2:
			AutoresActivity.showHome(this);
			break;
		case 3:
			ContactoActivity.showHome(this);
			break;
		case 4:
			AboutActivity.showHome(this);
			break;
		}

		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public Dialog onCreateDialog(int id) {

		final Context context = this;
		switch (id) {
		case Utils.GPS_NOT_TURNED_ON:
			currentDialog = ConfirmationDialog.create(this, id,
					R.string.gps_not_turned_on,
					getString(R.string.gps_not_turned_on_details),
					R.string.confirm, new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					});
			return currentDialog;
		case Utils.GPS_NOT_TURNED_FF:
			currentDialog = ConfirmationDialog.create(this, id,
					R.string.gps_not_turned_on_yet,
					getString(R.string.gps_not_turned_on_details),
					R.string.confirm, R.string.cancel, new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					}, new Runnable() {
						@Override
						public void run() {
							HomeActivity.showHome(context);
						}
					});
			return currentDialog;
		}

		return super.onCreateDialog(id);
	}

}
