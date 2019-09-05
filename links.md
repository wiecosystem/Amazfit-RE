# Trying to map what calls what...

* The com.huami.watch.tranport.Transporter manages the communication with the watch, and is based on the ingenic lib
* The com.huami.watch.companion.bind actually manage the initial setup, and also hooks on BindingState and InitialState (?)
* The InitialState and BindingState is only some "status" classes apparently
* The com.huami.watch.companion.qrcode apparently are only helper classes to the activity that actually decode the qr code (in the activity itself, com.huami.watch.companion.ui.activity.DeviceBindScanQRActivity
* The QR-Code Activity decode the mac address, and this goes to BindingState (that keeps track of the mac address), and then calls the BindHelper singleton and notify that the QR code is scanned, this trigger the rest
