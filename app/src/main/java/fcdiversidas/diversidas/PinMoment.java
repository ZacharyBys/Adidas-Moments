package fcdiversidas.diversidas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.R.attr.bitmap;
import static fcdiversidas.diversidas.TimelinePage.decodeSampledBitmapFromResource;

public class PinMoment extends AppCompatActivity {
    String pinID;
    ArrayList<Bitmap> pictureMap;
    int currentSlide = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        pinID = intent.getStringExtra("pinID");
        pictureMap = new ArrayList<Bitmap>();

        setContentView(R.layout.activity_pin_moment);

        TextView pintitle = (TextView) findViewById(R.id.pintitle);
        pintitle.setText(pinID);

        ImageView imgView = (ImageView) findViewById(R.id.imageview);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNextImage();
            }
        });

        Bitmap bitmap = decodeSampledBitmapFromResource(this.getResources(),R.drawable.map_pin,5,60);
        pictureMap.add(bitmap);
        bitmap = decodeSampledBitmapFromResource(this.getResources(),R.drawable.enter_bg,5,60);
        pictureMap.add(bitmap);
        bitmap = decodeSampledBitmapFromResource(this.getResources(),R.drawable.adidas_moments,5,60);
        pictureMap.add(bitmap);

        setNextImage();
    }

    private void setNextImage() {
        ImageView view = (ImageView) findViewById(R.id.imageview);
        view.setImageBitmap(pictureMap.get(currentSlide));
        currentSlide++;
        if (currentSlide >= pictureMap.size()){
            currentSlide = 0;
        }
    }
}
