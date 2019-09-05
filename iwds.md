# IWDS Protocol

## Random notes
* Apparently amazfit didn't customized much of the original SDK, so theorically, it may work to just use the IWOP SDK as is.
* It seems that Amazfit uses a version `1_0_1` but it doesn't exists in the SDK, there's only `1_0_0`, `1_3_1` and `1_3_2`; do `1_0_1` may be a customized version from amazfit (with minor changes); thus, it's also safe to say that amazfit uses the 'api1' (from v4.3 instead of the newer 5.1) of the iwds sdk 
* The BGService datatransactor UUID is a1dc19e2-17a4-0797-9362-68a0dd4bfb61
* The data transport service datatransactor UUID is 5a177e43-82e6-d483-b7e3-d7072047e3cc
* The IWDS protocol looks a lot like it's actually trying to do "android intents over bluetooth"
