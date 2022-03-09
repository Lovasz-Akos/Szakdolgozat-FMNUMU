package com.fmnumu.szakdolgozat.ui.slideshow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fmnumu.szakdolgozat.MainActivity;
import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentConnectionBinding;

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

        Button connect = (Button) root.findViewById(R.id.buttonMqttConnect);
        connect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Log.d("connect onclick", "onClick: CLICKED");
                connectMQTT(root);
            }
        });

        connectionViewModel =
                new ViewModelProvider(this).get(ConnectionViewModel.class);

        return root;
    }


    public void connectMQTT(View view){

        final EditText textBoxMqttAddress = (EditText) view.findViewById(R.id.textboxMqttBrokerAddr);
        final EditText textBoxMqttTopic = (EditText) view.findViewById(R.id.textboxMqttTopic);
        final EditText textBoxMqttMessage = (EditText) view.findViewById(R.id.textBoxMqttMessage);

        String mqttAddress = textBoxMqttAddress.getText().toString();
        String mqttTopic = textBoxMqttTopic.getText().toString();
        String mqttMessage = textBoxMqttMessage.getText().toString();

        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client = new MqttAndroidClient(getContext() , "tcp://"+mqttAddress+":1883", clientId);

        Log.d("MQTT PARAMS", "sendMessageMQTT-ADDRESS-LONG: "+ client.getServerURI().toString());

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
            if (!mqttAndroidClient.isConnected()) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}