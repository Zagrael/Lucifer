package com.example.aymerick.gameball;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

public class MainActivity extends Activity implements SensorEventListener {

    // Capteurs
    SensorManager sensorManager;
    Sensor accelerometer;
    float ax, ay;

    // Image
    ImageView ball;
    int ballWidth, ballHeight;
    float vx = 1, vy = 1;
    float x, y;

    Vector<ImageView> obstacles = new Vector<>();
    int initialPosition[] = {-400, -10, -200};
    float speed = 5;

    int score;
    TextView displayScore;
    long t1, t2;


    // Écran
    Display display;
    int screenWidth, screenHeight;
    Button replay;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération capteur
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Récupération écran
        display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth(); // Fonction barrée car non recommandée (meilleure alternative existante)
        screenHeight = display.getHeight();

        // Récupération image
        ball = (ImageView)findViewById(R.id.ball);
            // Position temporaire
            x = screenWidth / 2 - ballWidth / 2;
            y = screenHeight / 2 - ballHeight / 2;
        vx = 1;
        vy = 1;
        obstacles.clear();
        obstacles.addElement((ImageView)findViewById(R.id.obstacle1));
        obstacles.addElement((ImageView)findViewById(R.id.obstacle2));
        obstacles.addElement((ImageView)findViewById(R.id.obstacle3));
            for (int i = 0; i < 3; i++) {
                obstacles.get(i).setX(screenWidth * (i + 1) / 4);
                obstacles.get(i).setY(initialPosition[i]);
            }

        score = 20;
        displayScore = (TextView)findViewById(R.id.score);
        replay = (Button)findViewById(R.id.tryAgain);
        replay.setVisibility(View.INVISIBLE);
        replay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                score = 20;
                onCreate(savedInstanceState);
            }
        });

        // Activation de l'accéléromètre
        onResume();

        t1 = System.currentTimeMillis() / 1000;
    }

    // Désactivation de l'écouteur de l'accéléromètre quand l'application est en arrière-plan
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // Réactivation de l'écouteur de l'accéléromètre quand l'application est en premeir plan
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && score > 0) {

            // Récupération de la taille de l'image
            ballWidth = ball.getWidth();
            ballHeight = ball.getHeight();

            // Application en fonction des valeurs de l'accéléromètre (dans event.values[])
                // Calcul des accélérations
                ax = - 200 * event.values[0] * (float) 9.81 / 9;
                ay = 200 * event.values[1] * (float) 9.81 / 9;

                // Calcul des vitesses (v = v0 + a * t)
                vx += ax / 50;
                vy += ay / 50;

                // Calcul des positions (x = x0 + v0 * t + a * t² / 2
                x += vx / 50 + ax / 5000;
                y += vy / 50 + ay / 5000;

            // Gestion des rebonds
            if (x < 0) {
                x = 0;
                vx = -vx / 2;
            }
            else if (x + ballWidth > screenWidth) {
                x = screenWidth - ballWidth;
                vx = -vx / 2;
            }
            if (y < 0) {
                y = 0;
                vy = -vy / 2;
            }
            else if (y + ballHeight > screenHeight) {
                y = screenHeight - ballHeight;
                vy = -vy / 2;
            }

            // Positionnement de la balle
            ball.setX(x);
            ball.setY(y);

            // Déplacement des obstacles
            movingObstacles();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Actualisation des obstacles + gestion du score
    protected void movingObstacles() {
        for (int i = 0 ; i < 3; i++) {
            if (collision(ball, obstacles.get(i))) {
                score--;
                displayScore.setText(String.valueOf(score));
                obstacles.get(i).setY(initialPosition[i]);
            }
            else if (bottom(obstacles.get(i)) < screenHeight)
                obstacles.get(i).setY(obstacles.get(i).getY() + speed);
            else
                obstacles.get(i).setY(initialPosition[i]);
        }
        if (score == 0) {
            t2 = System.currentTimeMillis() / 1000;
            String str = "Game Over !\nScore : " + ((t2 - t1) * (t2 - t1));
            displayScore.setText(str);
            replay.setVisibility(View.VISIBLE);
        }
    }

    // Gestion des collisions
    protected boolean collision(ImageView b, ImageView o) {
        return left(b) <= right(o) && right(b) >= left(o) && top(b) <= bottom(o) && bottom(b) >= top(o);
    }

    private double left(ImageView I)
    {
        return I.getX();
    }

    private double right(ImageView I)
    {
        return I.getX() + I.getWidth();
    }

    private double top(ImageView I)
    {
        return I.getY();
    }

    private double bottom(ImageView I)
    {
        return I.getY() + I.getHeight();
    }
}
