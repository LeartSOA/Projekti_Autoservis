package autoservisi.Autoservisi.MakinaIme.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.MainActivity;
import autoservisi.Autoservisi.MakinaIme.adapters.BaseRealmAdapter;
import autoservisi.Autoservisi.MakinaIme.adapters.ExpenseRecyclerViewAdapter;
import autoservisi.Autoservisi.MakinaIme.adapters.InsuranceRecyclerViewAdapter;
import autoservisi.Autoservisi.MakinaIme.adapters.RefuelingRecyclerViewAdapter;
import autoservisi.Autoservisi.MakinaIme.adapters.ServiceRecyclerViewAdapter;
import autoservisi.Autoservisi.MakinaIme.adapters.VehicleRecyclerViewAdapter;
import autoservisi.Autoservisi.MakinaIme.realm.Constants;
import autoservisi.Autoservisi.MakinaIme.realm.models.RealmSettings;
import autoservisi.Autoservisi.MakinaIme.realm.models.Vehicle;

public class ListFragment extends Fragment {

    public static final String TAG = "ListFragment";

    private Realm myRealm;
    private BaseRealmAdapter adapter;
    private VehicleRecyclerViewAdapter vehicleAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int id = bundle.getInt(MainActivity.FRAGMENT_TYPE);
        String vehicleId;
        Vehicle vehicle;
        myRealm = Realm.getDefaultInstance();
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        final RealmRecyclerView recyclerView = (RealmRecyclerView) view.findViewById(R.id.realm_recycler_view);

        if (id == R.id.nav_my_cars) {
            RealmResults<Vehicle> vehicles = myRealm
                    .where(Vehicle.class)
                    .findAllSortedAsync(Constants.NAME, Sort.ASCENDING);
            vehicleAdapter = new VehicleRecyclerViewAdapter(getContext(),
                    vehicles, true, true);
            vehicleAdapter.addCallback();
            recyclerView.setAdapter(vehicleAdapter);
            vehicleAdapter.setViewForSnackbar(recyclerView);
        }else {
            vehicleId = bundle.getString(Constants.ID);
            vehicle = myRealm.where(Vehicle.class)
                    .equalTo(Constants.ID, vehicleId)
                    .findFirst();
            if (vehicle != null) {
                switch (id) {
                    case R.id.nav_services:
                        adapter = new ServiceRecyclerViewAdapter(getContext(),
                                vehicle.getServices()
                                        .where()
                                        .findAllSortedAsync(Constants.DATE, Sort.DESCENDING)
                                , false, true);
                        adapter.setDeleteType(Constants.ActivityType.SERVICE);
                        break;
                    case R.id.nav_expenses:
                        adapter = new ExpenseRecyclerViewAdapter(getContext(),
                                vehicle.getExpenses()
                                        .where()
                                        .findAllSortedAsync(Constants.TYPE, Sort.ASCENDING),
                                false, true);
                        adapter.setDeleteType(Constants.ActivityType.EXPENSE);
                        break;
                    case R.id.nav_refuelings:
                        adapter = new RefuelingRecyclerViewAdapter(getContext(),
                                vehicle.getRefuelings()
                                        .where()
                                        .findAllSortedAsync(Constants.DATE, Sort.DESCENDING)
                                , false, true);
                        adapter.setDeleteType(Constants.ActivityType.REFUELING);
                        break;
                    case R.id.nav_insurances:
                        adapter = new InsuranceRecyclerViewAdapter(getContext(),
                                vehicle.getInsurances()
                                        .where()
                                        .findAllSortedAsync(Constants.DATE, Sort.DESCENDING),
                                false, true);
                        adapter.setDeleteType(Constants.ActivityType.INSURANCE);
                        break;
                }
                if (adapter != null) {
                    adapter.setColor(vehicle.getColor().getColor());
                    adapter.setVehicleId(vehicleId);
                    adapter.setRealmSettings(myRealm.where(RealmSettings.class).findFirst());
                    adapter.addCallback();
                    recyclerView.setAdapter(adapter);
                }
            }
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.removeCallback();
        }else {
            vehicleAdapter.removeCallback();
        }
        myRealm.close();
    }
}
