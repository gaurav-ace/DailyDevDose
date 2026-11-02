# Feature wiki

Long-term memory for design decisions and how each feature works, kept next to the code and
maintained with the `feature-wiki` skill.

Design pages and overviews carry a status: `proposed` is still open, `decided` and `implemented`
are binding, and `superseded` is kept for history. `learning.md` files hold lessons from building
the project, one for the whole project and one per feature.

Built for vectorless retrieval: no embeddings or vector store. A reader narrows down through
a tree of summaries, then reads only the section it needs.

1. `INDEX.md`: every feature, one row each. Pick by meaning.
2. `features/<slug>/INDEX.md`: that feature's pages and sections, with line ranges.
3. The section itself. Read only that line range.

The two `INDEX.md` levels are generated, so never edit them by hand.
Pages carry `keywords`, `aliases` and `questions` in their frontmatter, which is what lexical
search matches when the tree alone does not settle it.
