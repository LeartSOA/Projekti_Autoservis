package autoservisi.Autoservisi.MakinaIme.activities;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import autoservisi.Autoservisi.MakinaIme.R;


import java.util.List;
import java.util.Locale;

public class Location extends AppCompatActivity {
TextView textView;
Geocoder geocoder;
List<Address>addresses;
Double latitude=42.648491;
Double longitude=21.166971;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

      textView =(TextView)findViewById(R.id.textView3);
      geocoder= new Geocoder(this,Locale.getDefault());
      try{

          addresses=geocoder.getFromLocation(latitude,longitude,1);
          String address=addresses.get(0).getAddressLine(0);
          String area=addresses.get(0).getLocality();
          String City=addresses.get(0).getAdminArea();
          String Country=addresses.get(0).getCountryName();
          String fulladdress=address+" "+area+" ";
          textView.setText(fulladdress);


      }
      catch (Exception e){


        }


    }

}
