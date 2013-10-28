# Griddler Mobile Game Java Backend Sample

This project is a Java implementation of Griddler mobile game backend sample on Google App Engine.

## Copyright
Copyright 2013 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Disclaimer
This sample application is not an official Google product.

## Supported Platform and Versions
This sample source code and project is designed to work with Eclipse. It was tested with Eclipse 3.8.

## Products
- [App Engine][1]

## Language
- [Java][3]

## APIs
- [Google Cloud Endpoints][2]

## Setup Instructions
1. Navigate to the [Google Cloud Console](https://cloud.google.com/console) and create a project. Note the "Project ID" you used - it will be needed in the steps below.

2. Navigate to the [Google APIs Console](https://code.google.com/apis/console/) and make sure that the project you just created is selected in the dropdown in the top left corner.

3. In the Google APIs Console, navigate to Services and toggle "Google Cloud Messaging for Android" to "ON".

4. In the Google APIs Console, navigate to Services and toggle "Google+ API" to "ON".

5. In the Google APIs Console, navigate to "API Access" and click "Create an Oauth 2.0 client ID" or "Create another client ID...". Choose "Web application" and click "Create client ID" using default values. Note the created "Client ID" - it will be needed in the steps below.

6. In the Google APIs Console, navigate to "API Access" and click "Create another client ID...". Choose "Installed application", then choose "Android". As the package name enter com.google.cloud.solutions.griddler.android . Enter your Signing certificate fingerprint (see [Documentation](https://developers.google.com/console/help/#installed_applications) for information on the certificate) and click "Create client ID". Note the created "Client ID" - it will be needed in the steps below.

7. In the Google APIs Console, navigate to "API Access" and click "Create another client ID...". Choose "Installed application", then choose "iOS". Enter your bundle ID (e.g., com.YourCompany.Griddler) and App Store ID and click "Create client ID". Note the created "Client ID" - it will be needed in the steps below.

8. In the Google APIs Console, navigate to "API Access" and click "Create new Server key...". Click "Create". Note the created "API key" - it will be needed in the steps below.

9. This project requires a few jar files that are not included in the GitHub repository. Follow the steps below to obtain these jars:
   * gcm-server.jar. Open Android SDK Manager and choose Extras > Google Cloud Messaging for Android Library. This creates a gcm directory under YOUR_ANDROID_SDK_ROOT/extras/google/ containing "gcm-server/dist" subdirectory which has gcm-server.jar file.
   * Download [google-gson-2.1-release.zip](https://google-gson.googlecode.com/files/google-gson-2.1-release.zip) and extract the zip file, and you have "google-gson-2.1" directory which has gson-2.1.jar file.
   * Download [json-simple.1.1.1.jar](https://json-simple.googlecode.com/files/json-simple-1.1.1.jar) file.
   * Download [log4j-1.2.17.jar](http://logging.apache.org/log4j/1.2/download.html) file.
   * Download [JavaPNS-2.2.jar](https://code.google.com/p/javapns/downloads/list) file.
   * Download [bcprov-jdk15on-146.jar](http://www.bouncycastle.org/download/bcprov-jdk15on-146.jar) file. The downloaded jar file is signed.  Execute the following command to unsign this jar file:

         zip -d bcprov-jdk15on-146.jar META-INF/MANIFEST.MF

10. Copy all the jars obtained in step 1 above to war/WEB-INF/lib subdirectory.

11. Import the project into Eclipse (File->Import->General->Existing Projects into Workspace and select the directory where you have unzipped the downloaded project). If you see any build errors they will be resolved after you follow the remaining steps.

12. Configure "Griddler - App Engine" project, by selecting Properties from the context menu in the Package Explorer. Select Google -> App Engine and make sure "Use App Engine" is selected and the selected App Engine SDK version is 1.8.1 or newer. As Application ID enter the Project ID that you created in step 1.

13. Obtain APNS p12 certificate for the bundle ID that you entered in step 7. Copy the p12 file to the src package in Eclipse. You can skip this step if you don't want to use iOS client.

14. Edit Configuration.java in com.google.cloud.solutions.sampleapps.griddler.backend.api package and update the following constants:

        String WEB_CLIENT_ID = "Your Web Client ID"; // Use the Client ID created in step 5 above.
        String ANDROID_CLIENT_ID = "Your Android Client ID"; // Use the Client ID created in step 6 above.
        String IOS_CLIENT_ID = "Your iOS Client ID"; // Use the Client ID created in step 7 above.
        String CLOUD_MESSAGING_API_KEY = "Your Simple API Access key for server apps"; // Use the API key created in step 8 above.
        String APNS_CERTIFICATE_FILE = "YouriOSPushNoficicationCertificate.p12"; // The certificate file name described in step 13 above.
        String APNS_CERTIFICATE_PASSWORD = "YouriOSPushNoficicationCertificatePassword"; // The password for the certificate described in step 13 above.

15. In the Package Explorer, select "Griddler - App Engine" project and from the context menu select Google -> Generate Cloud Endpoint Client Library.

16. In the Package Explorer, select "Griddler - App Engine" project and from the context menu select Google -> Deploy to App Engine. This will upload Griddler mobile game backend to the cloud.

17. Upload sample game board definitions to your mobile game backend. Make sure that you have [Python App Engine SDK](https://developers.google.com/appengine/downloads) installed. Navigate to "data" subdirectory and execute the following command (Make sure to replace YOUR_PROJECT_ID with your actual project id created in step 1):

        appcfg.py upload_data --config_file=config.yml --filename=boards.csv --url=http://YOUR_PROJECT_ID.appspot.com/remote_api --kind=Board

18. Now you are ready to configure and run [Griddler Android client](https://github.com/GoogleCloudPlatform/solutions-griddler-sample-android-client) and/or [Griddler iOS client](https://github.com/GoogleCloudPlatform/solutions-griddler-sample-iOS-client)

[1]: https://developers.google.com/appengine
[2]: https://developers.google.com/appengine/docs/java/endpoints/
[3]: http://java.com/en/


