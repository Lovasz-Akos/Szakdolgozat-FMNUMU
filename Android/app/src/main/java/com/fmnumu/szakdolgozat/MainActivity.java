package com.fmnumu.szakdolgozat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.fmnumu.szakdolgozat.databinding.ActivityMainBinding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        FloatingActionButton fabSend = (FloatingActionButton) findViewById((R.id.fabSend));
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                  //      .setAction("Action", null).show();
                sendMessageMQTT(view);
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void sendMessageMQTT(View view){

        EditText textboxMqttAddress = (EditText)findViewById(R.id.textboxMqttBrokerAddr);
        EditText textboxMqttTopic = (EditText)findViewById(R.id.textboxMqttTopic);
        EditText textboxMqttMessage = (EditText)findViewById(R.id.textboxMqttMessage);

        String mqttAddress = textboxMqttAddress.getText().toString();
        String mqttTopic = textboxMqttTopic.getText().toString();
        String mqttMessage = textboxMqttMessage.getText().toString();

        Log.d("MQTT PARAMS", "sendMessageMQTT-ADDR: "+ mqttAddress);
        Log.d("MQTT PARAMS", "sendMessageMQTT-TOPC: "+ mqttTopic);
        Log.d("MQTT PARAMS", "sendMessageMQTT-MSG: "+ mqttMessage);

        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(), "tcp://"+mqttAddress+":1883", clientId);

        Log.d("MQTT PARAMS", "sendMessageMQTT-ADDR-LONG: "+ client.getServerURI().toString());

        byte[] encodedPayload = new byte[0];
        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("Connection", "connect succeed");

                    publishMessage(mqttMessage, client, mqttTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("Connection", "connect anyád fasza");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void publishMessage(String payload, MqttAndroidClient mqttAndroidClient, String topic) {
        try {
            if (mqttAndroidClient.isConnected() == false) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(topic, message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("Connection", "publish succeed!") ;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("Connection", "publish failed!") ;
                }
            });
        } catch (MqttException e) {
            Log.e("mqttException", e.toString());
            e.printStackTrace();
        }
    }
}