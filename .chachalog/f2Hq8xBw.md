---
# Allowed version bumps: patch, minor, major
default: patch
---

External link component now restricts the rendered href to an allow-list of URL schemes (http, https, ftp, mailto, tel) and escapes the URL, so other schemes and special characters are handled safely.
