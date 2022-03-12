# Linkek és jegyzetek

## Ecliplse mosquitto

[MQTT Broker](https://mosquitto.org/download/) for self hosting mqtt server. This way I can use any device to handle mqtt packets

## Node.js

[Node.js](https://nodejs.org/en/). Needed to install Node-red on Windows.

## Node-RED

[Node-RED](https://nodered.org/) is the entire back-end of the project

## MQTT broker config help

[Broker config with examples](http://www.steves-internet-guide.com/mossquitto-conf-file/)

[Broker config documentation](https://mosquitto.org/man/mosquitto-conf-5.html)

## Node-RED Web UI

[node-red-dashboard](https://flows.nodered.org/node/node-red-dashboard)
good for basic interaction and rapid deployment and testing of features

## MQTT alap docs

[MQTT docs](https://www.hivemq.com/mqtt-essentials/)

## Starting the servers

This is automated on the deployment hardware.

```bash
ssh root@{IP of server}
pw: funstar3d //note that this is only applicable to my hardware
```

The web UI is hosted on {IP of server}:1880 and is accessible from any device on the network

### Node-RED Server

```bash
cd .\node_modules\node-red\
node red
```

### MQTT Broker

```bash
mosquitto -c mqtt.conf -v
```

 >note that the mqtt config contains the host device's static ipv4 address, this may need to be changed for it to function as intended

## Android basics

[Fundamentals](https://developer.android.com/guide/components/fundamentals)

### Android notes

- App components:
  - Activity
    - The primary entrypoint when opening the app from the icon, and depending on setting other sources as well, such as:
      - Notifications
      - Widgets
      - Links
    - Activities have UI attached to them, so it's the primary interaction point with the user.
  - Service
    - General-purpose entry point
      - Does not provide UI, could be some of the following:
        - Media playback in the background
        - Data syncing
        - APIs
      - The user is usually not aware of most service components
      - The service runs until it's job is complete
  - Broadcast Receiver
    - Primarily used as the system's entry point from outside, this is where the app receives commands to perform an action that wasn't directly started by the user
  - Content Provider
    - Database management
    - Data access ot the app from file system
    - Private data streaming

The AndroidManifest.xml contains all of the components the app uses, so it must declare every required component for the system can start the app components.

## Linux notes

Access to file that contains startup scripts

```bash
sudo crontab -e
```

Startup manager

```bash
sysv-rc-conf
```

## Tesztelés

[Node-RED testing](https://www.technicalfeeder.com/2021/02/how-to-write-node-red-flow-test/)

[Android testing]([https://link](https://developer.android.com/training/testing/fundamentals))

## MQTT Android basics

[Eclipse docs](https://www.eclipse.org/paho/index.php?page=clients/python/docs/index.php)

[Hive quickstart guide](https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/)

## Android persistent storage

[Prefrences](https://developer.android.com/guide/topics/ui/settings/use-saved-values)
