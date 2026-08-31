# WTF Hello Mod

The reference mod for the **WTF Stremio Mod Host** — the external mod platform in
a patched Stremio Android TV client.

It exists to be installed, not to be useful. It is the smallest package that
proves the whole path works: published here, resolved from a GitHub release,
downloaded, verified against its own signed hash list, installed into private
storage, loaded, and run when the user presses a button.

## What is in a `.wtfmod`

A ZIP with a frozen layout:

```
mod.json                      identity, version, compatibility, signing claim
payload/plugin.jar            the code (a classes.dex inside a jar)
assets/…                      anything else the mod ships
META-INF/files.sha256         SHA-256 of every other entry, byte-wise sorted
META-INF/public-key.der       the signing key, X.509 SubjectPublicKeyInfo DER
META-INF/signature.p256       ECDSA P-256 over the exact bytes of files.sha256
```

`files.sha256` covers `public-key.der` and every payload and asset, and excludes
only itself and `signature.p256`. The verifier requires the listed paths and the
actual entries to correspond exactly, so nothing can ride along unlisted.

`signature.p256` holds the **DER** signature that `SHA256withECDSA` produces — an
ASN.1 `SEQUENCE { r, s }`, not a raw 64-byte `r||s`. Packager and verifier use
those bytes directly and never convert between encodings.

## Trust, stated plainly

Installing a mod runs someone else's code **inside Stremio's own process**, with
the same access Stremio has. The Mod Host checks that a package is intact and, on
a signed package, that it came from the key you already trusted. **It does not
sandbox it.**

A valid signature on a *first* install proves the package has not been altered
since it was signed. It does not prove who signed it. That first install is
trust-on-first-use: the television shows the key's fingerprint and remembers it,
and a later update signed by a different key is refused until the change is
approved by hand.

## Releases

Each release attaches exactly one `.wtfmod` and a `.sha256` beside it. The
checksum is transport integrity — the package's own signed hash list is what
actually decides whether the contents are trustworthy, and it is checked either
way.

## The repository contract

The television reads this repository before it downloads anything:

1. `GET /repos/{owner}/{repo}` — the **default branch**, read rather than assumed
   (this repository's is `master`).
2. `mod.json` **at the root of that branch** — the manifest it advertises.
3. `GET /repos/{owner}/{repo}/releases/latest` — the current release.
4. the single `.wtfmod` asset, and its `.sha256` if published.

After download, the packaged `mod.json` must agree with this one on **id and
version**. The root manifest is unsigned metadata and never overrides the
packaged one; it only decides whether to go on. So **bump the root `mod.json`
in the same commit you publish the release**, or the install stops with
*"the repository advertises version X but its current release contains Y"*.

## What is here

```
mod.json                              what the repository advertises
src/com/example/hello/HelloMod.java   the mod
pkg/mod.json                          the manifest that goes in the package
pkg/assets/                           what it ships alongside
```

`pkg/payload/plugin.jar` is built, not committed, and so is the signing key.

## Building

The packager is part of the host, not a reimplementation of its format:

```bash
external-mod-sdk/tools/generate-key.sh keys        # once, and never publish keys/
external-mod-sdk/tools/package-mod.sh src out.wtfmod keys
```

It builds the package, then verifies it with the same code the television runs.
