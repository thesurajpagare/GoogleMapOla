package com.example.surajpagare.googlelocation;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the ResultReceiver.
 */
public class FetchAddressIntentService extends IntentService {
    private static final String TAG = "FetchAddressIS";

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchAddressIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /**
     * Tries to get the location address using a Geocoder. If successful, sends an address to a
     * result receiver. If unsuccessful, sends an error message instead.
     * Note: We define a {@link ResultReceiver} in * MainActivity to process content
     * sent from this service.
     * <p>
     * This service calls this method from the default worker thread with the intent that started
     * the service. When this method returns, the service automatically stops.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
           System.out.println("intent service calls");
        mReceiver = intent.getParcelableExtra(AppUtils.LocationConstants.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }
        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(AppUtils.LocationConstants.LOCATION_DATA_EXTRA);

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if (location == null) {
            errorMessage = getString(R.string.no_location_data_provided);
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(AppUtils.LocationConstants.FAILURE_RESULT, errorMessage, null);
            return;
        }

        // Errors could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indicating failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
       // Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
           // Context ctx=(Context) Main2Activity.class;
            Geocoder geocoder = new Geocoder(FetchAddressIntentService.this, Locale.getDefault());

            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);




            System.out.print("addres size="+addresses.size());



            } catch (IOException ioException) {


           System.out.print("eroor2="+ioException.getMessage());
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            System.out.print("eroor3="+illegalArgumentException.getMessage());
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(AppUtils.LocationConstants.FAILURE_RESULT, errorMessage, null);
        } else {
            System.out.print("hi2");
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();


          // System.out.print("maxaddress="+address.getLocality());

            String strAdd="";
            try {
             //   List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    StringBuilder strReturnedAddress = new StringBuilder("");

                    for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                        addressFragments.add(address.getAddressLine(i));
                    }
                    strAdd = strReturnedAddress.toString();
                    System.out.print("addresss=="+strAdd);
                    Log.w("loction address", strReturnedAddress.toString());
                } else {
                    Log.w("loction address", "No Address returned!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(" loction address", "Canont get Address!");
            }




          /* for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
                System.out.print("hi122=="+address.getAddressLine(i));
            }*/
            deliverResultToReceiver(AppUtils.LocationConstants.SUCCESS_RESULT, strAdd, address);
            //TextUtils.split(TextUtils.join(System.getProperty("line.separator"), addressFragments), System.getProperty("line.separator"));


        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message, Address address) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(AppUtils.LocationConstants.RESULT_DATA_KEY, message);

            bundle.putString(AppUtils.LocationConstants.LOCATION_DATA_AREA, address.getSubLocality());

            bundle.putString(AppUtils.LocationConstants.LOCATION_DATA_CITY, address.getLocality());
            bundle.putString(AppUtils.LocationConstants.LOCATION_DATA_STREET, address.getAddressLine(0));

            mReceiver.send(resultCode, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
