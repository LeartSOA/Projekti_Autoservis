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
import autoservisi.Autoservisi.MakinaIme.realm.models.Expense;
import autoservisi.Autoservisi.MakinaIme.utils.DateUtils;
import autoservisi.Autoservisi.MakinaIme.utils.MoneyUtils;

public class ExpenseRecyclerViewAdapter extends
        BaseRealmAdapter<Expense, ExpenseRecyclerViewAdapter.ViewHolder>
    {

    public ExpenseRecyclerViewAdapter(Context context, RealmResults<Expense> realmResults,
                                      boolean automaticUpdate, boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_properties_recycler_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
        final Expense expense = realmResults.get(position);
        viewHolder.tvType.setText(expense.getType());
        viewHolder.tvDatetime.setText(DateUtils
                .datetimeToString(expense.getDate()));
        String price = MoneyUtils.longToString(new BigDecimal(expense.getPrice()))
                + " " + getRealmSettings().getCurrencyUnit();
        String odometer = expense.getOdometer() + getRealmSettings().getLengthUnit();
        viewHolder.tvOdometer.setText(odometer);
        viewHolder.tvPrice.setText(price);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewActivity.class);
                intent.putExtra(Constants.ID, getVehicleId());
                intent.putExtra(Constants.ITEM_ID, expense.getId());
                intent.putExtra(Constants.TYPE, Constants.ActivityType.EXPENSE.ordinal());
                getContext().startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RealmViewHolder {

        public TextView tvType, tvDatetime, tvPrice, tvOdometer;

        public ViewHolder(View itemView) {
            super(itemView);
            View viewColor = itemView.findViewById(R.id.view_row_service_vehicle_color);
            viewColor.setBackgroundColor(getColor());
            tvType = (TextView) itemView.findViewById(R.id.tv_row_service_type);
            tvDatetime = (TextView) itemView.findViewById(R.id.tv_row_service_datetime);
            tvPrice = (TextView) itemView.findViewById(R.id.tv_row_service_price);
            tvOdometer = (TextView) itemView.findViewById(R.id.tv_row_service_notification_datetime);
        }
    }
}
