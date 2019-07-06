package autoservisi.Autoservisi.MakinaIme.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.abstracts.BaseActivity;
import autoservisi.Autoservisi.MakinaIme.dialogs.SettingsDialog;
import autoservisi.Autoservisi.MakinaIme.fragments.InfoFragment;
import autoservisi.Autoservisi.MakinaIme.fragments.ListFragment;
import autoservisi.Autoservisi.MakinaIme.fragments.StatisticsFragment;
import autoservisi.Autoservisi.MakinaIme.realm.Constants;
import autoservisi.Autoservisi.MakinaIme.realm.models.Vehicle;
import autoservisi.Autoservisi.MakinaIme.utils.FileUtils;
import autoservisi.Autoservisi.MakinaIme.utils.RealmUtils;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String FRAGMENT_TYPE = "fragment_type";
    public static final String STATISTIC_TYPE = "statistic_type";
    private static final int READ_REQUEST_CODE = 42;
    private Button button;
    private Spinner spnChooseVehicle;
    private Realm myRealm;
    private RealmResults<Vehicle> results;
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<String> spinnerDataSet;
    private DrawerLayout drawer;
    private int menuId;
    private RealmChangeListener<RealmResults<Vehicle>> callback =
            new RealmChangeListener<RealmResults<Vehicle>>() {
                @Override
                public void onChange(RealmResults<Vehicle> element) {
                    spinnerDataSet = getVehicleNamesFromResults();
                    spinnerAdapter.clear();
                    spinnerAdapter.addAll(spinnerDataSet);
                    spinnerAdapter.notifyDataSetChanged();
                    if (spinnerAdapter.getCount() > 0) {
                        spnChooseVehicle.setSelection(0);
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            menuId = savedInstanceState.getInt("menu_id");
        }else {
            menuId = R.id.nav_info;
        }
        setContentView(R.layout.activity_main);
        initComponents();
        setComponentListeners();
        button =(Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLocation();
            }
        });
    }
    public void openLocation()

    {
        Intent intent=new Intent(this,Location.class);
        startActivity(intent);

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("menu_id", menuId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            SettingsDialog settingsDialog = new SettingsDialog();
            settingsDialog.show(getSupportFragmentManager(), "settings_dialog");
            return true;
        }else if (id == R.id.action_import) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, READ_REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    String content = FileUtils.getContentFromInputStream(inputStream);
                    final Vehicle vehicle = new Gson().fromJson(content, Vehicle.class);
                    if (vehicle != null) {
                        if (myRealm.where(Vehicle.class)
                                .equalTo(Constants.NAME, vehicle.getName())
                                .findFirst() != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Gabim");
                            builder.setMessage(vehicle.getName() + " " + vehicle.getType().toLowerCase()
                                    + " tanime i regjistruar. Doni te ndryshoni?");
                            builder.setCancelable(true);
                            builder.setNegativeButton("Jo", null);
                            builder.setPositiveButton("Po", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    importVehicle(vehicle, true);
                                }
                            });
                            builder.show();
                        } else {
                            importVehicle(vehicle, false);
                        }
                    }else {
                        showMessage("Nuk mund te ruhet");
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    showMessage("Nuk mund ti qaseni makines");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showMessage("Nuk u gjet fajlli");
                }
            }
        }
    }

    private void importVehicle(Vehicle vehicle, boolean exists) {
        Realm.Transaction.OnSuccess onSuccess = new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                openFragment();
                showMessage("Makina u importua");
            }
        };
        Realm.Transaction.OnError onError = new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
                showMessage("OOOPS nje gabim");
            }
        };
        RealmUtils.importVehicle(vehicle, exists, myRealm, onSuccess, onError);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        setToolbarTitle(item.getTitle().toString());
        menuId = item.getItemId();
        openFragment();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        results.removeChangeListener(callback);
        myRealm.close();
    }

    @Override
    public void initComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        spnChooseVehicle = (Spinner) findViewById(R.id.spn_main_choose_vehicle);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        myRealm = Realm.getDefaultInstance();
        results = myRealm.where(Vehicle.class)
                .findAllSortedAsync(Constants.NAME, Sort.ASCENDING);
        spinnerDataSet = getVehicleNamesFromResults();
        spinnerAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.textview_spinner_white, spinnerDataSet);
        results.addChangeListener(callback);
        spnChooseVehicle.setSelection(0);
        navigationView.getMenu().performIdentifierAction(menuId, 0);
        navigationView.setCheckedItem(menuId);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnChooseVehicle.setAdapter(spinnerAdapter);
    }

    @Override
    public void setComponentListeners() {
        spnChooseVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                openFragment();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_speed_dial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                Intent intent;
                Class aClass;
                String vehicleId = null;
                long vehicleOdometer = 0;
                boolean valid = false;
                if (results != null && !results.isEmpty()) {
                    Vehicle vehicle = results.get(spnChooseVehicle.getSelectedItemPosition());
                    vehicleId = vehicle.getId();
                    vehicleOdometer = vehicle.getOdometer();
                    valid = true;
                }
                switch (menuItem.getItemId()) {
                    case R.id.action_add_car:
                        aClass = NewVehicleActivity.class;
                        vehicleId = null;
                        break;
                    case R.id.action_add_service:
                        aClass = NewServiceActivity.class;
                        break;
                    case R.id.action_add_expense:
                        aClass = NewExpenseActivity.class;
                        break;
                    case R.id.action_add_refueling:
                        aClass = NewRefuelingActivity.class;
                        break;
                    case R.id.action_add_insurance:
                        aClass = NewInsuranceActivity.class;
                        break;
                    default:
                        return false;
                }
                if (valid || menuItem.getItemId() == R.id.action_add_car) {
                    intent = new Intent(getApplicationContext(), aClass);
                    if (vehicleId != null) {
                        intent.putExtra(Constants.ID, vehicleId);
                        intent.putExtra(Constants.ODOMETER, vehicleOdometer);
                    }
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    @Override
    public void setContent() {}

    private void openFragment() {
        if (results != null && !results.isEmpty()) {
            String vehicleId = results.get(spnChooseVehicle.getSelectedItemPosition()).getId();
            Fragment fragment;
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ID, vehicleId);
            if (menuId == R.id.nav_statistics) {
                fragment = new StatisticsFragment();
            }else if (menuId == R.id.nav_info) {
                fragment = new InfoFragment();
            }else {
                fragment = new ListFragment();
                bundle.putInt(FRAGMENT_TYPE, menuId);
            }
            fragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_content_main, fragment)
                    .commit();
        }
   }

    private ArrayList<String> getVehicleNamesFromResults() {
        ArrayList<String> names = new ArrayList<>(results.size());
        for (Vehicle vehicle : results) {
            names.add(vehicle.getName());
        }
        return names;
    }
}
