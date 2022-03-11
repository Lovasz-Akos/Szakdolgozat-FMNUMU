package com.fmnumu.szakdolgozat.ui.slideshow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentConnectionBinding;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ConnectionFragment extends Fragment{


    private ConnectionViewModel connectionViewModel;
    private FragmentConnectionBinding binding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConnectionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final EditText textBoxMqttAddress = (EditText) root.findViewById(R.id.textboxMqttBrokerAddr);
        final EditText textBoxMqttTopic = (EditText) root.findViewById(R.id.textboxMqttTopic);
        final EditText textBoxMqttMessage = (EditText) root.findViewById(R.id.textBoxMqttMessage);

        final MqttAndroidClient[] connectMQTT = new MqttAndroidClient[1];


        Button connect = (Button) root.findViewById(R.id.buttonMqttConnect);
        connect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                String mqttAddress = textBoxMqttAddress.getText().toString();
                connectMQTT[0] = connectMQTT(root, mqttAddress);
            }
        });

        Button publish = (Button) root.findViewById(R.id.buttonMqttPublish);
        publish.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                String mqttTopic = textBoxMqttTopic.getText().toString();
                String mqttMessage = textBoxMqttMessage.getText().toString();

                publishMessage(mqttMessage,  connectMQTT[0], mqttTopic);
            }
        });

        connectionViewModel =
                new ViewModelProvider(this).get(ConnectionViewModel.class);

        return root;
    }


    public MqttAndroidClient connectMQTT(View view, String mqttAddress){


        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client = new MqttAndroidClient(getContext() , "tcp://"+mqttAddress+":1883", clientId);

        Log.d("MQTT PARAMS", "sendMessageMQTT-ADDRESS-LONG: "+ client.getServerURI().toString());

        byte[] encodedPayload = new byte[0];
        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    //publishMessage(mqttMessage, client, mqttTopic);
                    Snackbar snackbar = Snackbar
                            .make(view, "Connection Success", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    Snackbar snackbar = Snackbar
                            .make(view, "Connection Failed", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });

        } catch (MqttException e) {
            Snackbar snackbar = Snackbar
                    .make(view, "Connection failed, please check host address", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        return client;
    }

    public void publishMessage(String payload, MqttAndroidClient mqttAndroidClient, String topic) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(topic, message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("Connection", "publish succeed!");
                    Log.i("Connection", "payload: " + payload);
                    Log.i("Connection", "client: "+ mqttAndroidClient.getClientId());
                    Log.i("Connection", "topic: " + topic);

                    Snackbar snackbar = Snackbar
                            .make(getView(), "Publish success!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Snackbar snackbar = Snackbar
                            .make(getView(), "Publish failed!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
        } catch (MqttException e) {
            Log.e("mqttException", e.toString());
            Snackbar snackbar = Snackbar
                    .make(getView(), "Fatal MQTT Error", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        catch (NullPointerException e){
            Log.e("nullPointerException", e.toString());
            Snackbar snackbar = Snackbar
                    .make(getView(), "Publish Failed, please check host address", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}