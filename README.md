# Szakdolgozat-FMNUMU

Smarthome hub and controller. 

Thesis work of Lovász Ákos.
Eszterházy Károly Katolikus Egyetem.

## The idea

An Orange PI running linux hosts the Node-RED server and the MQTT Broker (Mosquitto), while the Android app is the primary control surface for the system. The Android app's main screen hosts the subscribed topics contained in seperate cards and the connection screen is for configuring the host address (saved between sessions, optimally a one-time setup). From the app all of the topics are freely accessible, and are able to be controlled with hand picked interfaces such as switches, buttons, text inputs etc. You can read more about it in [this document](https://github.com/Lovasz-Akos/Szakdolgozat-FMNUMU/blob/main/Doc/Szakdolgozat%20%C3%96sszegz%C3%A9s.pdf) (hungarian, english release tbd).

## Starting the servers:

### Node-RED Server:

     Auto-configured on the dedicated hardware it's currently running on. (Orange PI)


### MQTT Broker:

     (on the dedicated hardware, config file soon to be uploaded)
     cd /etc
     mosquitto -c mqtt.conf -v
    
>note that the mqtt config contains the host device's static ipv4 address, this may need to be changed for it to function as intended
