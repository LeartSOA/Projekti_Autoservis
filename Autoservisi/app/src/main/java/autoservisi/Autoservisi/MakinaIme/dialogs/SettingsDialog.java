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

import org.apache.commons.lang3.math.NumberUtils;

import io.realm.Realm;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.interfaces.INewBaseActivity;
import autoservisi.Autoservisi.MakinaIme.realm.models.RealmSettings;
import autoservisi.Autoservisi.MakinaIme.utils.TextUtils;
import autoservisi.Autoservisi.MakinaIme.utils.ValidationUtils;

public class SettingsDialog extends DialogFragment implements INewBaseActivity{

    private View view;
    private Spinner spnLength, spnCurrency;
    private TextInputLayout tilDistanceInAdvance;
    private Button btnSave;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        initComponents();
        setComponentListeners();
        setContent();
        builder.setView(view);
        return builder.create();
    }

    @SuppressLint("InflateParams")
    @Override
    public void initComponents() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_settings, null);
        spnLength = (Spinner) view.findViewById(R.id.spn_settings_length);
        spnCurrency = (Spinner) view.findViewById(R.id.spn_settings_currency);
        tilDistanceInAdvance = (TextInputLayout) view.findViewById(R.id.til_settings_distance_advance);
        btnSave = (Button) view.findViewById(R.id.btn_settings_save);
    }

    @Override
    public void setComponentListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInputValid()) {
                    saveToRealm();
                    dismiss();
                }
            }
        });
    }

    @Override
    public void setContent() {
        Realm myRealm = Realm.getDefaultInstance();
        RealmSettings settings = myRealm.where(RealmSettings.class).findFirst();
        TextUtils.setTextToTil(tilDistanceInAdvance, String.valueOf(settings.getDistanceInAdvance()));
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
                .createFromResource(getContext(), R.array.length_unit, R.layout.textview_spinner);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLength.setAdapter(spinnerAdapter);
        spnLength.setSelection(spinnerAdapter.getPosition(settings.getLengthUnit()));
        spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.textview_spinner,
                new CharSequence[]{"BGN"});
        spnCurrency.setAdapter(spinnerAdapter);
        myRealm.close();
    }

    @Override
    public boolean isInputValid() {
        boolean valid = true;
        String text = TextUtils.getTextFromTil(tilDistanceInAdvance);
        if (!ValidationUtils.isNumeric(text)) {
            tilDistanceInAdvance.setError("Pritet numer");
            valid = false;
        }else {
            if (NumberUtils.createInteger(text).compareTo(NumberUtils.INTEGER_ZERO) < 0) {
                tilDistanceInAdvance.setError("Vlera negative nuk lejohet");
                valid = false;
            }
        }
        return valid;
    }

    @Override
    public void saveToRealm() {
        final Realm myRealm = Realm.getDefaultInstance();
        myRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmSettings settings = realm.where(RealmSettings.class).findFirst();
                settings.setDistanceInAdvance(NumberUtils
                        .createInteger(TextUtils.getTextFromTil(tilDistanceInAdvance)));
                settings.setLengthUnit(spnLength.getSelectedItem().toString());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                myRealm.close();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
                myRealm.close();
            }
        });
    }
}
