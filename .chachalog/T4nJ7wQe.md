---
# Allowed version bumps: patch, minor, major
default: patch
---

An image component with no image picked no longer writes the word "empty" into the page in edit mode. It left a translated word — "vide", "leer" — where jContent expects an empty component, which kept the image out of the "Nothing to display" box every other untranslated component gets. It now leaves an empty placeholder that keeps the component selectable in Page Builder.
