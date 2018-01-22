package fcdiversidas.diversidas;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.gotev.uploadservice.UploadService;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Exchanger;
import java.util.concurrent.RunnableFuture;

import io.socket.emitter.Emitter;

import static java.lang.System.out;

public class TimelinePage extends AppCompatActivity {

    Initializer initializer;
    WebSocketHelper webSocketHelper;
    ArrayList<TimelinePin> pinArray;
    public Typeface texgyBold;
    boolean pressedMoment;
    boolean toggleVid = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_page);

        texgyBold = Typeface.createFromAsset(getAssets(),
                "texgyreadventor-bold.otf");
        pressedMoment = true;
        initializer = new Initializer();
        webSocketHelper = new WebSocketHelper();
        pinArray = new ArrayList<TimelinePin>();

        ImageView backbutton = (ImageView) findViewById(R.id.backbutton);
        backbutton.bringToFront();
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimelinePage.this, MatchPage.class);
                startActivity(intent);
            }
        });

        Button highlightbutton = (Button) findViewById(R.id.highlightbutton);
        highlightbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pressedMoment) {
                    ImageView img = (ImageView) findViewById(R.id.highlight);
                    img.setVisibility(View.VISIBLE);
                    img.bringToFront();
                    pressedMoment = false;
                    return;
                }

                VideoView videoview = (VideoView) findViewById(R.id.highlightvideo);

                if (toggleVid) {
                    videoview.setVisibility(View.VISIBLE);
                    videoview.bringToFront();
                    String path = "android.resource://" + getPackageName() + "/" + R.raw.zachvideo;
                    videoview.setVideoURI(Uri.parse(path));
                    videoview.start();
                    toggleVid = false;
                } else {
                    videoview.stopPlayback();
                    videoview.setVisibility(View.GONE);
                    toggleVid = true;
                }
            }
        });

        webSocketHelper.socket.on("pins", onNewPin);

        receivePins();

        TextView matchinfo = (TextView) findViewById(R.id.matchinfo);
        TextView halftime = (TextView) findViewById(R.id.halftime);

        halftime.setTypeface(texgyBold);
        matchinfo.bringToFront();
        matchinfo.setTypeface(texgyBold);
    }

    private Emitter.Listener onNewPin = new Emitter.Listener(){

        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long timestamp = 0;
                    String pinid = null;
                    String type = null;
                    int size = 0;
                    JSONObject data = (JSONObject) args[0];
                    try {
                        timestamp = data.getLong("time");
                        pinid = data.getString("_id");
                        type = data.getString("type");
                        size = data.getInt("size");
                    } catch (Exception e){

                    }
                    //append the pin
                    TimelinePin newPin = new TimelinePin(timestamp, pinid, type, size);
                    pinArray.add(newPin);
                    addPinToTimeline(newPin);
                    Log.d("pininfo", Long.toString(timestamp));

                }
            });

        }
    };

   /* private void addImageToTimeline(int position, Bitmap image){
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(image);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.timeline);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp.addRule(RelativeLayout.BELOW, lastImageID );
        lp.setMargins(75,position,0,0);
        rl.addView(iv, lp);
    }*/

    private void addPinToTimeline(final TimelinePin pin){
        final Context cont = this;
        final String pinID = pin.pinid;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageButton ib = new ImageButton(cont);
                ib.setMaxWidth(62);
                ib.setMaxHeight(30);
                RelativeLayout rl = (RelativeLayout) findViewById(R.id.timeline);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                lp.addRule(RelativeLayout.BELOW, R.id.timelineBase);
                int width = 90;
                int height = 45;

                /*if (pin.pinSize > 3){
                    width *= 1.1;
                } else if (pin.pinSize > 7){
                    width *= 1.2;
                }*/
                int id = R.drawable.first_goal;
                if (!(pin.type == null) && pin.type.equals("goal")) id = R.drawable.first_goal;
                else if (!(pin.type == null) && pin.type.equals("card")) id = R.drawable.cardpin;
                //Bitmap icon = decodeSampledBitmapFromResource(cont.getResources(),id,width, height);
                Bitmap ic = BitmapFactory.decodeResource(cont.getResources(), id);
                Bitmap icon = Bitmap.createScaledBitmap(ic, 414, 200, false);

                ImageView imgV = (ImageView) findViewById(R.id.topbackground);
                ib.setImageBitmap(icon);
                double percentage = ((int) pin.timestamp)/95.;
                double position = ((rl.getHeight()-imgV.getHeight())* percentage)-15;
                lp.setMargins(110,(int) position + 120 ,0,0);
                ib.setBackgroundColor(Color.TRANSPARENT);
                rl.addView(ib, lp);
                ib.bringToFront();

                TextView minute = new TextView(cont);
                minute.setText(Long.toString(pin.timestamp) + "'");
                minute.setTextColor(getResources().getColor(R.color.numbers));
                RelativeLayout.LayoutParams newlp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                minute.setTypeface(texgyBold);
                newlp.setMargins(215, (int) position+230 ,0,0 );
                rl.addView(minute, newlp);

                ib.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(TimelinePage.this, PinMoment.class);
                        intent.putExtra("pinID", pinID);
                        intent.putExtra("time", Long.toString(pin.timestamp));
                        startActivity(intent);
                    }
                });
            }
        });
    }

    private void receivePins(){
        //pinArray;
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://ec2-54-236-246-164.compute-1.amazonaws.com:3000/pins";
        StringRequest request = new StringRequest(Request.Method.GET,url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray array = new JSONArray();
                try {
                    array = new JSONArray(response);
                } catch (Exception e){

                }
                Log.d("response", response);
                for(int i =0;i<array.length();i++){
                    try {
                        long time = array.getJSONObject(i).getLong("time");
                        String id = array.getJSONObject(i).getString("_id");
                        String pintype = array.getJSONObject(i).getString("type");
                        int pinSize = array.getJSONObject(i).getInt("size");
                        TimelinePin pin = new TimelinePin(time,id,pintype, pinSize);
                        pinArray.add(pin);
                    } catch (Exception e){
                        Log.d("errooooor", e.getMessage());
                    }
                }

                for (TimelinePin pin : pinArray) {
                    addPinToTimeline(pin);
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

            }


        });
        queue.add(request);
        Log.d("request", "Request Sent");
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
