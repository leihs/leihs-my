# Security Vulnerability Fixes

## Summary

This document covers 19 security alerts detected in the project's dependencies (Ruby gems and npm packages). Fixes were applied on 2026-02-19.

| Severity | Fixed | No Public Fix | Total |
|----------|-------|---------------|-------|
| High     | 8     | 0             | 8     |
| Moderate | 6     | 1             | 7     |
| Low      | 3     | 1             | 4     |
| **Total**| **17**| **2**         | **19**|

---

## Ruby Gems (Gemfile.lock)

### #172 — Rack: Directory Traversal via Rack::Directory (High)

- **CVE:** CVE-2026-25500
- **Package:** rack
- **Fixed:** 3.1.19 → **3.1.20**
- **Details:** `Rack::Directory` used a string prefix match on the expanded path, allowing requests like `/../root_example/` to escape the configured root and list directories outside the intended scope.

### #174 — Rack: Stored XSS in Rack::Directory via `javascript:` Filenames (Moderate)

- **CVE:** CVE-2026-25500
- **Package:** rack
- **Fixed:** 3.1.19 → **3.1.20**
- **Details:** `Rack::Directory` renders file entries as clickable links. Files whose basename starts with the `javascript:` scheme caused the generated anchor `href` to execute arbitrary JavaScript when clicked.

### #170 — Faraday: SSRF via Protocol-Relative URL Host Override (Moderate)

- **CVE:** CVE-2026-25765
- **Package:** faraday
- **Fixed:** 1.10.3 → **1.10.5**
- **Details:** `build_exclusive_url` uses `URI#merge`, which per RFC 3986 allows protocol-relative URLs (`//evil.com/path`) to override the base URL host. User-controlled input passed to Faraday request methods could redirect requests to arbitrary hosts.

### #149 — Sinatra: ReDoS Through ETag Header Value Generation (Low)

- **CVE:** CVE-2025-61921
- **Package:** sinatra
- **Fixed:** 4.1.1 → **4.2.1**
- **Details:** The `If-Match` / `If-None-Match` header parsing used for ETag generation was vulnerable to Regular Expression Denial of Service on Ruby < 3.2. Crafted input could cause excessive CPU consumption.

---

## Root npm (package-lock.json)

### #85 — phin: Sensitive Headers Leaked After Redirect (Moderate)

- **CVE:** GHSA-x565-32qp-m3vf
- **Package:** phin (transitive via jimp)
- **Fixed:** 2.9.3 → **3.7.1** (via npm override)
- **Details:** When `followRedirects` was enabled, sensitive headers were included in subsequent requests after a redirect, potentially exposing authentication tokens to unintended recipients.

### #161 — Elliptic: Risky Cryptographic Implementation (Low) — NO PUBLIC FIX

- **CVE:** CVE-2025-14505
- **Package:** elliptic (transitive via shadow-cljs → crypto-browserify)
- **Status:** **No fix available on npm.** Latest public version is 6.6.1 which remains vulnerable.
- **Details:** Incorrect computation of the byte-length of the ECDSA `k` value (RFC 6979) when it has leading zeros. Under specific conditions, an attacker with both a faulty signature and a correct one could derive the secret key.
- **Mitigation:** Low risk in a development build tool context (shadow-cljs). Monitor for a patched release. HeroDevs offers a commercial NES fix at v6.6.3.

---

## UI npm (ui/package-lock.json)

### #163 — node-tar: Race Condition via Unicode Ligature Collisions on macOS APFS (High)

- **CVE:** CVE-2026-23950
- **Package:** tar (transitive via storybook)
- **Fixed:** 6.2.1 → **7.5.9** (via npm override)
- **Details:** The `path-reservations` system used NFD Unicode normalization, but macOS APFS treats certain Unicode characters (e.g. `ß` and `ss`) as identical at the inode level. This desync allowed parallel processing of what should be serialized operations, enabling symlink poisoning attacks.

### #162 — node-tar: Arbitrary File Overwrite via Insufficient Path Sanitization (High)

- **CVE:** CVE-2026-23745
- **Package:** tar (transitive via storybook)
- **Fixed:** 6.2.1 → **7.5.9** (via npm override)
- **Details:** Insufficient path sanitization during tar extraction allowed crafted archives to overwrite arbitrary files via symlink poisoning and directory cache manipulation.

### #165 — node-tar: Arbitrary File Creation/Overwrite via Hardlink Path Traversal (High)

- **CVE:** CVE-2026-24842
- **Package:** tar (transitive via storybook)
- **Fixed:** 6.2.1 → **7.5.9** (via npm override)
- **Details:** Security checks for hardlink targets used different path resolution semantics than the actual hardlink creation logic, allowing arbitrary file read/write outside the extraction directory.

### #175 — node-tar: Arbitrary File Read/Write via Hardlink Target Escape Through Symlink Chain (High)

- **CVE:** CVE-2026-26960
- **Package:** tar (transitive via storybook)
- **Fixed:** 6.2.1 → **7.5.9** (via npm override)
- **Details:** Inadequate symlink resolution checks during extraction allowed hardlink targets to escape the extraction directory through symlink chains.

### #160 — qs: arrayLimit Bypass via Bracket Notation Allows DoS (High)

- **CVE:** CVE-2025-15284
- **Package:** qs (transitive via storybook)
- **Fixed:** 6.13.0 → **6.15.0** (via npm override)
- **Details:** The `arrayLimit` option did not enforce limits for bracket notation (`a[]=1&a[]=2`), only for indexed notation, allowing memory exhaustion attacks.

### #171 — qs: arrayLimit Bypass via Comma Parsing Allows DoS (Low)

- **CVE:** CVE-2026-2391
- **Package:** qs (transitive via storybook)
- **Fixed:** 6.13.0 → **6.15.0** (via npm override)
- **Details:** When `comma: true` is enabled, `arrayLimit` was not enforced for comma-separated values, allowing arbitrarily large arrays from a single parameter.

### #159 — Storybook: Environment Variables Exposed During Build (High)

- **CVE:** CVE-2025-68429
- **Package:** storybook
- **Fixed:** 7.6.20 → **7.6.23** (direct dependency bump)
- **Details:** The `storybook build` command unexpectedly bundled sensitive `.env` file variables into build artifacts. Only affects projects that build and publish Storybook with a `.env` file containing secrets.

### #136 — webpack-dev-server: Source Code Theft via Non-Chromium Browsers (Moderate)

- **CVE:** CVE-2025-30360
- **Package:** webpack-dev-server
- **Fixed:** 4.15.2 → **5.2.3** (direct dependency bump)
- **Details:** Overly permissive `Origin` header validation for WebSocket connections allowed IP-address-origin websites to connect and steal source code. Does not affect Chromium 94+.

### #135 — webpack-dev-server: Source Code Theft via Script Injection (Moderate)

- **CVE:** CVE-2025-30359
- **Package:** webpack-dev-server
- **Fixed:** 4.15.2 → **5.2.3** (direct dependency bump)
- **Details:** Attackers could inject script tags pointing to a local webpack-dev-server instance and extract source code via webpack runtime variables and `Function::toString`.

### #127 — esbuild: Development Server Allows Arbitrary Cross-Origin Requests (Moderate)

- **CVE:** GHSA-67mh-4wv8-2f99
- **Package:** esbuild (transitive via storybook)
- **Fixed:** 0.18.20 → **0.25.12** (via npm override)
- **Details:** The dev server set `Access-Control-Allow-Origin: *` on all requests, allowing any website to read responses including source code, source maps, and file listings.

### #164 — Lodash: Prototype Pollution in `_.unset` and `_.omit` (Moderate)

- **CVE:** CVE-2025-13465
- **Package:** lodash (transitive via storybook)
- **Fixed:** 4.17.21 → **4.17.23** (via npm override)
- **Details:** Crafted paths passed to `_.unset` or `_.omit` could delete properties from global prototypes, potentially compromising application stability.

### #168 — webpack: buildHttp allowedUris Bypass via HTTP Redirects (Low)

- **CVE:** CVE-2025-68458 (related)
- **Package:** webpack
- **Fixed:** 5.102.0 → **5.105.2** (direct dependency bump)
- **Details:** When `experiments.buildHttp` is enabled, the `HttpUriPlugin` could be bypassed via HTTP redirects, allowing SSRF and cache persistence of malicious content.

### #167 — webpack: buildHttp allowedUris Bypass via URL Userinfo (@) (Low)

- **CVE:** CVE-2025-68458
- **Package:** webpack
- **Fixed:** 5.102.0 → **5.105.2** (direct dependency bump)
- **Details:** Raw string prefix checks in `allowedUris` validation could be bypassed using URL userinfo syntax (`user@evil.com`), leading to build-time SSRF.

---

## Changes Made

### Gemfile

```
gem "faraday", ">= 1.10.5"   # was unpinned
gem "rack", ">= 3.1.20"      # added explicit minimum
gem "sinatra", ">= 4.2.0"    # was unpinned
```

### Gemfile.lock

Updated via `bundle update rack faraday sinatra`:

| Gem | Before | After |
|-----|--------|-------|
| rack | 3.1.19 | 3.1.20 |
| faraday | 1.10.3 | 1.10.5 |
| sinatra | 4.1.1 | 4.2.1 |
| rack-protection | 4.1.1 | 4.2.1 |
| mustermann | 3.0.0 | 3.0.4 |

### package.json (root)

Added overrides section:
```json
"overrides": {
  "phin": "^3.7.1"
}
```

### ui/package.json

Updated direct devDependencies:
```json
"@storybook/*": "^7.0.27" → "^7.6.21"
"storybook": "^7.0.27" → "^7.6.21"
"webpack": "^5.88.1" → "^5.104.1"
"webpack-dev-server": "^4.15.1" → "^5.2.1"
```

Added overrides for transitive dependencies:
```json
"overrides": {
  "tar": "^7.5.9",
  "qs": "^6.14.2",
  "lodash": "^4.17.23",
  "esbuild": "^0.25.0"
}
```

---

## Remaining Notes

- **elliptic (CVE-2025-14505):** No public fix exists on npm (latest is 6.6.1). This is a development-only transitive dependency via `shadow-cljs → crypto-browserify`. Risk is low in this context. Monitor for upstream fix.
- **webpack-dev-server 4→5:** This is a major version bump. Test the dev server configuration to ensure compatibility. Key changes include requiring Node.js 18.12.0+ and some config option changes.
- **Remaining npm audit findings** in `ui/` are deep transitive issues in the storybook/eslint ecosystem (minimatch, ajv, @babel/runtime) that require upstream fixes in those meta-packages. These are all development-only dependencies.
