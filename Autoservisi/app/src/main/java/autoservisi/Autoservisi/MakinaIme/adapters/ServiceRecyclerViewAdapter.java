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
import autoservisi.Autoservisi.MakinaIme.realm.models.DateNotification;
import autoservisi.Autoservisi.MakinaIme.realm.models.Service;
import autoservisi.Autoservisi.MakinaIme.utils.DateUtils;
import autoservisi.Autoservisi.MakinaIme.utils.MoneyUtils;

public class ServiceRecyclerViewAdapter extends BaseRealmAdapter<Service, ServiceRecyclerViewAdapter.ViewHolder> {

    public ServiceRecyclerViewAdapter(Context context, RealmResults<Service> realmResults,
                                      boolean automaticUpdate, boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
    }

    @Override
    public ServiceRecyclerViewAdapter.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_properties_recycler_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindRealmViewHolder(final ServiceRecyclerViewAdapter.ViewHolder viewHolder, int position) {
        final Service service = realmResults.get(position);
        viewHolder.tvType.setText(service.getType().getName());
        viewHolder.tvDatetime.setText(DateUtils
                .datetimeToString(service.getDate()));
        String text = "Rikujtuesi: ";
        DateNotification dateNotification = service.getDateNotification();
        if (dateNotification == null) {
            text += service.getTargetOdometer() + getRealmSettings().getLengthUnit();
        }else {
            text += DateUtils.dateToString(service.getDateNotification().getDate());
        }
        viewHolder.tvNotifDatetime.setText(text);
        text = MoneyUtils.longToString(new BigDecimal(service.getPrice())) +
                getRealmSettings().getCurrencyUnit();
        viewHolder.tvPrice.setText(text);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewActivity.class);
                intent.putExtra(Constants.ID, getVehicleId());
                intent.putExtra(Constants.ITEM_ID, service.getId());
                intent.putExtra(Constants.TYPE, Constants.ActivityType.SERVICE.ordinal());
                getContext().startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RealmViewHolder{

        public TextView tvType, tvDatetime, tvNotifDatetime, tvPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            View viewColor = itemView.findViewById(R.id.view_row_service_vehicle_color);
            viewColor.setBackgroundColor(getColor());
            tvType = (TextView) itemView.findViewById(R.id.tv_row_service_type);
            tvDatetime = (TextView) itemView.findViewById(R.id.tv_row_service_datetime);
            tvNotifDatetime = (TextView) itemView.findViewById(
                    R.id.tv_row_service_notification_datetime);
            tvPrice = (TextView) itemView.findViewById(R.id.tv_row_service_price);
        }
    }
}
