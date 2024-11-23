# Just Dad Jokes

Application to get and display dad jokes from https://icanhazdadjoke.com/api.

## Firebase

This project uses Firebase, but the `google-services.json` file has been left out of git.
The file needs to be placed within the `app/` directory.

## App signing

While the codebase is open source, security secrets like the signing keys are not.
To avoid checking the keystore into git, I will only produce the release bundles manually.
The pipeline produces unsigned release APK's as a test. 
