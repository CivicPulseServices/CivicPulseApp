# CivicPulseApp

## Sign-in and organization access

Users register once with an email, password, and single-use organization invitation code. The invitation assigns the user's role and organization; sign-in restores the Firebase Authentication session and opens the dashboard for the assigned role. Users linked to multiple organizations can switch accounts from the dashboard.

The app requires Firebase Authentication (Email/Password) and Cloud Firestore. Create invitation documents at `invitations/{code}` with these fields:

| Field | Type | Description |
|---|---|---|
| `role` | string | `provider`, `supervisor`, or `client`, assigned by the organization |
| `accountId` | string | ID of the organization/site being joined |
| `accountName` | string | Display name of the organization/site |
| `email` | string | Optional email restriction for the invite |
| `used` | boolean | Set to `false`; registration atomically marks it used |

Account documents are stored at `accounts/{accountId}` with `id`, `name`, and `memberIds`. The app adds the registering user to the account and saves the assigned role and account IDs on `users/{uid}`. Configure Firestore Security Rules to restrict invitation creation and enforce the same role/account access boundaries as the app; client-side navigation is not a substitute for server-side authorization.

## Build and test

Open the project in Android Studio, or run `gradlew :app:assembleDebug :app:testDebugUnitTest` with an Android SDK and Java runtime configured.