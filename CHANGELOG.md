# default Changelog

## 0.1.0

### New Features

* Site role management now only grants or revokes roles for principals belonging to the administered site or the server, ignoring principals from other sites.

* Consistently HTML-escape editor-provided values rendered into HTML attributes in the navigation menu (`j:styleName`/`j:layoutID`), image reference link (`j:linkTitle`) and component link (`divClass`/`aClass`) views, matching the escaping already applied to the adjacent title/label fields.

* Remove the unused `nt:base` raw debug node-inspector view (`nt_base/raw`). It had no legitimate production use and exposed node internals on request.

* Escape the `cssClass` and `target` request parameters in the `nt:base` link view and restrict `target` to the standard frame keywords.

### Bug Fixes

* Use StringUtils from Apache commons lang3 to remove plexus dependency (#140)

* External link component now restricts the rendered href to an allow-list of URL schemes (http, https, ftp, mailto, tel) and escapes the URL, so other schemes and special characters are handled safely.
