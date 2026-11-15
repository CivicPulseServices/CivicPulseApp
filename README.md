# CivicPulseApp

## Sign-in and organization access

Users register once with an email, password, and single-use organization invitation code. The invitation assigns the user's role and organization; sign-in restores the Firebase Authentication session and opens the dashboard for the assigned role. Users linked to multiple organizations can switch accounts from the dashboard.

The app targets Firebase project `civic-pulse-831d6` and requires Firebase Authentication (Email/Password) and Cloud Firestore. Download that project's Android `google-services.json` and place it at `app/google-services.json` (this local config is git-ignored). Enable Email/Password sign-in in Firebase Authentication.

### Initial platform admin

Admin access is read from the Firebase Authentication ID token's `admin` custom claim, not from the Firestore user profile. After creating the initial user in Firebase Authentication, grant the claim from a trusted local environment with Google Cloud CLI installed and authorized for this project:

```powershell
gcloud auth application-default login
cd scripts/bootstrap-admin
npm install
npm run grant-admin -- FIREBASE_AUTH_UID
```

Replace `FIREBASE_AUTH_UID` with that user's UID from Firebase Authentication. The script targets `civic-pulse-831d6`, preserves existing custom claims, and never needs a service-account key in the repository or Android app. Sign out and sign back in after the claim is granted.

The Android app force-refreshes the ID token at sign-in/session restore, recognizes `admin: true`, and opens the platform-admin dashboard. Custom claims are not settable from the Android client.

**Important:** this repository does not yet contain deployed Firestore Security Rules. A client-side admin dashboard check does not secure Firestore data. Before production, add and deploy rules that authorize admin operations with `request.auth.token.admin == true` and enforce account-level access for other roles. Keep admin claim assignment limited to trusted operators.

### Organization invitations

Create invitation documents at `invitations/{code}` with these fields:

| Field | Type | Description |
|---|---|---|
| `role` | string | `provider`, `supervisor`, or `client`, assigned by the organization |
| `accountId` | string | ID of the organization/site being joined |
| `accountName` | string | Display name of the organization/site |
| `email` | string | Optional email restriction for the invite |
| `used` | boolean | Set to `false`; registration atomically marks it used |

Account documents are stored at `accounts/{accountId}` with `id`, `name`, and `memberIds`. The app adds the registering user to the account and saves the assigned role and account IDs on `users/{uid}`. Configure Firestore Security Rules to restrict invitation creation and enforce the same role/account access boundaries as the app; client-side navigation is not a substitute for server-side authorization.

## Build and test

Open the project in Android Studio, or run `gradlew :app:assembleDebug :app:testDebugUnitTest` with an Android SDK and Java runtime configured. The Google Services Gradle plugin is applied when `app/google-services.json` exists.