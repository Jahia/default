---
# Allowed version bumps: patch, minor, major
default: minor
---

Consistently HTML-escape editor-provided values rendered into HTML attributes in the navigation menu (`j:styleName`/`j:layoutID`), image reference link (`j:linkTitle`) and component link (`divClass`/`aClass`) views, matching the escaping already applied to the adjacent title/label fields.
