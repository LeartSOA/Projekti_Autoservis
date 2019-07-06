package autoservisi.Autoservisi.MakinaIme.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.ViewActivity;
import autoservisi.Autoservisi.MakinaIme.realm.Constants;
import autoservisi.Autoservisi.MakinaIme.realm.models.FuelTank;
import autoservisi.Autoservisi.MakinaIme.realm.models.Refueling;
import autoservisi.Autoservisi.MakinaIme.utils.MoneyUtils;

public class RefuelingRecyclerViewAdapter extends BaseRealmAdapter<Refueling,
        RefuelingRecyclerViewAdapter.ViewHolder> {

    public RefuelingRecyclerViewAdapter(Context context, RealmResults<Refueling> realmResults, boolean automaticUpdate, boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
    }

    @Override
    public RefuelingRecyclerViewAdapter.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_refuelings_recycler_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindRealmViewHolder(RefuelingRecyclerViewAdapter.ViewHolder viewHolder, int position) {
        final Refueling refueling = realmResults.get(position);
        String text;
        Realm myRealm = Realm.getDefaultInstance();
        FuelTank fuelTank = refueling.getFuelTank();
        viewHolder.tvFuelType.setText(fuelTank.getType());
        text = "%d " + fuelTank.getUnit();
        myRealm.close();
        text = String.format(text, refueling.getQuantity());
        viewHolder.tvQuantity.setText(text);
        text = String.format(getContext().getString(R.string.fuel_price_placeholder),
                MoneyUtils.longToString(new BigDecimal(refueling.getFuelPrice())));
        String currencyUnit = " " + getRealmSettings().getCurrencyUnit();
        text += currencyUnit;
        viewHolder.tvFuelPrice.setText(text);
        text = String.format(getContext().getString(R.string.price_placeholder),
                MoneyUtils.longToString(new BigDecimal(refueling.getPrice())));
        text += currencyUnit;
        viewHolder.tvPrice.setText(text);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewActivity.class);
                intent.putExtra(Constants.ID, getVehicleId());
                intent.putExtra(Constants.ITEM_ID, refueling.getId());
                intent.putExtra(Constants.TYPE, Constants.ActivityType.REFUELING.ordinal());
                getContext().startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RealmViewHolder{

        public TextView tvFuelType, tvFuelPrice, tvQuantity, tvPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            View viewColor = itemView.findViewById(R.id.view_row_refueling_vehicle_color);
            viewColor.setBackgroundColor(getColor());
            tvFuelType = (TextView) itemView.findViewById(R.id.tv_row_refueling_fuel_type);
            tvFuelPrice = (TextView) itemView.findViewById(R.id.tv_row_refueling_fuel_price);
            tvQuantity = (TextView) itemView.findViewById(R.id.tv_row_refueling_quantity);
            tvPrice = (TextView) itemView.findViewById(R.id.tv_row_refueling_price);
        }
    }
}
