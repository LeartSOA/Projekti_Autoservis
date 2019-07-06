package autoservisi.Autoservisi.MakinaIme.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.interfaces.IBaseActivity;
import autoservisi.Autoservisi.MakinaIme.realm.models.FuelTank;
import autoservisi.Autoservisi.MakinaIme.utils.TextUtils;

public class NewFuelTankDialog extends DialogFragment implements IBaseActivity {

    private View view;
    private Spinner spnFuelTankFuelType;
    private TextInputLayout tilFTCapacity;
    private TextInputLayout tilFTConsumption;
    private Button btnAdd;
    private OnAddFuelTankListener listener;
    private ArrayList<String> possibleFuelTypes;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        initComponents();
        setComponentListeners();
        setContent();
        builder.setCancelable(true);
        builder.setView(view);
        return builder.create();
    }

    @SuppressLint("InflateParams")
    @Override
    public void initComponents() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_new_fuel_tank, null);
        spnFuelTankFuelType = (Spinner) view.findViewById(R.id.spn_new_ft_fuel_type);
        tilFTCapacity = (TextInputLayout) view.findViewById(R.id.til_new_ft_capacity);
        tilFTConsumption = (TextInputLayout) view.findViewById(R.id.til_new_ft_consumption);
        btnAdd = (Button) view.findViewById(R.id.btn_dialog_new_ft_add);
    }

    @Override
    public void setComponentListeners() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInputValid()) {
                    FuelTank fuelTank = new FuelTank();
                    fuelTank.setType(spnFuelTankFuelType.getSelectedItem().toString());
                    fuelTank.setCapacity(Integer.parseInt(TextUtils.getTextFromTil(tilFTCapacity)));
                    fuelTank.setConsumption(Integer.parseInt(TextUtils.getTextFromTil(tilFTConsumption)));
                    listener.onAddFuelTank(fuelTank);
                }
            }
        });
    }

    @Override
    public void setContent() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                R.layout.textview_spinner, possibleFuelTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFuelTankFuelType.setAdapter(spinnerAdapter);
    }

    private boolean isInputValid() {
        boolean valid = true;
        if (TextUtils.getTextFromTil(tilFTCapacity).length() < 1) {
            valid = false;
            tilFTCapacity.setError("Kapaciteti gabim");
        }
        if (TextUtils.getTextFromTil(tilFTConsumption).length() < 1) {
            valid = false;
            tilFTConsumption.setError("Konsumi gabim");
        }
        return valid;
    }

    public void setPossibleFuelTypes(ArrayList<String> possibleFuelTypes) {
        this.possibleFuelTypes = possibleFuelTypes;
    }

    public void setListener(OnAddFuelTankListener listener) {
        this.listener = listener;
    }

    public interface OnAddFuelTankListener{
        void onAddFuelTank(FuelTank fuelTank);
    }
}
