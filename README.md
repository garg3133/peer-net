# Peer-net 
Peer-to-peer communication without internet

## Progress till now
A basic application which can find peers available on the same network (who are using **peernet** app and have their visibility over the network turned on) and connect with them to share a simple message using sockets.

- [x] Display available peers on the same network
- [x] Connect to a particular peer
- [x] Send a simple message from one device
- [x] Recieve message on another device
- [x] Add chat interface: On clicking a particular device, chat interface (like whatsapp) should appear.
- [x] Improve the overall UI of the application to make it more like a chat application.

##### Screenshots
App interface | Visibility turned on| Discovery started and device found | Devices connected and Message received 
:-------------------------:|:-------------------------:|:----------------------------------:|:-------------------------:
![](/assets/screenshots/host1.png)  | ![](/assets/screenshots/host2.png) | ![](/assets/screenshots/host3.png) | ![](/assets/screenshots/host4.png)

##### Chat Interface
Register Interface | People | Chat interface
:-------------------------:|:-------------------------:|:----------------------------------:
![](/assets/screenshots/register.jpg)  | ![](/assets/screenshots/allchats.jpg) | ![](/assets/screenshots/samplechat.jpg)

#### Current Bugs
- Correct text is not dislayed sometimes over the buttons and conection status.

## Next Step
- Add more information to chat interface (eg: time in messages, etc)
- Create local database for storing information on device
- Integrate the functionality of finding peers and connecting with them to newly added chat interface.
 