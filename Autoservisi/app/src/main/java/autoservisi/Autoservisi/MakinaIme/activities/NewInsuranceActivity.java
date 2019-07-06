package autoservisi.Autoservisi.MakinaIme.activities;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.abstracts.NewBaseActivity;
import autoservisi.Autoservisi.MakinaIme.dialogs.NewCompanyDialog;
import autoservisi.Autoservisi.MakinaIme.realm.Constants;
import autoservisi.Autoservisi.MakinaIme.realm.models.Company;
import autoservisi.Autoservisi.MakinaIme.realm.models.DateNotification;
import autoservisi.Autoservisi.MakinaIme.realm.models.Insurance;
import autoservisi.Autoservisi.MakinaIme.realm.models.Vehicle;
import autoservisi.Autoservisi.MakinaIme.utils.DateTimePickerUtils;
import autoservisi.Autoservisi.MakinaIme.utils.DateUtils;
import autoservisi.Autoservisi.MakinaIme.utils.MoneyUtils;
import autoservisi.Autoservisi.MakinaIme.utils.NotificationUtils;
import autoservisi.Autoservisi.MakinaIme.utils.RealmUtils;
import autoservisi.Autoservisi.MakinaIme.utils.TextUtils;

public class NewInsuranceActivity extends NewBaseActivity {

    private Spinner spnCompanies;
    private RealmResults<Company> results;
    private ArrayAdapter<String> companiesAdapter;
    private String companyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_insurance);
        initComponents();
        btnTime.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_calendar_black_24dp), null, null);
        setContent();
        setComponentListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setEnabled(false);
        int id = item.getItemId();
        if (id == R.id.action_save) {
            progressBar.setIndeterminate(true);
            if (isInputValid()) {
                companyId = results.get(spnCompanies.getSelectedItemPosition()).getId();
                saveToRealm();
            }else {
                item.setEnabled(true);
                progressBar.setIndeterminate(false);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initComponents() {
        super.initComponents();
        spnCompanies = (Spinner) findViewById(R.id.spn_new_insurance_company_name);
        results = myRealm.where(Company.class).findAllSorted(Constants.NAME, Sort.ASCENDING);
        companiesAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.textview_spinner, getCompanyNames());
        companiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCompanies.setAdapter(companiesAdapter);
    }

    @Override
    public void setComponentListeners() {
        super.setComponentListeners();
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimePickerUtils.showDatePicker(NewInsuranceActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = DateUtils.getDateFromInts(year, month, dayOfMonth);
                        btnTime.setText(date);
                    }
                });
            }
        });
        findViewById(R.id.img_btn_add_company).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NewCompanyDialog dialog = new NewCompanyDialog();
                dialog.setListener(new NewCompanyDialog.OnAddNewCompanyListener() {
                    @Override
                    public void onAddCompany(String companyName) {
                        dialog.dismiss();
                        results = myRealm.where(Company.class).findAllSorted(Constants.NAME, Sort.ASCENDING);
                        ArrayList<String> spinnerDataSet = getCompanyNames();
                        int index = spinnerDataSet.indexOf(companyName);
                        companiesAdapter.clear();
                        companiesAdapter.addAll(spinnerDataSet);
                        companiesAdapter.notifyDataSetChanged();
                        spnCompanies.setSelection(index);
                    }
                });
                dialog.show(getSupportFragmentManager(), "NewCompany");
            }
        });
    }

    @Override
    protected void populateNewItem() {
        Calendar calendar = Calendar.getInstance();
        btnDate.setText(DateUtils.dateToString(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        btnTime.setText(DateUtils.dateToString(calendar.getTime()));
    }

    @Override
    protected void populateExistingItem() {
        Insurance insurance = myRealm.where(Insurance.class)
                .equalTo(Constants.ID, itemId)
                .findFirst();
        btnDate.setText(DateUtils.dateToString(insurance.getDate()));
        btnTime.setText(DateUtils.dateToString(insurance.getNotification().getDate()));
        TextUtils.setTextToTil(tilOdometer, String.valueOf(insurance.getOdometer()));
        TextUtils.setTextToTil(tilPrice, MoneyUtils.longToString(new BigDecimal(insurance.getPrice())));
        TextUtils.setTextToTil(tilNote, insurance.getNote());
        spnCompanies.setSelection(results.indexOf(insurance.getCompany()));
    }

    @Override
    public boolean isInputValid() {
        boolean valid = true;

        if (DateUtils.isDateInFuture(btnDate.getText().toString())) {
            valid = false;
            showMessage("Data duhet te jete para te tashmes");
        }

        if (DateUtils.isNotValidDate(btnTime.getText().toString(), false)) {
            valid = false;
            showMessage("Date jo e sakte e skadimit");
        }else if (!DateUtils.isExpirationDateValid(btnTime.getText().toString())) {
            valid = false;
            showMessage("Data e skadimit duhet te jete te pakten nje dite pas te sotmes");
        }

        return super.isInputValid() && valid;
    }

    @Override
    protected void saveItem(Realm realm) {
        Vehicle vehicle = realm.where(Vehicle.class)
                .equalTo(Constants.ID, vehicleId)
                .findFirst();

        Insurance insurance = new Insurance();

        if (isNewItem()) {
            insurance.setId(UUID.randomUUID().toString());
        } else {
            Insurance oldInsurance = vehicle.getInsurances()
                    .where()
                    .equalTo(Constants.ID, itemId)
                    .findFirst();
            RealmUtils.deleteProperty(oldInsurance, Constants.ActivityType.INSURANCE);
            insurance.setId(itemId);
        }

        Date date = DateUtils.stringToDate(btnDate.getText().toString());
        insurance.setDate(date);

        long odometer = Long.parseLong(TextUtils.getTextFromTil(tilOdometer));
        insurance.setOdometer(odometer);
        if (odometer > getVehicleOdometer()) {
            vehicle.setOdometer(odometer);
            setVehicleOdometer(odometer);
        }

        long price = MoneyUtils.stringToLong(TextUtils.getTextFromTil(tilPrice));
        insurance.setPrice(price);

        insurance.setNote(TextUtils.getTextFromTil(tilNote));

        DateNotification notification = realm.createObject(DateNotification.class, UUID.randomUUID().toString());
        Number number = realm.where(DateNotification.class).max(Constants.NOTIFICATION_ID);
        int notificationId;
        if (number == null) {
            notificationId = 0;
        } else {
            notificationId = number.intValue() + 1;
        }
        notification.setNotificationId(notificationId);
        date = DateUtils.stringToDate(btnTime.getText().toString());
        notification.setDate(date);
        notification.setTriggered(false);
        insurance.setNotification(notification);

        Company company = realm.where(Company.class)
                .equalTo(Constants.ID, companyId)
                .findFirst();
        insurance.setCompany(company);
        vehicle.getInsurances().add(realm.copyToRealmOrUpdate(insurance));
        setNotification(insurance);
    }

    @Override
    protected void onItemSaved() {
        if (isNewItem()) {
            showMessage("Sigurimi i ri u shtua.");
        }else {
            showMessage("Sigurimi u rifreskua");
            Intent intent = new Intent(getApplicationContext(), ViewActivity.class);
            intent.putExtra(Constants.ID, vehicleId);
            intent.putExtra(Constants.ITEM_ID, itemId);
            intent.putExtra(Constants.TYPE, Constants.ActivityType.INSURANCE.ordinal());
            startActivity(intent);
        }

        finish();
    }

    private void setNotification(Insurance insurance) {
        Date notificationDate = insurance.getNotification().getDate();

        Notification notification = NotificationUtils.createNotification(this, vehicleId,
                insurance.getId(), Constants.ActivityType.INSURANCE, "Insurance",
                insurance.getCompany().getName() + " sigurimi skadon me " +
                DateUtils.datetimeToString(insurance.getNotification().getDate()),
                R.drawable.ic_insurance_black);

        NotificationUtils.setNotificationOnDate(getApplicationContext(), notification,
                insurance.getNotification().getNotificationId(), notificationDate.getTime());
    }

    private ArrayList<String> getCompanyNames() {
        ArrayList<String> names = new ArrayList<>(results.size());
        for (Company company : results) {
            names.add(company.getName());
        }
        return names;
    }
}
