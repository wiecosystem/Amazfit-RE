# Bind qr code

This one's a trap, the URL you scan actually doesn't exist. If you go to the URL, it actualy redirect you to the playstore for the amazfit's app!

No need to go to the url then, the model is obviously the "m" parameter, but what's with this strange mac?

Actually, it's pretty simple, but an obvious take at obfuscation: you have to split to the '-' in the string, and then, the mac address equal each byte of the first part minus each byte of the second part...

```python
import urllib.parse
url = 'https://api-watch.huami.com/forwarding/watchUS?mac=(...)&m=A1612'
qs = urllib.parse.parse_qs(url.split('?')[1])
mac = qs['mac'][0].split('-')
print('Model = {} // MAC = {}'.format(qs['m'][0], ''.join(chr(ord(mac[0][i])-ord(mac[1][i])) for i in range(len(mac[0])))))
```
