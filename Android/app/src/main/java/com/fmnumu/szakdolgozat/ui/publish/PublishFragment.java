package com.fmnumu.szakdolgozat.ui.publish;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fmnumu.szakdolgozat.MainActivity;
import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentPublishBinding;
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

        final EditText textBoxMqttTopic = root.findViewById(R.id.textboxMqttTopic);
        final EditText textBoxMqttMessage = root.findViewById(R.id.textBoxMqttMessage);

        publishViewModel =
                new ViewModelProvider(this).get(PublishViewModel.class);

        Button publish = root.findViewById(R.id.buttonMqttPublish);
        publish.setOnClickListener(view -> {
            String mqttTopic = textBoxMqttTopic.getText().toString();
            String mqttMessage = textBoxMqttMessage.getText().toString();
            publishMessage(mqttMessage, ((MainActivity) getActivity()).getClient(), mqttTopic);
        });

        Button subscribe = root.findViewById(R.id.buttonMqttSubscribe);
        subscribe.setOnClickListener(view -> {
            String mqttTopic = textBoxMqttTopic.getText().toString();
            subscribeMQTT(((MainActivity) getActivity()).getClient(), mqttTopic);
        });

        Button unsubscribe = root.findViewById(R.id.buttonMqttUnsubscribe);
        unsubscribe.setOnClickListener(view -> {
            String mqttTopic = textBoxMqttTopic.getText().toString();
            unsubscribeMQTT(((MainActivity) getActivity()).getClient(), mqttTopic);
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
            mqttAndroidClient.publish(topic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Snackbar snackbar = Snackbar
                            .make(getView(), "Publish success! ad:" + mqttAndroidClient.getServerURI(), Snackbar.LENGTH_SHORT);
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
        } catch (NullPointerException e) {
            Log.e("nullPointerException", e.toString());
            Snackbar snackbar = Snackbar
                    .make(getView(), "Publish Failed, please check host address", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    public void subscribeMQTT(MqttAndroidClient mqttAndroidClient, String topic) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }
            mqttAndroidClient.subscribe(topic, 0, getContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast toast = Toast.makeText(getContext(), "Subscribed to " + topic, Toast.LENGTH_SHORT);
                    toast.show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Snackbar snackbar = Snackbar
                            .make(getView(), "Failed to subscribe to topic: " + topic, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            });
        } catch (Exception e) {
            Log.d("tag", "Error :" + e);
        }
    }

    public void unsubscribeMQTT(MqttAndroidClient mqttAndroidClient, String topic) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }
            IMqttToken unsubToken = mqttAndroidClient.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Snackbar snackbar = Snackbar
                            .make(getView(), "Unsubscribed from topic " + topic, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Snackbar snackbar = Snackbar
                            .make(getView(), topic + " topic unsubscribe failed!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            });
        } catch (MqttException e) {
            Log.d("tag", "Error :" + e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}