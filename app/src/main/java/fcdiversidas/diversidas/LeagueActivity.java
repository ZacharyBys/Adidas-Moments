package fcdiversidas.diversidas;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LeagueActivity extends AppCompatActivity {
    public Typeface opensans;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league);

        ImageView backbutton = (ImageView) findViewById(R.id.backbutton);
        backbutton.bringToFront();
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeagueActivity.this, HomePage.class);
                startActivity(intent);
            }
        });

        opensans = Typeface.createFromAsset(getAssets(),
                "OpenSans-Bold.ttf");

        TextView choose = (TextView) findViewById(R.id.choose);
        choose.setTypeface(opensans);

        ImageView bundes = (ImageView) findViewById(R.id.bundes);
        bundes.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LeagueActivity.this, MatchPage.class);
                startActivity(intent);
            }
        });

    }

}
