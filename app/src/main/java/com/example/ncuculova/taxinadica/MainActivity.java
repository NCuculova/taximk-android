package com.example.ncuculova.taxinadica;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity {

    TaxiAdapter mTaxiAdapter;
    ProgressBar mProgressBar;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    String mActivityTitle;
    CityAdapter mCityAdapter;
    SwipeMenuListView mListTaxi;
    EditTaxis editTaxis;
    boolean favouriteMode;
    City selectedCity;
    MenuItem cancelItem;
    FloatingActionButton btn;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Intent mService;
    IntentFilter mIntentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListTaxi = (SwipeMenuListView) findViewById(R.id.listView);
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
        cancelItem = null;
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
                startService(mService);
            }
        });

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (favouriteMode) {
                    mTaxiAdapter.setFavModeOff();
                    rotateBtnBack(btn);
                }
                mTaxiAdapter.setFilteredTaxis(id);
                mTaxiAdapter.setFilter();
                mTaxiAdapter.notifyDataSetChanged();
                selectedCity = (City) mCityAdapter.getItem(position);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        FetchTaxis fetchTaxis = new FetchTaxis(this);
        fetchTaxis.setmOnFetchedTaxis(mOnFetchedTaxis);
        DownloadTaxis downloadTaxis = new DownloadTaxis(this);
        downloadTaxis.setmOnDownloadedTaxis(new OnDownloadedTaxis() {
            @Override
            public void onDownloaded(Data result) {
                mTaxiAdapter.setTaxis(result.getTaxis());
                mTaxiAdapter.notifyDataSetChanged();
                SharedPreferences settings = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("saved", true);
                // Commit the edits!
                editor.commit();
                List<City> cities = new ArrayList<>(result.getCities());
                mCityAdapter.setCities(cities);
                mCityAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
            }

        });

        boolean saved = settings.getBoolean("saved", false);
        if (!saved) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("saved", true);
            // Commit the edits!
            editor.commit();
            startService(mService);
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
                Intent dial = new Intent();
                dial.setAction("android.intent.action.DIAL");
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
                        editTaxis = new EditTaxis(getApplicationContext());
                        editTaxis.execute(taxi);
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

    private final OnFetchedTaxis mOnFetchedTaxis = new OnFetchedTaxis() {
        @Override
        public void onFetched(Data result) {
            mTaxiAdapter.setTaxis(result.getTaxis());
            mTaxiAdapter.notifyDataSetChanged();
            List<City> cities = new ArrayList<>(result.getCities());
            mCityAdapter.setCities(cities);
            mCityAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
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
                if (selectedCity != null) {
                    getSupportActionBar().setTitle(selectedCity.getName());
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
        cancelItem = menu.getItem(1);
        if (selectedCity != null) {
            cancelItem.setVisible(true);
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
        if (item.getItemId() == R.id.action_cancel) {
            selectedCity = null;
            mTaxiAdapter.clearFilter();
            mTaxiAdapter.notifyDataSetChanged();
            item.setVisible(false);
            getSupportActionBar().setTitle("Сите такси компании");
        }

        return super.onOptionsItemSelected(item);
    }

    public void onFavorite(View view) {
        btn = (FloatingActionButton) view;
        favouriteMode = !favouriteMode;
        ViewPropertyAnimator animator = btn.animate();
        if (favouriteMode) {
            cancelItem.setVisible(false);
            getSupportActionBar().setTitle("Омилени");
            mTaxiAdapter.clearFilter();
            mTaxiAdapter.setFavoriteTaxis();
            mTaxiAdapter.setFavMode();
            btn.setImageResource(R.drawable.ic_action_cancel);
            animator.rotation(90);
        } else {
            mTaxiAdapter.setFavModeOff();
            if (selectedCity != null) {
                cancelItem.setVisible(true);
                getSupportActionBar().setTitle(selectedCity.getName());
                mTaxiAdapter.setFilteredTaxis(selectedCity.getId());
                mTaxiAdapter.setFilter();
            } else {
                getSupportActionBar().setTitle("Сите такси компании");
            }
            rotateBtnBack(btn);
        }
        mTaxiAdapter.notifyDataSetChanged();
    }

    public void rotateBtnBack(FloatingActionButton btn) {
        btn.setImageResource(R.drawable.ic_action_star_10);
        ViewPropertyAnimator animator = btn.animate();
        animator.rotation(-72);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class DownloadStateReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private DownloadStateReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "Data downloaded", Toast.LENGTH_SHORT).show();
            FetchTaxis fetchTaxis = new FetchTaxis(getApplicationContext());
            fetchTaxis.execute();
            fetchTaxis.setmOnFetchedTaxis(mOnFetchedTaxis);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}