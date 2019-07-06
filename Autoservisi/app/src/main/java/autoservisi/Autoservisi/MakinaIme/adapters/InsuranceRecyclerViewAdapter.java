package autoservisi.Autoservisi.MakinaIme.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;

import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import autoservisi.Autoservisi.MakinaIme.R;
import autoservisi.Autoservisi.MakinaIme.activities.ViewActivity;
import autoservisi.Autoservisi.MakinaIme.realm.Constants;
import autoservisi.Autoservisi.MakinaIme.realm.models.Insurance;
import autoservisi.Autoservisi.MakinaIme.utils.DateUtils;
import autoservisi.Autoservisi.MakinaIme.utils.MoneyUtils;

public class InsuranceRecyclerViewAdapter extends BaseRealmAdapter<Insurance,
        InsuranceRecyclerViewAdapter.ViewHolder> {

    public InsuranceRecyclerViewAdapter(Context context, RealmResults<Insurance> realmResults,
                                        boolean automaticUpdate, boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
    }

    @Override
    public InsuranceRecyclerViewAdapter.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_insurances_recycler_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindRealmViewHolder(InsuranceRecyclerViewAdapter.ViewHolder viewHolder, int position) {
        final Insurance insurance = realmResults.get(position);
        String text = String.format(getContext().getString(R.string.date_placeholder), DateUtils.datetimeToString(insurance.getDate()));
        viewHolder.tvCompany.setText(insurance.getCompany().getName());
        viewHolder.tvDate.setText(text);
        text = String.format(getContext().getString(R.string.expiration_date_placeholder),
                DateUtils.dateToString(insurance.getNotification().getDate()));
        viewHolder.tvExpirationDate.setText(text);
        text = String.format(getContext().getString(R.string.price_placeholder),
                MoneyUtils.longToString(new BigDecimal(insurance.getPrice())));
        viewHolder.tvPrice.setText(text + " " + getRealmSettings().getCurrencyUnit());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewActivity.class);
                intent.putExtra(Constants.ID, getVehicleId());
                intent.putExtra(Constants.ITEM_ID, insurance.getId());
                intent.putExtra(Constants.TYPE, Constants.ActivityType.INSURANCE.ordinal());
                getContext().startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RealmViewHolder{

        public TextView tvCompany, tvDate, tvExpirationDate, tvPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            View viewColor = itemView.findViewById(R.id.view_row_insurance_vehicle_color);
            viewColor.setBackgroundColor(getColor());
            tvCompany = (TextView) itemView.findViewById(R.id.tv_row_insurance_company);
            tvDate = (TextView) itemView.findViewById(R.id.tv_row_insurance_date);
            tvExpirationDate = (TextView) itemView.findViewById(R.id.tv_row_insurance_expiration_date);
            tvPrice = (TextView) itemView.findViewById(R.id.tv_row_insurance_price);
        }
    }
}
