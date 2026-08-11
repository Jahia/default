---
default: patch
---

Chained actions are always checked against their own declared requirements, including when the chain runs with a system session (behaviour change: a chained action is now refused when the caller does not meet them)
