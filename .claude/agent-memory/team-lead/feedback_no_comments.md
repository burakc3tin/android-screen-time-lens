---
name: no-code-comments
description: User forbids comments in code in this project; also prefers direct minimal explanations
metadata:
  type: feedback
---

Write no comments anywhere in code for this project (including KDoc/`///` doc blocks). Existing comments in files being touched should be stripped.

**Why:** User stated it explicitly ("yorum satırı yazma kodlarda hiçbir yere"); code should be self-documenting.

**How to apply:** Applies to every file written or edited in [[screentimelens-stack]], overriding the global rule that allows doc comments for public APIs.
