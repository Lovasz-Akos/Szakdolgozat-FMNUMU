package com.fmnumu.szakdolgozat.ui.publish;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.fmnumu.szakdolgozat.MainActivity;
import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentPublishBinding;
import com.fmnumu.szakdolgozat.ui.connection.ConnectionFragment;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PublishFragment extends Fragment {

    private PublishViewModel publishViewModel;
    private FragmentPublishBinding binding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPublishBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final EditText textBoxMqttTopic = (EditText) root.findViewById(R.id.textboxMqttTopic);
        final EditText textBoxMqttMessage = (EditText) root.findViewById(R.id.textBoxMqttMessage);

        publishViewModel =
                new ViewModelProvider(this).get(PublishViewModel.class);

        final MqttAndroidClient[] connectMQTT = new MqttAndroidClient[1];

        FragmentManager fm = getFragmentManager();

        ConnectionFragment connectionFragment = (ConnectionFragment) fm.findFragmentById(R.id.nav_connection);

        Button publish = (Button) root.findViewById(R.id.buttonMqttPublish);
        publish.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                String mqttTopic = textBoxMqttTopic.getText().toString();
                String mqttMessage = textBoxMqttMessage.getText().toString();
                publishMessage(mqttMessage, ((MainActivity)getActivity()).getClient(), mqttTopic);
            }
        });

        return root;
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
                            .make(getView(), "Publish success! ad:"+mqttAndroidClient.getServerURI(), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Snackbar snackbar = Snackbar
                            .make(getView(), "Publish failed! status: MQTT disconnected", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            });
        } catch (MqttException e) {
            Log.e("mqttException", e.toString());
            Snackbar snackbar = Snackbar
                    .make(getView(), "Fatal MQTT Error", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        catch (NullPointerException e){
            Log.e("nullPointerException", e.toString());
       /*     Snackbar snackbar = Snackbar
                    .make(getView(), "Publish Failed, please check host address", Snackbar.LENGTH_SHORT);
            snackbar.show();
*/
            Snackbar snackbar2 = Snackbar
                    .make(getView(), "host: "+ mqttAndroidClient.getServerURI(), Snackbar.LENGTH_LONG);
            snackbar2.show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}