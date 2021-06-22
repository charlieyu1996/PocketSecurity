package pocketsecurity.agroupofstudents.com.pocketsecurity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import pocketsecurity.agroupofstudents.com.pocketsecurity.custom.MyMarkerView;
import pocketsecurity.agroupofstudents.com.pocketsecurity.Recording.LaunchRecordingActivity;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Observer, OnChartValueSelectedListener {

    // Private Variables
    HomeModel homeModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Get home model instance
        homeModel = HomeModel.getInstance();
        homeModel.addObserver(this);

        // people detected chart
        LineChart chart = findViewById(R.id.chart);
        Typeface mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Bold.ttf");
        LineData data = getData(36, 100, 0);
        data.setValueTypeface(mTf);
        setupChart(chart, data, Color.argb(250, 21, 25, 33));
    }

    @Override
    public void onResume(){
        super.onResume();
        // if the user just finished recording, show a toast
        if (homeModel.getFinishedRecording()){
            recreate();
            ArrayList<Entry> flagList = homeModel.getFlag();
            Entry lastFlag = flagList.get(flagList.size()-1);
            int flagNum = (int) lastFlag.getY();

            Context context = getApplicationContext();
            CharSequence text = "Flags Detected: " + flagNum + ", the video will be available in a few minutes";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            homeModel.setFinishedRecording(false);
        }
    }

    private void setupChart(LineChart chart, LineData data, int color) {
        ((LineDataSet) data.getDataSetByIndex(0)).setCircleHoleColor(color);

        // no description text
        chart.getDescription().setEnabled(false);

        // enable / disable grid background
        chart.setDrawGridBackground(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // create marker to display box when values are selected
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        // Set the marker to the chart
        mv.setChartView(chart);
        chart.setMarker(mv);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setBackgroundColor(color);

        // set custom chart offsets (automatic offset calculation is hereby disabled)
        chart.setViewPortOffsets(55, 0, 40, 20);

        // add data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(200f);
            yAxis.setAxisMinimum(0f);

            yAxis.setTextColor(Color.WHITE);
        }

        // animate calls invalidate()...
        chart.animateX(1500);
    }

    private LineData getData(int count, float range, int type) {
        ArrayList<Entry> values = homeModel.getFlag();

        // create a data set and give it a type
        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);
        set1.setLineWidth(1.75f);
        set1.setCircleRadius(5f);
        set1.setCircleHoleRadius(2.5f);
        set1.setColor(Color.WHITE);
        set1.setCircleColor(Color.RED);
        set1.setHighLightColor(Color.WHITE);
        // text color and size
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTextSize(9f);
        set1.setDrawValues(true);

        // create a data object with the data sets
        return new LineData(set1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(this, LaunchRecordingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, ViewingActivity.class);
            startActivity(intent);
        } else if(id == R.id.nav_password_verification) {
            Intent intent = new Intent(this, PasswordSetup.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void update(Observable o, Object arg) {}

    @Override
    public void onValueSelected(Entry e, Highlight h) {}

    @Override
    public void onNothingSelected() {}
}
