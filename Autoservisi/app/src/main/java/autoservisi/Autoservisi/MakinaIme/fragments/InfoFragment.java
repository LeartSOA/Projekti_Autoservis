package autoservisi.Autoservisi.MakinaIme.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;

import io.realm.Realm;
import io.realm.RealmQuery;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.realm.Constants;
import autoservisi.Autoservisi.MakinaIme.realm.models.Expense;
import autoservisi.Autoservisi.MakinaIme.realm.models.Insurance;
import autoservisi.Autoservisi.MakinaIme.realm.models.RealmSettings;
import autoservisi.Autoservisi.MakinaIme.realm.models.Refueling;
import autoservisi.Autoservisi.MakinaIme.realm.models.Service;
import autoservisi.Autoservisi.MakinaIme.realm.models.Vehicle;
import autoservisi.Autoservisi.MakinaIme.utils.MoneyUtils;

public class InfoFragment extends Fragment {

    public static final String TAG = "InfoFragment";

    private View view;

    public InfoFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info, container, false);
        setContent();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setContent();
    }

    private void setContent() {
        Realm myRealm = Realm.getDefaultInstance();
        RealmSettings settings = myRealm.where(RealmSettings.class).findFirst();
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.ll_fragment_info);
        linearLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        BigDecimal totalCost = new BigDecimal(0);
        displayView("Totali i makinave", String.valueOf(myRealm.where(Vehicle.class).count()),
                inflater);
        RealmQuery<Service> services = myRealm.where(Service.class);
        displayView("Totali i Riparimeve", String.valueOf(services.count()), inflater);
        BigDecimal bigDecimal = new BigDecimal(services.sum(Constants.PRICE).toString());
        String text = "%s " + settings.getCurrencyUnit();
        totalCost = totalCost.add(bigDecimal);
        displayView("Parate e shpenzuara per riparim", String.format(text, MoneyUtils.longToString(bigDecimal)), inflater);

        RealmQuery<Insurance> insurances = myRealm.where(Insurance.class);
        displayView("Numri total i sigurimeve", String.valueOf(insurances.count()), inflater);
        bigDecimal = new BigDecimal(insurances.sum(Constants.PRICE).toString());
        totalCost = totalCost.add(bigDecimal);
        displayView("Parate e shpenzuara per sigurime", String.format(text, MoneyUtils.longToString(bigDecimal)), inflater);

        RealmQuery<Refueling> refuelings = myRealm.where(Refueling.class);
        displayView("Mbushjet totale", String.valueOf(refuelings.count()), inflater);
        bigDecimal = new BigDecimal(refuelings.sum(Constants.PRICE).toString());
        totalCost = totalCost.add(bigDecimal);
        displayView("Parate e shpenzuara per mbushje", String.format(text, MoneyUtils.longToString(bigDecimal)), inflater);

        RealmQuery<Expense> expenses = myRealm.where(Expense.class);
        displayView("Shpenzimet totale", String.valueOf(expenses.count()), inflater);
        bigDecimal = new BigDecimal(expenses.sum(Constants.PRICE).toString());
        displayView("Parate per shpenzime", String.format(text, MoneyUtils.longToString(bigDecimal)), inflater);
        totalCost = totalCost.add(bigDecimal);

        displayView("Kostoja totale", String.format(text, MoneyUtils.longToString(totalCost)), inflater);
        myRealm.close();
    }

    private void displayView(String title, String value, LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.item_view_activity, null);
        TextView tvTitle = (TextView) v.findViewById(R.id.tv_item_view_title);
        TextView tvValue = (TextView) v.findViewById(R.id.tv_item_view_value);
        tvTitle.setText(title);
        tvValue.setText(value);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.ll_fragment_info);
        linearLayout.addView(v);
    }
}
