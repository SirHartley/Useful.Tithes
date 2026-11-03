# Release

How Useful.Tithes is built, packaged and published. Nothing here runs unless a release is asked for: no version bumps, rebuilt jars, tags or published releases for ordinary work. Changelog entry rules are in [CHANGELOG.md](CHANGELOG.md); code owners are in [ARCHITECTURE.md](ARCHITECTURE.md).

## Branches

Work lands on `dev`. `master` is the release branch: it holds the online version file, and the Release workflow runs on it. A release merges `dev` into `master` first.

## Pieces

| Piece | Path | Role |
|---|---|---|
| Changelog | `Changelog` | Source of the version and the release notes. The newest version heading is the release |
| Mod metadata | `mod_info.json` | Version stamped into the ZIP's copy; loads `jars/src.jar` |
| Version file | `usefultithes.version` | Online Version Checker file on `master`, registered in `data/config/version/version_files.csv`; stamped at packaging, committed back after a release |
| Release builder | `jars/src/src/bettertithes/tools/ReleaseBuild.java` | `build` and `package`, run with `java <file>` |
| Builder checks | `jars/src/src/bettertithes/tools/ReleaseBuildChecks.java` | Headless checks of changelog parsing, stamping and file filtering |
| Build libraries | `lib/` | `starfarer.api.jar`, `json.jar`, `log4j-1.2.9.jar`, `lwjgl.jar`, `lwjgl_util.jar` from game `0.98a-RC8`, copied from Industrial.Evolution's `lib/` |
| Release jar | `jars/release/usefultithes.jar` | Built by `build`, gitignored, runtime classes only; shipped as `jars/src.jar` |
| Release workflow | `.github/workflows/release.yml` | Manual only: preview without a version, publish with one |

`jars/src.jar` in the repository is the IntelliJ artifact (`.idea/artifacts/src_jar.xml`). The package never takes it: it ships the release jar under that name. `jars/production/` is IntelliJ compiler output and never ships either.

The release jar is built in CI from `lib/` and never committed, as in Tidy.Panels. Do not add the house template's committed jar and source hash check.

## Versions

Versions use `major.minor.letter`, starting at `1.0.a`: the first number for a major release, the second for changes players will notice, the letter for fixes and small patches. Reset the lower parts when a higher one changes. The newest `Changelog` section is marked `- UNRELEASED` until it ships.

## Build the release jar

From the repository root, with JDK 17:

```text
java jars/src/src/bettertithes/tools/ReleaseBuild.java build
```

- Compiles every `.java` file under `jars/src/src/` against the jars in `lib/`. No game install is needed. `starfarer_obf.jar` is not needed: the code uses only API types.
- Builds into a fresh `out/release-build-*`, keeps only `bettertithes/` runtime classes (the `tools` package never ships), and replaces the release jar only after a clean compile. The jar must contain `bettertithes/plugins/ModPlugin.class`.
- After a game update, copy the new jars into `lib/` and update the version in the table above.

## Package

```text
java jars/src/src/bettertithes/tools/ReleaseBuild.java package          # preview
java jars/src/src/bettertithes/tools/ReleaseBuild.java package 1.0.b    # release
```

- Needs a release jar from `build`.
- Preview packages the newest changelog section.
- Release requires the version (`1.0.b`, no `v`) to equal the newest changelog heading, and that section must not be `UNRELEASED`.
- Ships Git-tracked files only: `mod_info.json`, `usefultithes.version`, `Changelog`, `usefultithes.png` (the LunaLib settings icon, named from the mod root in `data/config/LunaSettingsConfig.json`), license and credit files if added, `data/` (`csv json shader ship wpn proj system variant skin`), `graphics/` (`png jpg jpeg`), `sounds/` (`ogg wav`) and the release jar as `jars/src.jar`. Source, `lib/`, `jars/src.url`, docs, IDE, agent and workflow files, and any path under a dot, `unused`, `unedited`, `tools` or `docs` directory are left out.
- Fails if a `graphics/usefultithes/` or `sounds/usefultithes/` path named in `data/` is not packaged.
- Output in `out/release/`: `Useful.Tithes-<version>.zip` (one `Useful.Tithes/` folder, sorted, fixed timestamps), `release-notes.md`, the stamped version file and `version.txt`.

`1.0.a` was packaged by hand as `Useful.Tithes1.0.a.zip` with a `Useful.Tithes1.0.a/` folder. Releases from the builder use `Useful.Tithes-<version>.zip` and a `Useful.Tithes/` folder; the stamped `directDownloadURL` follows that name.

## Publish

1. Finish the newest `Changelog` section and remove `UNRELEASED`.
2. Run `build` and `package <version>` locally as the preview check, then merge `dev` into `master`.
3. Start the Release workflow on `master` with `version` set: Actions tab, Release, Run workflow, or `gh workflow run release.yml --ref master -f version=<version>`.
4. The workflow runs the builder checks, builds the jar, packages, creates the release and its tag on the packaged commit, then commits the stamped `usefultithes.version` to `master`.
5. Check the release ZIP and notes, and that `master`'s version file names the new version. Merge `master` back into `dev` so both carry the stamped file.

The workflow has no push, pull request, tag or schedule trigger. Without a version it only uploads a preview artifact. It refuses to overwrite an existing release, and run on an existing version tag it publishes that tag. It compiles from `lib/` and downloads nothing.

## Maintenance

Update this guide when `ReleaseBuild`, `ReleaseBuildChecks`, `.github/workflows/release.yml`, the version file or the jars in `lib/` change.
