package com.ncuculova.taximk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    TaxiAdapter mTaxiAdapter;
    ProgressBar mProgressBar;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    String mActivityTitle;
    CityAdapter mCityAdapter;
    SwipeMenuListView mListTaxi;
    EditTaxis mEditTaxis;
    City mSelectedCity;
    MenuItem mCancelItem;
    FloatingActionButton mBtn;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Intent mService;
    IntentFilter mIntentFilter;
    SharedPreferences mSettings;
    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditorSettings;
    EnableNetworkDialog mEnableNetworkDialog;
    Intent dial;
    int selectedIndex;
    boolean favouriteMode;
    boolean isReturnedFromSettings;
    boolean isOpened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mListTaxi = (SwipeMenuListView) findViewById(R.id.listView);
        mListTaxi.setOnTouchListener(mOnTouchListener);
        mListTaxi.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                mSwipeRefreshLayout.setEnabled(false);
            }

            @Override
            public void onSwipeEnd(int position) {
            }
        });
        mListTaxi.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
                isOpened = true;
            }

            @Override
            public void onMenuClose(int position) {
                isOpened = false;
                mSwipeRefreshLayout.setEnabled(true);
            }
        });
        mListTaxi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListTaxi.smoothOpenMenu(position);
            }
        });
        mTaxiAdapter = new TaxiAdapter(this);
        mListTaxi.setAdapter(mTaxiAdapter);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        mCityAdapter = new CityAdapter(this);
        mDrawerList.setAdapter(mCityAdapter);
        favouriteMode = false;
        mCancelItem = null;
        selectedIndex = -1;
        mEnableNetworkDialog = new EnableNetworkDialog();
        mEnableNetworkDialog.setOnDialogClickListener(this);
        mService = new Intent(this, DownloadTaxiService.class);
        mIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        // Instantiates a new DownloadStateReceiver
        DownloadStateReceiver mDownloadStateReceiver =
                new DownloadStateReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadStateReceiver,
                mIntentFilter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isOnline(getApplicationContext())) {
                    CheckTaxiVersion checkTaxiVersion = new CheckTaxiVersion(getApplicationContext());
                    checkTaxiVersion.execute();
                    checkTaxiVersion.setOnVersionChecked(mOnVersionChecked);
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mEnableNetworkDialog.show(getSupportFragmentManager(), "enable_network");
                }
            }
        });

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setCityFilteredData(id);
                mSelectedCity = (City) mCityAdapter.getItem(position);
                selectedIndex = position;
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        mSettings = getPreferences(MODE_PRIVATE);
        mEditorSettings = mSettings.edit();
        FetchTaxis fetchTaxis = new FetchTaxis(this);
        fetchTaxis.setmOnFetchedTaxis(mOnFetchedTaxis);

        boolean saved = mSettings.getBoolean("saved", false);
        if (!saved) {
            if (isOnline(this)) {
                CheckTaxiVersion checkTaxiVersion = new CheckTaxiVersion(getApplicationContext());
                checkTaxiVersion.setOnVersionChecked(mOnVersionChecked);
                checkTaxiVersion.execute();
                startService(mService);
            } else {
                mEnableNetworkDialog.show(getSupportFragmentManager(), "enable_network");
            }
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            fetchTaxis.execute();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setupDrawer();

        //swipe menu creator to add items
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                // create "call" item
                SwipeMenuItem phone1 = new SwipeMenuItem(
                        getApplicationContext());
                SwipeMenuItem phone2 = new SwipeMenuItem(
                        getApplicationContext());
                SwipeMenuItem favItem = new SwipeMenuItem(
                        getApplicationContext());
                favItem.setBackground(new ColorDrawable(Color.BLACK));
                favItem.setWidth(width / 4);
                // set item background
                phone1.setBackground(new ColorDrawable(Color.BLACK));
                phone2.setBackground(new ColorDrawable(Color.BLACK));
                phone1.setWidth(width / 4);
                phone2.setWidth(width / 4);
                if (menu.getViewType() == TaxiAdapter.ViewType.MOBILE_MOBILE_ES.ordinal()) {
                    phone1.setIcon(R.drawable.ic_action_phone);
                    phone2.setIcon(R.drawable.ic_action_phone);
                    favItem.setIcon(R.drawable.ic_action_star_0);
                } else if (menu.getViewType() == TaxiAdapter.ViewType.MOBILE_PHONE_ES.ordinal()) {
                    phone1.setIcon(R.drawable.ic_action_phone);
                    phone2.setIcon(R.drawable.ic_action_phone_start);
                    favItem.setIcon(R.drawable.ic_action_star_0);
                } else if (menu.getViewType() == TaxiAdapter.ViewType.PHONE_MOBILE_ES.ordinal()) {
                    phone1.setIcon(R.drawable.ic_action_phone_start);
                    phone2.setIcon(R.drawable.ic_action_phone);
                    favItem.setIcon(R.drawable.ic_action_star_0);
                } else if (menu.getViewType() == TaxiAdapter.ViewType.PHONE_PHONE_ES.ordinal()) {
                    phone1.setIcon(R.drawable.ic_action_phone_start);
                    phone2.setIcon(R.drawable.ic_action_phone_start);
                    favItem.setIcon(R.drawable.ic_action_star_0);
                } else if (menu.getViewType() == TaxiAdapter.ViewType.MOBILE_MOBILE_FS.ordinal()) {
                    phone1.setIcon(R.drawable.ic_action_phone);
                    phone2.setIcon(R.drawable.ic_action_phone);
                    favItem.setIcon(R.drawable.ic_action_star_10);
                } else if (menu.getViewType() == TaxiAdapter.ViewType.MOBILE_PHONE_FS.ordinal()) {
                    phone1.setIcon(R.drawable.ic_action_phone);
                    phone2.setIcon(R.drawable.ic_action_phone_start);
                    favItem.setIcon(R.drawable.ic_action_star_10);
                } else if (menu.getViewType() == TaxiAdapter.ViewType.PHONE_MOBILE_FS.ordinal()) {
                    phone1.setIcon(R.drawable.ic_action_phone_start);
                    phone2.setIcon(R.drawable.ic_action_phone);
                    favItem.setIcon(R.drawable.ic_action_star_10);
                } else if (menu.getViewType() == TaxiAdapter.ViewType.PHONE_PHONE_FS.ordinal()) {
                    phone1.setIcon(R.drawable.ic_action_phone_start);
                    phone2.setIcon(R.drawable.ic_action_phone_start);
                    favItem.setIcon(R.drawable.ic_action_star_10);
                }
                // add to menu
                menu.addMenuItem(phone1);
                menu.addMenuItem(phone2);
                menu.addMenuItem(favItem);
            }
        };

        // set creator
        mListTaxi.setMenuCreator(creator);
        mListTaxi.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Taxi taxi = mTaxiAdapter.getTaxi(position);
                checkUserCallPreferences();
                switch (index) {
                    case 0: {
                        dial.setData(Uri.parse("tel:" + taxi.getPhone()));
                        startActivity(dial);
                        break;
                    }
                    case 1: {
                        if (Taxi.isEmpty(taxi.getPhone2()))
                            dial.setData(Uri.parse("tel:" + taxi.getPhone()));
                        else
                            dial.setData(Uri.parse("tel:" + taxi.getPhone2()));
                        startActivity(dial);
                        break;
                    }
                    case 2: {
                        taxi.setIsFav(!taxi.isFav());
                        mTaxiAdapter.setFavoriteTaxis();
                        mTaxiAdapter.notifyDataSetChanged();
                        mEditTaxis = new EditTaxis(getApplicationContext());
                        mEditTaxis.execute(taxi);
                        break;
                    }
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        // Right
        mListTaxi.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);

    }

    View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (isOpened) {
                mSwipeRefreshLayout.setEnabled(false);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mSwipeRefreshLayout.setEnabled(true);
                }
            }
            return false;
        }
    };

    public void setCityFilteredData(long id) {
        if (favouriteMode) {
            mTaxiAdapter.setFavModeOff();
            rotateBtnBack(mBtn);
        }
        mTaxiAdapter.setFilteredTaxis(id);
        mTaxiAdapter.setFilter();
        mTaxiAdapter.notifyDataSetChanged();
    }

    private final OnFetchedTaxis mOnFetchedTaxis = new OnFetchedTaxis() {
        @Override
        public void onFetched(Data result) {
            mTaxiAdapter.setTaxis(result.getTaxis());
            mTaxiAdapter.notifyDataSetChanged();
            List<City> cities = new ArrayList<>(result.getCities());
            mCityAdapter.setCities(cities);
            mCityAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            if (checkUserRememberPreferences()) {
                selectedIndex = mSettings.getInt("cityFilter", -1);
                if (selectedIndex != -1) {
                    mSelectedCity = (City) mCityAdapter.getItem(selectedIndex);
                    setCityFilteredData(mSelectedCity.getId());
                    getSupportActionBar().setTitle(mSelectedCity.getName());
                    mCancelItem.setVisible(true);
                }
            }
        }
    };

    private final OnVersionChecked mOnVersionChecked = new OnVersionChecked() {
        @Override
        public void onChecked(Integer result) {
            int ver = mSettings.getInt("version", 0);
            if (result != null && result > ver) {
                startService(mService);
                mEditorSettings.putInt("version", result);
                mEditorSettings.commit();
            } else {
                mSwipeRefreshLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Нема нови податоци", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("TaxiMK");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (mSelectedCity != null) {
                    getSupportActionBar().setTitle(mSelectedCity.getName());
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        mCancelItem = menu.getItem(1);
        if (mSelectedCity != null) {
            mCancelItem.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            // Display the fragment as the main content.
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }
        if (item.getItemId() == R.id.action_cancel) {
            mSelectedCity = null;
            mTaxiAdapter.clearFilter();
            mTaxiAdapter.notifyDataSetChanged();
            getSupportActionBar().setTitle(getString(R.string.all_companies));
            item.setVisible(false);
            mDrawerList.setItemChecked(-1, true);
            selectedIndex = -1;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUserCallPreferences() {
        boolean call = mPreferences.getBoolean("call", false);
        if (call) {
            dial = new Intent(Intent.ACTION_CALL);
        } else {
            dial = new Intent(Intent.ACTION_DIAL);
        }
    }

    private boolean checkUserRememberPreferences() {
        return mPreferences.getBoolean("remember", true);
    }

    public void onFavorite(View view) {
        mBtn = (FloatingActionButton) view;
        favouriteMode = !favouriteMode;
        ViewPropertyAnimator animator = mBtn.animate();
        if (favouriteMode) {
            mCancelItem.setVisible(false);
            getSupportActionBar().setTitle("Омилени");
            mTaxiAdapter.clearFilter();
            mTaxiAdapter.setFavoriteTaxis();
            mTaxiAdapter.setFavMode();
            mBtn.setImageResource(R.drawable.ic_action_cancel);
            animator.rotation(90);
        } else {
            resetFavouriteMode();
        }
        mTaxiAdapter.notifyDataSetChanged();
    }

    public void rotateBtnBack(FloatingActionButton btn) {
        btn.setImageResource(R.drawable.ic_action_star_10);
        ViewPropertyAnimator animator = btn.animate();
        animator.rotation(-72);
    }

    public void resetFavouriteMode() {
        mTaxiAdapter.setFavModeOff();
        if (mSelectedCity != null) {
            mCancelItem.setVisible(true);
            getSupportActionBar().setTitle(mSelectedCity.getName());
            mTaxiAdapter.setFilteredTaxis(mSelectedCity.getId());
            mTaxiAdapter.setFilter();
        } else {
            getSupportActionBar().setTitle("Сите такси компании");
        }
        rotateBtnBack(mBtn);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    static boolean isOnline(Context context) {
        // ConnectivityManager is used to check available network(s)
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            // no network is available
            return false;
        } else {
            // at least one type of network is available
            return true;
        }
    }

    //handle dialog interface
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.dismiss();
            if (!mSettings.getBoolean("saved", false)) {
                Toast.makeText(getApplicationContext(),
                        "Податоците првично се превземаат од интернет\nПроверете ја вашата интернет врска", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            isReturnedFromSettings = true;
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    // Handling time to get internet connection
    class ResumeActivityTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mProgressBar.setVisibility(View.GONE);
            if (isOnline(getApplicationContext())) {
                mProgressBar.setVisibility(View.VISIBLE);
                CheckTaxiVersion checkTaxiVersion = new CheckTaxiVersion(getApplicationContext());
                checkTaxiVersion.execute();
                checkTaxiVersion.setOnVersionChecked(mOnVersionChecked);
            } else if (!mSettings.getBoolean("saved", false)) {
                Toast.makeText(getApplicationContext(),
                        "Податоците првично се превземаат од интернет\nПроверете ја вашата интернет врска", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Потребна е интернет врска за освежување на податоците", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEditorSettings.putInt("cityFilter", selectedIndex);
        mEditorSettings.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isReturnedFromSettings) {
            isReturnedFromSettings = false;
            new ResumeActivityTask().execute();
        }
    }

    @Override
    public void onBackPressed() {
        if (favouriteMode) {
            favouriteMode = false;
            resetFavouriteMode();
            mTaxiAdapter.notifyDataSetChanged();
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class DownloadStateReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private DownloadStateReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), getText(R.string.data_downloaded), Toast.LENGTH_SHORT).show();
            mEditorSettings.putBoolean("saved", true);
            // Commit the edits!
            mEditorSettings.commit();
            FetchTaxis fetchTaxis = new FetchTaxis(getApplicationContext());
            fetchTaxis.execute();
            fetchTaxis.setmOnFetchedTaxis(mOnFetchedTaxis);
            mProgressBar.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}