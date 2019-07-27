# Legacy Wagerr Android Wallet

The Wagerr Android Wallet based on Bitcoinj made by a Wagerr community member is no longer supported and is being deprecated. It will soon be replaced by a new wallet based on Bread SPV. 

This final release is only meant for existing users and contains fixes that allow the Wallet to completely sync past block 703521. Users are then required to withdraw any balance to their QT or the upcoming electron or SPV mobile wallet.

### This project contains several sub-projects:

app: The Android app itself. This is probably what you're searching for.

pivtrumj: Stratum protocol implementation.(Not used)

wagerr-core-0.14.4: wagerrj.

You can build all sub-projects at once using Gradle:

gradle clean build
