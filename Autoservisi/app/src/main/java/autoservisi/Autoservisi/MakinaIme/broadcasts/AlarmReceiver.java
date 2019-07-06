package autoservisi.Autoservisi.MakinaIme.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import autoservisi.Autoservisi.MakinaIme.services.VehicleItemsExpirationService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, VehicleItemsExpirationService.class);
        context.startService(intent1);
    }
}
