---
title: Epona — Privacy Policy
layout: default
---

# Privacy Policy

**Last updated: {{ site.terms_version }}**

This Privacy Policy explains how Epona ("**Epona**," "**we**," "**us**") collects, uses, and shares information when you use the Epona mobile app (the "**App**").

Epona is operated by {{ site.legal_name }}, an individual based in {{ site.governing_country }}, who is the data controller for the purposes of this policy.

If you don't agree with this policy, please don't use the App.

## 1. Information we collect

### Account information
When you create an account, we collect your **name**, **email address**, and (if you set one) a **profile photo**. If you sign in with Google, we receive the name, email, and profile photo Google shares with us.

### Pet information
Pets you register include: name, species, breed, color, size, approximate age, sex, an optional microchip ID, a description, and up to three photos.

### Alert information
When you post a lost or found alert, we collect: the pet's last-seen location (coordinates and address), the date and time, a description, whether you're offering a reward, and a **contact phone number**. See [What other users can see](#3-what-other-users-can-see) below — this information is not private.

### Sighting information
When you report seeing someone else's pet, we collect: the location and address where you saw it, up to three photos, an optional note, and the time.

### Location data
The App asks for your device's **precise or approximate location** (foreground use only — we never collect location in the background). We use it to:
- show you alerts near you and calculate distance,
- attach a last-seen or sighting point to a post you create,
- optionally store your last known location so we can notify you about new alerts nearby.

Photos are re-processed on your device before upload, which strips any location (EXIF) data embedded in the image file itself.

### Notifications
We store a push-notification token (from Firebase Cloud Messaging) tied to your account, and a history of the notifications we've sent you (e.g., "someone reported seeing your pet"), so the App can show your notification list.

### Automatically collected information
Our infrastructure provider (Supabase) automatically logs standard technical data for security and reliability, such as IP address, timestamps, and request metadata. We do not use this data for advertising or profiling.

### What we don't collect
We don't use advertising SDKs, and we don't run analytics tools that profile you for advertising purposes. We don't collect background location. We don't request access to your photo gallery as a whole — you pick individual photos through the operating system's photo picker, which never shares the rest of your library with us.

## 2. How we use your information

We use your information to:
- create and secure your account, and let you sign in;
- let you post and manage lost/found alerts and sighting reports;
- show you nearby alerts and calculate distances;
- send you push notifications about activity relevant to you (a sighting on your alert, a new nearby alert, your pet being marked reunited);
- keep a local cache on your device so the App works offline and loads quickly;
- maintain the security, integrity, and proper functioning of the App;
- respond to support requests and enforce our [Terms of Service](terms.html).

## 3. What other users can see

Epona is a community app built around public posts. If you post an alert:
- your **display name**, **profile photo**, the **pet's details and photos**, the **last-seen location/address**, and the **contact phone number you entered** are visible to **every other signed-in user** of the App, and photo files are reachable by anyone with their direct link, whether or not they use the App.
- Alerts can be shared outside the App (e.g., via a link), which makes that information visible even more widely.
- If you report a sighting, your **name** and **profile photo** are visible to the alert's owner and to anyone else viewing that alert's sighting trail.

Please don't include information in a description or note that you don't want to be public, and think carefully before sharing a personal phone number.

## 4. Sharing and service providers

We don't sell your personal information, and we don't share it with third parties for their own advertising purposes.

We share information with the following service providers, who process it on our behalf under their own security and privacy commitments:

| Provider | What they process | Purpose |
|---|---|---|
| **Supabase** | Account data, pet/alert/sighting data, photos, authentication | Database, authentication, file storage, real-time updates ({{ site.server_region }}) |
| **Google (Firebase Cloud Messaging)** | Notification token, notification content | Delivering push notifications |
| **Google (Maps SDK / Play Services location)** | Device location, map interactions | Showing maps and turning coordinates into addresses |

Google's use of information is governed by the [Google Privacy Policy](https://policies.google.com/privacy). Supabase's is governed by the [Supabase Privacy Policy](https://supabase.com/privacy).

We may also disclose information if required by law, to protect the rights and safety of Epona or its users, or in connection with a merger, acquisition, or sale of assets (in which case we'll tell you before your information becomes subject to a different privacy policy).

## 5. International data transfers

Our service providers may process data outside the country you live in, including in {{ site.server_region }} and in the United States. By using the App, you understand your information may be transferred to, stored, and processed in a country with different data protection laws than your own. Where required (e.g., for users in the European Economic Area), such transfers rely on the safeguards our providers publish (such as Standard Contractual Clauses).

## 6. Data retention

We keep your account and content for as long as your account is active. If you delete your account (see [Deleting your account and data](#8-your-rights) below), we delete your personal data, pets, alerts, and sightings — along with their photos — within **30 days**, except:
- copies that may persist briefly in encrypted backups until they age out (typically within 30 days), and
- de-identified records of content reports, which we may keep for up to **12 months** for safety and moderation purposes.

## 7. Security

We use industry-standard measures to protect your information, including encryption in transit (HTTPS/TLS) and access controls that limit who can read your data on our infrastructure. No method of transmission or storage is 100% secure, and we can't guarantee absolute security.

## 8. Your rights

Depending on where you live, you may have rights to access, correct, delete, or export your personal information, or to object to or restrict our use of it. Regardless of location, we offer everyone the following:

- **Access & export:** contact us to receive a copy of your personal data.
- **Correction:** update your name and photo directly in the App (Profile). Other details can be corrected by contacting us.
- **Deletion:** delete your account and associated data at any time from **Profile → Delete account** inside the App, or by visiting our [account deletion page](delete-account.html) if you no longer have the App installed.
- **Withdraw consent:** where we rely on your consent (e.g., location), you can withdraw it via your device's permission settings, though parts of the App may stop working.

**Venezuela.** As a resident of Venezuela, you have habeas data rights under Articles 28 and 60 of the Constitution of the Bolivarian Republic of Venezuela to know, access, and request correction or removal of your personal information held by us.

**European Economic Area / UK (GDPR).** If you're located in the EEA or UK, you have the rights described above under the General Data Protection Regulation, including the right to lodge a complaint with your local data protection authority. Our legal bases for processing are: performance of a contract (running the App), consent (e.g., location, notifications), and legitimate interests (security, abuse prevention).

**California.** California residents have rights under the CCPA/CPRA to know, delete, and correct personal information, and to not be discriminated against for exercising these rights. We do not sell or "share" personal information as those terms are defined by California law.

To exercise any of these rights, contact us at {{ site.contact_email }}.

## 9. Children's privacy

Epona is intended for users **18 years of age and older** (see our [Terms of Service](terms.html)). We do not knowingly collect information from anyone under 18. If you believe a minor has provided us information, contact us at {{ site.contact_email }} and we'll delete it.

## 10. Changes to this policy

We may update this policy from time to time. If we make material changes, we'll update the "Last updated" date above and, where appropriate, notify you in the App. Continued use of the App after a change means you accept the updated policy.

## 11. Contact us

Questions about this policy or your data? Contact {{ site.legal_name }} at {{ site.contact_email }}.
