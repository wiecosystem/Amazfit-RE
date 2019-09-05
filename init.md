# Watch init

## Mac address and pairing

* The mac address you may get from the watch from simple bluetooth scanning doesn't cut it, you need the "hidden" (or "real" mac address from the QR code (see qrcode.md)
* You can bond using com.huami.iwds.uniconnect.Link.bondAddress() using the mac address you've obtained with the QR code, somehow, it's triggering the watch to do a pairing request to the phone
* The watch and phone will ask for the same pincode, this seems more because of how bluetooth works than something really even wanted by the watch
* After pairing, the watch needs some informations (see next chapter)

## Sync of first time informations

* Apparently, there's two "modes", based on if the app/account was previously tied to the watch or not, they are called "OnRestore" and "OnNotRestore"
* "Restore" seems to only sync the watchface configuration and the AGPS data
* The information sent is apparently these, sent to "com.huami.watch.companion.transport.SyndData":
* * UID (string)
* * USER_OVERSEA (boolean)
* * USER_MI_ID (string)
* * USER_INFO (parcelable)
* * EXTRA_INFO (string)
* * USER_SETTINGS (json string)
* * if "find", SPORT_ORDER (json string)
