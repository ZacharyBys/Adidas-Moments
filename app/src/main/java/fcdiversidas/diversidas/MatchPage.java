package fcdiversidas.diversidas;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MatchPage extends AppCompatActivity {

    public Typeface opensemi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_page);

        opensemi = Typeface.createFromAsset(getAssets(),
                "OpenSans-Semibold.ttf");

        ImageView backbutton = (ImageView) findViewById(R.id.backbutton);
        backbutton.bringToFront();
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MatchPage.this, HomePage.class);
                startActivity(intent);
            }
        });

        TextView title1 = (TextView) findViewById(R.id.title1);
        TextView subtitle1 = (TextView) findViewById(R.id.subtitle1);

        title1.setTypeface(opensemi);
        subtitle1.setTypeface(opensemi);

        title1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MatchPage.this, TimelinePage.class);
                startActivity(intent);
            }
        });

        subtitle1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MatchPage.this, TimelinePage.class);
                startActivity(intent);
            }
        });



    }
}
