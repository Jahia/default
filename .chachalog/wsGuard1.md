---
# Allowed version bumps: patch, minor, major
default: patch
---

Web project creation submitted through a page in the live workspace is now refused instead of leaving a partially created site. Site creation is only carried out from the default workspace, matching the publication actions. You are affected if you placed the deprecated `jnt:createWebProject` component on a published page and relied on it to create sites; use Server Settings > Sites, or place the component on a page you use in edit mode.
