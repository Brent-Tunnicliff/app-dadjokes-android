# template-app-android

Template as a base for all my Android apps.

After generating the repository find all cases of `REPLACE_ME`/`replace_me` and rename them.

## App signing

While the codebase is open source, security secrets like the signing keys are not.
To avoid checking the keystore into git, I will only produce the release bundles manually.
The pipeline produces unsigned release APK's as a test. 
