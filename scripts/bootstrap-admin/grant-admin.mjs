import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";

const projectId = "civic-pulse-831d6";
const uid = process.argv[2]?.trim();

if (!uid) {
  console.error("Usage: npm run grant-admin -- <firebase-auth-uid>");
  process.exitCode = 1;
} else {
  try {
    const app = initializeApp({
      credential: applicationDefault(),
      projectId
    });
    const auth = getAuth(app);
    const user = await auth.getUser(uid);
    await auth.setCustomUserClaims(uid, {
      ...user.customClaims,
      admin: true
    });
    console.log("Admin claim granted. Sign out and sign in again to refresh the ID token.");
  } catch (error) {
    console.error("Could not grant admin claim:", error.message);
    process.exitCode = 1;
  }
}
