package autoservisi.Autoservisi.MakinaIme.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;

import io.realm.Realm;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.abstracts.BaseActivity;
import autoservisi.Autoservisi.MakinaIme.realm.Constants;
import autoservisi.Autoservisi.MakinaIme.realm.models.FuelTank;
import autoservisi.Autoservisi.MakinaIme.realm.models.RealmSettings;
import autoservisi.Autoservisi.MakinaIme.realm.models.Vehicle;
import autoservisi.Autoservisi.MakinaIme.utils.DateUtils;
import autoservisi.Autoservisi.MakinaIme.utils.FileUtils;
import autoservisi.Autoservisi.MakinaIme.utils.ImageViewUtils;
import autoservisi.Autoservisi.MakinaIme.utils.PermissionUtils;
import autoservisi.Autoservisi.MakinaIme.utils.RealmUtils;

public class ViewVehicleActivity extends BaseActivity {

    private ImageView imageView;
    private TextView tvBrand, tvModel, tvOdometer, tvManufactureDate;
    private TextView tvHorsePower, tvCubicCentimeters, tvRegistrationPlate;
    private TextView tvVinPlate, tvNotes;
    private String vehicleId;
    private LinearLayout llFuelTanks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vehicle);
        initComponents();
        setContent();
        setComponentListeners();
    }

    private void setStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
    }

    public void setContent() {
        Realm myRealm = Realm.getDefaultInstance();
        final Vehicle vehicle = myRealm.where(Vehicle.class)
                .equalTo(Constants.ID, vehicleId)
                .findFirst();
        myRealm.close();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_view_vehicle);
        int textIconsColor = vehicle.getColor().getTextIconsColor();
        toolbar.setTitle(vehicle.getName());
        toolbar.setTitleTextColor(textIconsColor);
        setSupportActionBar(toolbar);
        setBackNavigation();
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                verticalOffset = Math.abs(verticalOffset);
                int difference = appBarLayout.getTotalScrollRange() - toolbar.getHeight();
                if (verticalOffset >= difference) {
                    setStatusBarColor(ResourcesCompat.getColor(getResources()
                            , R.color.colorPrimaryDark, null));
                } else {
                    setStatusBarColor(vehicle.getColor().getRelevantDarkColor());
                }
            }
        });
        appBarLayout.setBackgroundColor(vehicle.getColor().getColor());
        setStatusBarColor(vehicle.getColor().getRelevantDarkColor());
        imageView.setBackground(ImageViewUtils.getDrawableByVehicleType(vehicle.getType(),
                getApplicationContext(), vehicle.getColor().getTextIconsColor()));
        tvBrand.setText(vehicle.getBrand().getName());
        tvModel.setText(vehicle.getModel().getName());
        RealmSettings settings = myRealm.where(RealmSettings.class).findFirst();
        tvOdometer.setText(String.valueOf(vehicle.getOdometer() + settings.getLengthUnit()));
        tvManufactureDate.setText(DateUtils.dateToString(vehicle.getManufactureDate()));
        tvHorsePower.setText(String.valueOf(vehicle.getHorsePower()));
        tvCubicCentimeters.setText(String.valueOf(vehicle.getCubicCentimeter()));
        tvRegistrationPlate.setText(vehicle.getRegistrationPlate());
        tvVinPlate.setText(vehicle.getVinPlate());
        for (FuelTank fuelTank : vehicle.getFuelTanks()) {
            displayFuelTanks(fuelTank);
        }
        tvNotes.setText(vehicle.getNote());
    }

    private void displayFuelTanks(FuelTank fuelTank) {
        View view = getLayoutInflater().inflate(R.layout.row_view_fuel_tank, llFuelTanks, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.imgv_row_view_ft_fuel_type);
        TextView tvFuelType = (TextView) view.findViewById(R.id.tv_row_view_ft_fuel_type);
        TextView tvCapacity = (TextView) view.findViewById(R.id.tv_row_view_ft_capacity);
        TextView tvConsumption = (TextView) view.findViewById(R.id.tv_row_view_ft_consumption);
        imageView.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_menu_refueling, null));
        String text = String.format(getString(R.string.fuel_type_placeholder), fuelTank.getType());
        tvFuelType.setText(text);
        text = String.format(getString(R.string.capacity_placeholder), fuelTank.getCapacity());
        tvCapacity.setText(text);
        text = String.format(getString(R.string.consumption_placeholder), fuelTank.getConsumption());
        tvConsumption.setText(text);
        llFuelTanks.addView(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_vehicle, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportVehicle();
            } else {
                showMessage("Nuk nevojitet leje");
            }
        }
    }

    private void exportVehicle() {
        File dir = FileUtils.createAppDir(getApplicationContext());
        if (dir != null) {
            Realm myRealm = Realm.getDefaultInstance();
            Vehicle vehicle = myRealm.where(Vehicle.class)
                    .equalTo(Constants.ID, vehicleId).findFirst();
            vehicle = myRealm.copyFromRealm(vehicle);
            String content = new Gson().toJson(vehicle);
            myRealm.close();
            File file = FileUtils.createFile(getApplicationContext(), dir,
                    vehicle.getType() + "_" + vehicle.getName(), content);
            if (file != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, FileUtils.getFileUri(file));
                shareIntent.setType("text/plain");
                shareIntent.setPackage("com.android.bluetooth");
                startActivity(Intent.createChooser(shareIntent, "Send to"));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export){
            if (FileUtils.isExternalStorageWritable()) {
                if (PermissionUtils.hasWriteExternalStoragePermission(ViewVehicleActivity.this)) {
                    exportVehicle();
                }else {
                    showMessage("Nuk nevojitet leje per te iu qasur bazes se jashtme");
                    PermissionUtils.requestWriteExternalStoragePermission(ViewVehicleActivity.this);
                }
            }else {
                showMessage("Nuk ka baze te jashtme");
            }
            return true;
        }else if (id == R.id.action_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewVehicleActivity.this);
            builder.setTitle(getToolbarTitle());
            builder.setMessage("MAkina do te fshihet. A jeni te sigurte?");
            builder.setCancelable(true)
                    .setNegativeButton("Jo", null)
                    .setPositiveButton("Po", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Realm myRealm = Realm.getDefaultInstance();
                            myRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Vehicle vehicle = realm.where(Vehicle.class)
                                            .equalTo(Constants.ID, vehicleId)
                                            .findFirst();
                                    RealmUtils.deleteVehicle(vehicle);
                                }
                            }, new Realm.Transaction.OnSuccess() {
                                @Override
                                public void onSuccess() {
                                    showMessage("Makina u fshi!");
                                    finish();
                                }
                            }, new Realm.Transaction.OnError() {
                                @Override
                                public void onError(Throwable error) {
                                    error.printStackTrace();
                                    showMessage("OOOPS gabim");
                                    finish();
                                }
                            });
                        }
                    });
            builder.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initComponents() {
        imageView = (ImageView) findViewById(R.id.imgv_view_vehicle_type);
        tvBrand = (TextView) findViewById(R.id.tv_view_vehicle_brand);
        tvModel = (TextView) findViewById(R.id.tv_view_vehicle_model);
        tvOdometer = (TextView) findViewById(R.id.tv_view_vehicle_odometer);
        tvManufactureDate = (TextView) findViewById(R.id.tv_view_vehicle_manufacture_date);
        tvHorsePower = (TextView) findViewById(R.id.tv_view_vehicle_horse_power);
        tvCubicCentimeters = (TextView) findViewById(R.id.tv_view_vehicle_cubic_centimeters);
        tvRegistrationPlate = (TextView) findViewById(R.id.tv_view_vehicle_registration_plate);
        tvVinPlate = (TextView) findViewById(R.id.tv_view_vehicle_vin_plate);
        tvNotes = (TextView) findViewById(R.id.tv_view_vehicle_notes);
        llFuelTanks = (LinearLayout) findViewById(R.id.ll_view_vehicle_fuel_tanks);
        Intent intent = getIntent();
        vehicleId = intent.getStringExtra(Constants.ID);
    }

    @Override
    public void setComponentListeners() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_view_vehicle);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewVehicleActivity.class);
                intent.putExtra(Constants.ID, vehicleId);
                startActivity(intent);
                finish();
            }
        });
    }
}
