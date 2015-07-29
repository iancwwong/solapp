package com.example.administrator.myapplication;

import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    // A code
    private DrawerLayout drawerLayout;
    private ListView listView;
    private String[] planets;
    private ActionBarDrawerToggle drawerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // A code
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
//        drawerLayout.setScrimColor(Color.parseColor("#00FFFFFF"));
        planets = getResources().getStringArray(R.array.planets);
        listView = (ListView) findViewById(R.id.drawerList);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, planets));
        listView.setOnItemClickListener(this);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setHomeButtonEnabled(true); // Makes top left area clickable
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Displays back arrow in top left

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        drawerListener = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close)
        {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView); // Called when drawer is closed
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView); // Called when drawer is opened
            }
        };
        drawerLayout.setDrawerListener(drawerListener);





        ViewPagerAdapter adapter = new ViewPagerAdapter();
        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    SetMode1();
                }
                if (position == 1) {
                    SetMode2();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        pager.setAdapter(adapter);

        sb = (SeekBar) findViewById(R.id.seekBar);
        pb = (CircleProgressBar)findViewById(R.id.progressBar);
        pb.tx1 = (TextView)findViewById(R.id.text1);
        pb.tx2 = (TextView)findViewById(R.id.text2);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                // this sends a PERCENTAGE only ...

                int SendValue = MinVal + ((MaxVal - MinVal) * i / 100);

                if (b) {
                    pb.AnimateProgressTo(SendValue);
                } else {
                    pb.setCurVal(SendValue);
                }

                if (Mode == 1) {
                    Mode1 = SendValue;
                }
                if (Mode == 2) {
                    Mode2 = SendValue;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SetMode1(); // init as View1
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    SeekBar sb = null;
    CircleProgressBar pb = null;

    int MinVal = 0;
    int MaxVal = 100;
    int CurVal = 0;

    int Mode = 0;
    int Mode1 = 0;
    int Mode2 = 0;

    public void ResetBars() {
        int CurrentPercent = 100 * (pb.getCurVal()) / (pb.getMax());
        int NewPercent = (int)(100 * (float)CurVal/(float)MaxVal);
        sb.setProgress(NewPercent);
        pb.mode = Mode;
        pb.setMin(MinVal);
        pb.setMax(MaxVal);
        pb.EndAnimation();

        int NewVal = (CurrentPercent * MaxVal) / 100;
        pb.setCurVal(NewVal);
        pb.AnimateProgressTo(CurVal);
    }

    public void SetMode1() {
        MinVal = 0;
        MaxVal = 20000;
        CurVal = Mode1;
        Mode = 1;
        ResetBars();
    }

    public void SetMode2() {
        MinVal = 0;
        MaxVal = 500;
        CurVal = Mode2;
        Mode = 2;
        ResetBars();
    }

    public void ShortToast(String text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void buttonOnClick1(View v) {
        SetMode1();
        ShortToast("Mode 1");
    }

    public void buttonOnClick2(View v) {
        SetMode2();
        ShortToast("Mode 2");
    }

    // A code
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Toast.makeText(this, planets[position] + " was selected", Toast.LENGTH_LONG).show();
        selectItem(position); // Change title to selected item
    }

    // A code
    // Change title to selected item
    private void selectItem(int position) {
        listView.setItemChecked(position, true);
//        setTitle(planets[position]);
    }
    public void setTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    // Horizontal Bars minimising and maximised
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerListener.syncState();
    }

    // When item is clicked, this method is called
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (drawerListener.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerListener.onConfigurationChanged(newConfig); // Change for screen size changes
    }


}
//
//class myAdapter extends BaseAdapter {
//
//    String[] socialSites;
//    int[] images = {
//            R.drawable.abc_list_pressed_holo_dark,
//
//    };
//    public myAdapter(Context context) {
//        this.context = context;
//        socialSites = context.getResources().getStringArray(R.array.social);
//    }
//
//    @Override
//    public int getCount() {
//        return socialSites.length;
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return socialSites[position];
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if(convertView==null){
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        }
//        else
//        {
//
//        }
//        return null;
//    }
//}