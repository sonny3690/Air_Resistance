package com.app.sonny.airresistance;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity {

    double fdrag = 0;
    double initVel = 0;
    double dragCoefficient = 0.3;
    double radius; //in celsius, input
    double temperature; //in celsius, input
    double angle; // in radians
    double dropHeight; //ALWAYS NEGATIVE?
    double mass;
    double time;
    double range;
    Button calcButton;
    private EditText velText, tempText, angText, dropText, massText, radText;
    private TextView resultText;

    //@OVERRIDE
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //INITIALIZE OBJECTS
        velText = (EditText) findViewById(R.id.velocity);
        angText = (EditText) findViewById(R.id.angle);
        dropText = (EditText) findViewById(R.id.dropHeight);
        massText = (EditText) findViewById(R.id.mass);
        radText = (EditText) findViewById(R.id.radius);
        calcButton = (Button) findViewById(R.id.calcButton);
        resultText = (TextView) findViewById(R.id.results);
        tempText = (EditText) findViewById(R.id.temperature);
        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //INITIALIZE VARIABLES
                try {
                    initVel = Double.parseDouble(velText.getText().toString());
                    angle = Double.parseDouble(angText.getText().toString());
                    dropHeight = Double.parseDouble(dropText.getText().toString());
                    mass = Double.parseDouble(massText.getText().toString());
                    temperature = Double.parseDouble(tempText.getText().toString());
                    radius = Double.parseDouble(radText.getText().toString());


                    angle = Math.toRadians(angle); //convert to radians
                    Log.w("VAR", "Angle: " + angle);
                    fdrag = getDragForce(getAirDensity(temperature), initVel, dragCoefficient, getCrossSectionArea(radius));

                    time = getTime(-0.5 * (fdrag * Math.sin(angle) + 9.81), initVel * Math.sin(angle), dropHeight * -1); //calculates quadratic equation

                    fdrag = updateDragForce(0.5 * (initVel + initVel * Math.cos(angle) * time / 2));
                    Log.w("VAR", "Fdrag: " + fdrag);
                    //Log.w("VAR", "QUAD W/0: " + findTime(-4.905, 0, 4.91));

                    range = getRange(initVel, time, angle, fdrag, mass);


                    showResults(range, time, 0);

                }
                catch (Exception e){
                    
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public double getRange(double v, double t, double angle, double fdrag, double m) {
        return v * Math.cos(angle) * t - 0.5 * (fdrag * Math.cos(angle)) * t * t;
    }

    public double getTime(double a, double b, double c) {
        double t = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
        return t;
    }

    public double findTime(double a, double b, double c) {
        double df = this.dragCoefficient;
        double vel = this.initVel;
        double position = dropHeight * -1;
        double time = 0;
        double tick = 1;
        int counter = 0;

        for (double t = 0; t <100000; t += 1) {
            counter  ++;
            vel = vel - (+ 9.81) * t;
            df = updateDragForce(vel);
            position = position + vel * t;
            if (position <=0) {
                Log.w("POSITION", "pos: " +  position + "| time: " +  t);
                time = t;
                t = 1000001;
            }

        }

        Log.w("COUNTER", "" +counter);

        //fdrag = getDragForce(getAirDensity(temperature), initVel, dragCoefficient, getCrossSectionArea(radius));
        return time;
    }

    public double getCrossSectionArea(double radius) {
        return Math.PI * radius * radius;
    }

    public double getDragForce(double p, double v, double c, double a) {

        return (0.5 * p * c * a / mass) * v;
    }


    public double getAirDensity(double temperature) {
        return 101325 / (287.05 * (273.15 + temperature));
    }

    public double updateDragForce(double vel) {
        return getDragForce(getAirDensity(temperature), vel, dragCoefficient, getCrossSectionArea(radius));
    }


    public void showResults(double range, double time, double reynolds) {
        resultText.setText("Range: " + range + "\n" + "Time: " + time + "\n" + "Reynolds Number: " + getReynoldsNumber()

        + "\n" + "FDRAG: " + fdrag
        );
    }

    public double getReynoldsNumber (){
        return getAirDensity(temperature) * (0.5 * (initVel + initVel * Math.cos(angle)*time/2)) * radius *2/3.5;
    }

}
