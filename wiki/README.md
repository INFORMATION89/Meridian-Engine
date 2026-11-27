# Meridian Wiki source

This directory is the version-controlled source for the native [GitHub Wiki](https://github.com/INFORMATION89/Meridian-Engine/wiki).

## Publishing model

- Edit these Markdown files through normal branches and pull requests in the main repository.
- After changes are merged to `main`, the workflow at `.github/workflows/publish-wiki.yml` copies them to the native Wiki Git repository.
- `Home.md` is intentionally not managed here. The existing Wiki Home page is preserved and must be edited directly in GitHub if its content needs changing.
- The publisher does not delete Wiki pages that are absent from this directory. This prevents accidental removal of pages created directly in the Wiki.

## One-time setup

1. Create a GitHub Personal Access Token (classic) with the `public_repo` scope. The main Meridian repository is public.
2. In **Meridian-Engine → Settings → Secrets and variables → Actions**, create a repository secret named `WIKI_TOKEN`.
3. Paste the token as the secret value. Never commit it, place it in a file, or share it in chat.
4. Merge the publishing workflow to `main`. The next push changing `wiki/**` or the workflow will publish the pages.
5. If the Wiki repository has not been initialized, open the Wiki once and create its Home page before the first publish.

## Maintenance

Treat `PLAN.md`, `RULES.md`, and `CLEAN_ROOM.md` as authoritative. Wiki pages summarize and link to them; they must not create competing requirements. Weekly updates are concise summaries of meaningful work, not daily logs. Clearly distinguish completed work from proposals, open PRs, and pending owner decisions.
