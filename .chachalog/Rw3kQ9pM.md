---
# Allowed version bumps: patch, minor, major
default: minor
---

Remove the unused `nt:base` raw debug node-inspector view (`nt_base/raw`). It had no legitimate production use and exposed node internals on request.
