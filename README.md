# Just Dad Jokes

Application to get and display dad jokes from https://icanhazdadjoke.com/api.

## Firebase

This project uses Firebase, but the `google-services.json` file has been left out of git.
The file needs to be placed within the `app/` directory.

To add or update the CI we store the base64 encoded `google-services.json` file in GitHub Actions secrets.

1. base64 encode the file `cat app/google-services.json | base64`
2. Save the above output under "Repository secrets" of the repo actions settings under the name `GOOGLE_SERVICES_JSON`.

## App signing

While the codebase is open source, security secrets like the signing keys are not.
To avoid checking the keystore into git, I will only produce the release bundles manually.
The pipeline produces unsigned release APK's as a test. 
