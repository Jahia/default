# default Changelog

## 0.1.0

### New Features

* Site role management now only grants or revokes roles for principals belonging to the administered site or the server, ignoring principals from other sites.

* Consistently HTML-escape editor-provided values rendered into HTML attributes in the navigation menu (`j:styleName`/`j:layoutID`), image reference link (`j:linkTitle`) and component link (`divClass`/`aClass`) views, matching the escaping already applied to the adjacent title/label fields.

* Remove the unused `nt:base` raw debug node-inspector view (`nt_base/raw`). It had no legitimate production use and exposed node internals on request.

* Escape the `cssClass` and `target` request parameters in the `nt:base` link view and restrict `target` to the standard frame keywords.

### Bug Fixes

* Hardened the navigation menu so page titles and descriptions are shown exactly as entered. Special characters and markup typed into a page title or description now appear as text in the menu.

* Use StringUtils from Apache commons lang3 to remove plexus dependency (#140)

* Chained actions are always checked against their own declared requirements, including when the chain runs with a system session (behaviour change: a chained action is now refused when the caller does not meet them)

* External link component now restricts the rendered href to an allow-list of URL schemes (http, https, ftp, mailto, tel) and escapes the URL, so other schemes and special characters are handled safely.

* Image reference link component now restricts the rendered href to an allow-list of URL schemes (http, https, ftp, mailto, tel) and escapes the URL, so other schemes and special characters are handled safely.

* Web project creation submitted through a page in the live workspace is now refused instead of leaving a partially created site. Site creation is only carried out from the default workspace, matching the publication actions. You are affected if you placed the deprecated `jnt:createWebProject` component on a published page and relied on it to create sites; use Server Settings > Sites, or place the component on a page you use in edit mode.
