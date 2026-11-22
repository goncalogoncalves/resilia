---
title: Privacy policy
lang: en
---

# Privacy policy

> Last updated: 2026-11-22. [Versão portuguesa](privacidade.md).

This policy covers the Android emergency-preparedness app published from this repository. The app's name is at the top of the [README](https://github.com/goncalogoncalves/resilia#readme).

## Summary

- No account, no ads, no analytics and no tracking.
- **We run no server.** We receive none of your data.
- What you write stays **on your phone only**.

## Data kept on the phone

| Data | Where | Notes |
|---|---|---|
| Kit inventory and calculator answers | App database | |
| Family plan, people and contacts | App database | |
| Medical sheets | App database | **Encrypted** with a key that exists only on this phone (Android Keystore) |
| Places | App database | Typed by you; the app does not ask for your location |
| Warning area and other preferences | App preferences | |

This data is **excluded from cloud backups and from phone-to-phone transfers**. Uninstalling the app or clearing its data deletes it.

## Internet connections

The app only uses the internet to **download public IPMA files** (weather warnings and fire risk), and only if you choose an area:
- it downloads the **national file**: the request includes neither your area, your municipality nor any identifier;
- as with any internet request, the IPMA server sees your phone's **IP address**. IPMA handles those logs under its own rules: see the [IPMA terms of use](https://api.ipma.pt/).

There are no other connections. We use no Google Play Services, Firebase, advertising or analytics libraries.

## When you open another app

"Call", "Open in maps", "Share", "Print" and source links open another app (dialler, maps, browser, printing). The app passes it only what is needed: the number, the coordinates or address, the PDF or the page address. What happens next follows that app's rules.

## Export and import

If you export your data, the file is saved where you choose. With a password, the file is encrypted (AES-256-GCM). **With medical sheets, a password is required.** If you lose the password, nobody can open the file.

## Permissions

| Permission | Why |
|---|---|
| Notifications | Kit expiry reminders and IPMA warnings, only if you turn them on |
| Internet | Download the IPMA files |
| Biometrics | Optional lock for medical sheets using the phone's lock |

The app **never asks** for location, camera, contacts, microphone or calls. The torch works without the camera permission.

## Children

The app does not ask for or keep data about children beyond what you write in the family plan, which stays on your phone.

## Changes

Changes to this policy are recorded in this repository's history.

## Contact

goncalofbg@live.com.pt
