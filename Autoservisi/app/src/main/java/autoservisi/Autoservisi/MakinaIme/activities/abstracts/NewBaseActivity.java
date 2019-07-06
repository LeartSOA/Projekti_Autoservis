package autoservisi.Autoservisi.MakinaIme.activities.abstracts;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import org.apache.commons.lang3.math.NumberUtils;

import io.realm.Realm;
import io.realm.RealmResults;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.interfaces.INewBaseActivity;
import autoservisi.Autoservisi.MakinaIme.realm.Constants;
import autoservisi.Autoservisi.MakinaIme.realm.models.RealmSettings;
import autoservisi.Autoservisi.MakinaIme.realm.models.Service;
import autoservisi.Autoservisi.MakinaIme.utils.DateTimePickerUtils;
import autoservisi.Autoservisi.MakinaIme.utils.DateUtils;
import autoservisi.Autoservisi.MakinaIme.utils.NotificationUtils;
import autoservisi.Autoservisi.MakinaIme.utils.TextUtils;

public abstract class NewBaseActivity extends BaseActivity implements INewBaseActivity {

    private long vehicleOdometer;
    protected String vehicleId;
    protected String itemId;
    protected Realm myRealm;
    protected ProgressBar progressBar;
    protected Button btnDate;
    protected Button btnTime;
    protected TextView tvCurrentOdometer;
    protected TextInputLayout tilOdometer;
    protected TextInputLayout tilPrice;
    protected TextInputLayout tilNote;

    protected abstract void populateNewItem();
    protected abstract void populateExistingItem();
    protected abstract void saveItem(Realm realm);
    protected abstract void onItemSaved();

    @Override
    public void initComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setBackNavigation();
        btnDate = (Button) findViewById(R.id.btn_add_edit_date);
        btnTime = (Button) findViewById(R.id.btn_add_edit_time);
        tvCurrentOdometer = (TextView) findViewById(R.id.tv_add_edit_current_odometer);
        tilOdometer = (TextInputLayout) findViewById(R.id.til_add_edit_odometer);
        tilPrice = (TextInputLayout) findViewById(R.id.til_add_edit_price);
        tilNote = (TextInputLayout) findViewById(R.id.til_add_edit_note);
        progressBar = (ProgressBar) findViewById(R.id.pb_add_edit);
        Intent intent = getIntent();
        vehicleId = intent.getStringExtra(Constants.ID);
        itemId = intent.getStringExtra(Constants.ITEM_ID);
        vehicleOdometer = intent.getLongExtra(Constants.ODOMETER, 0);
        myRealm = Realm.getDefaultInstance();
    }

    @Override
    public void setComponentListeners() {
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimePickerUtils.showDatePicker(NewBaseActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = DateUtils.getDateFromInts(year, month, dayOfMonth);
                        btnDate.setText(date);
                    }
                });
            }
        });
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimePickerUtils.showTimePicker(NewBaseActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = DateUtils.getTimeFromInts(hourOfDay, minute);
                        btnTime.setText(time);
                    }
                });
            }
        });
    }

    @Override
    public void setContent() {
        String text = String.format(getString(R.string.current_odometer_placeholder), vehicleOdometer);
        tvCurrentOdometer.setText(String.valueOf(text));
        if (isNewItem()) {
            populateNewItem();
        }else {
            populateExistingItem();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRealm.close();
    }

    @Override
    public boolean isInputValid() {
        boolean valid = true;
        if (DateUtils.isNotValidDate(btnDate.getText().toString(), false)) {
            valid = false;
            showMessage("Date jo e sakte");
        }

        if (!NumberUtils.isCreatable(TextUtils.getTextFromTil(tilOdometer))) {
            valid = false;
            tilOdometer.setError("Pritet vlere numerike");
        }else {
            if (NumberUtils.createLong(TextUtils.getTextFromTil(tilOdometer)) < NumberUtils.LONG_ZERO) {
                valid = false;
                tilOdometer.setError("Kilometrazhi nuk mund te jete negativ");
            }
        }

        if (TextUtils.getTextFromTil(tilNote).length() > 256) {
            valid = false;
            tilNote.setError("Shume karaktere");
        }

        if (!NumberUtils.isCreatable(TextUtils.getTextFromTil(tilPrice))) {
            valid = false;
            tilPrice.setError("Cmimi duhet t ejete numer");
        }

        return valid;
    }

    @Override
    public void saveToRealm() {
        myRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                saveItem(realm);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                new ServiceNotifyingAsyncTask().execute();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                progressBar.setIndeterminate(false);
                showMessage("OOOPS gabim");
                error.printStackTrace();
            }
        });
    }

    public void setVehicleOdometer(long vehicleOdometer) {
        this.vehicleOdometer = vehicleOdometer;
    }

    public long getVehicleOdometer() {
        return vehicleOdometer;
    }

    public boolean isNewItem() {
        return itemId == null;
    }

    private class ServiceNotifyingAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Realm realmDb = Realm.getDefaultInstance();
            RealmSettings settings = realmDb.where(RealmSettings.class).findFirst();
            long targetOdometer = vehicleOdometer + settings.getDistanceInAdvance();
            RealmResults<Service> services = realmDb
                    .where(Service.class)
                    .equalTo(Constants.SHOULD_NOTIFY, true)
                    .equalTo(Constants.IS_ODOMETER_TRIGGERED, false)
                    .notEqualTo(Constants.TARGET_ODOMETER, 0)
                    .lessThanOrEqualTo(Constants.TARGET_ODOMETER, targetOdometer)
                    .findAll();
            String text = "%s duhet te shikohet tek %d " + settings.getLengthUnit();
            int i = 0;
            for (final Service service : services) {
                Notification notification = NotificationUtils.createNotification
                        (getApplicationContext(), vehicleId, service.getId(), Constants.ActivityType.SERVICE,
                                "Service", String.format(text, service.getType().getName(),
                                service.getTargetOdometer()), R.drawable.ic_services_black);

                NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(i, notification);
                realmDb.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        service.setOdometerTriggered(true);
                    }
                });
                i++;
            }
            realmDb.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setIndeterminate(false);
            onItemSaved();
        }
    }
}
