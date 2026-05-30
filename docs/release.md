# Release

The Android release workflow runs when a tag matching `v*` or `V*` is pushed,
such as `v1.0`. It builds a signed release APK, uploads it as a workflow
artifact, and publishes it to GitHub Releases.

## Required GitHub Secrets

Add these repository secrets in GitHub:

- `LABCARDS_KEYSTORE_BASE64`
- `LABCARDS_RELEASE_STORE_PASSWORD`
- `LABCARDS_RELEASE_KEY_ALIAS`
- `LABCARDS_RELEASE_KEY_PASSWORD`

Create `LABCARDS_KEYSTORE_BASE64` from the local keystore:

```powershell
[Convert]::ToBase64String(
    [IO.File]::ReadAllBytes("C:\Users\Allan\AndroidStudioProjects\LabCards\labcards-key.jks")
)
```

Use the output as the secret value. Do not commit the `.jks` file or any
passwords to git.

## Publish v1.0

After the workflow and secrets are on GitHub, create and push the release tag:

```powershell
git tag v1.0
git push origin v1.0
```

You can also run the workflow manually from GitHub Actions with `tag_name` set
to an existing tag, for example `v1.0`.
