package org.reapbenefit.gautam.intern.potholedetectorbeta;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.reapbenefit.gautam.intern.potholedetectorbeta.Activities.MapsActivity;
import org.reapbenefit.gautam.intern.potholedetectorbeta.Core.ApplicationClass;
import org.reapbenefit.gautam.intern.potholedetectorbeta.Core.UploadTasksService;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by gautam on 26/01/17.
 */

public class TripListAdapter extends ArrayAdapter<Trip> {

    private final Context context;

    private static ArrayList<Trip> trips;
    private Uri uploadFileUri;
    ApplicationClass app;

    public TripListAdapter(Context context, ArrayList<Trip> values) {
        super(context, -1, values);
        this.context = context;
        trips = new ArrayList<>(values);
        app = ApplicationClass.getInstance();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.trip_list_item, parent, false);

        String countString, timeString, durationString, distanceString;
        ImageButton uploadButton, uploadedTick, mapButton;

        if(!trips.isEmpty()){
            try {
                final Trip trip = trips.get(position);
                countString = String.valueOf(trip.getPotholeCount()) + " potholes";
                timeString = trip.getStartTime();
                timeString = timeString.substring(4, timeString.indexOf("GMT") - 4);
                durationString = String.valueOf(trip.getDuration()) + " mins, "  +  humanReadableByteCount(trip.getFilesize(), true);
                distanceString = String.valueOf(roundTwoDecimals(trip.getDistanceInKM())) + "km";
                Log.d("special_case", distanceString);
                uploadButton = (ImageButton) rowView.findViewById(R.id.upload_button);
                uploadedTick = (ImageButton) rowView.findViewById(R.id.upload_tick);
                mapButton = (ImageButton) rowView.findViewById(R.id.map_button);

                if(trip.isUploaded()){
                    uploadButton.setVisibility(View.GONE);
                    uploadedTick.setVisibility(View.VISIBLE);
                }

                uploadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadFileUri = Uri.fromFile(new File(context.getApplicationContext().getFilesDir() + "/logs/" + trip.getTrip_id() + ".csv"));
                        startUploadService();
                    }
                });

                mapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File file = new File(context.getApplicationContext().getFilesDir(), "analysis/" + trip.getTrip_id() + ".csv");
                        if(!app.isTripInProgress() && file.exists()){ // check if file of same name is available in the analytics folder
                            Intent i = new Intent(context, MapsActivity.class);
                            i.putExtra("trip", trip);
                            context.startActivity(i);
                        }else{
                            //Toast.makeText(getActivity(), )
                        }
                    }
                });

            }catch (NullPointerException e){
                countString = "null";
                timeString = "null";
                durationString = "null";
                distanceString = "null";
            }

            TextView date = (TextView) rowView.findViewById(R.id.count);
            TextView time = (TextView) rowView.findViewById(R.id.start_time);
            TextView size = (TextView) rowView.findViewById(R.id.size);
            TextView distance = (TextView) rowView.findViewById(R.id.distance_view);
            date.setText(countString);
            time.setText(timeString);
            size.setText(durationString);
            distance.setText(distanceString);
        }

        return rowView;

    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
    }

    float roundTwoDecimals(float f) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Float.valueOf(twoDForm.format(f));
    }

    public void startUploadService(){
        Intent intent = new Intent(getContext(), UploadTasksService.class);
        intent.setAction("upload_now");
        intent.putExtra("upload_uri", uploadFileUri);
        this.getContext().startService(intent);

    }

}