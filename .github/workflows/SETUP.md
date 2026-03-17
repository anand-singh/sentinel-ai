# GitHub Actions Setup for Cloud Run Deployment

## Required GitHub Secrets

To deploy Sentinel AI to Google Cloud Run using GitHub Actions, you need to configure the following secrets in your GitHub repository.

### 1. GCP_SA_KEY (Service Account Key - JSON format)

This is the service account credentials that GitHub Actions will use to authenticate with Google Cloud.

#### Step 1: Create a Service Account in GCP

```bash
# Set your project ID
export PROJECT_ID="qwiklabs-asl-03-085d8ab3ed6e"

# Create service account
gcloud iam service-accounts create github-actions \
  --display-name "GitHub Actions Deployer" \
  --project $PROJECT_ID
```

#### Step 2: Grant Required Roles

```bash
# Get service account email
export SA_EMAIL="github-actions@${PROJECT_ID}.iam.gserviceaccount.com"

# Grant necessary roles
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/artifactregistry.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/iam.serviceAccountUser"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/storage.admin"
```

#### Step 3: Create and Download JSON Key

```bash
# Create JSON key
gcloud iam service-accounts keys create github-actions-key.json \
  --iam-account="${SA_EMAIL}" \
  --project=$PROJECT_ID

# Display the key (you'll copy this to GitHub)
cat github-actions-key.json
```

#### Step 4: Add Secret to GitHub

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `GCP_SA_KEY`
5. Value: Paste the entire contents of `github-actions-key.json`
6. Click **Add secret**

**IMPORTANT**: After adding the secret to GitHub, delete the local key file for security:
```bash
rm github-actions-key.json
```

### 2. GCP_PROJECT_ID

This is your Google Cloud Project ID.

1. Go to GitHub repository **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Name: `GCP_PROJECT_ID`
4. Value: `qwiklabs-asl-03-085d8ab3ed6e` (or your actual project ID)
5. Click **Add secret**

## Verify Secrets

After adding the secrets, verify they are set:

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. You should see:
   - `GCP_SA_KEY` (with a value hidden)
   - `GCP_PROJECT_ID` (with a value hidden)

## Test the Workflow

1. Make a small change to your code
2. Commit and push to the `main` branch
3. Go to **Actions** tab in GitHub
4. Watch the "Deploy to Cloud Run" workflow execute

Alternatively, trigger manually:
1. Go to **Actions** tab
2. Select "Deploy to Cloud Run" workflow
3. Click **Run workflow** → **Run workflow**

## Troubleshooting

### Error: "credentials_json is empty"

**Solution**: Ensure you copied the entire JSON key file contents, including the opening `{` and closing `}` braces.

### Error: "Permission denied"

**Solution**: Verify the service account has all required roles (see Step 2 above).

### Error: "Secret not found"

**Solution**: Secrets are not available for workflows triggered from forks. Ensure you're pushing to the main repository, not a fork.

### Error: "workload_identity_provider or credentials_json"

**Solution**: The workflow is now properly configured to use `credentials_json`. This error typically means the `GCP_SA_KEY` secret is not set or is empty.

## Security Best Practices

1. **Never commit** the service account JSON key to your repository
2. **Rotate keys** regularly (every 90 days recommended)
3. **Use least privilege**: Only grant roles necessary for deployment
4. **Monitor usage**: Review Cloud Audit Logs for service account activity
5. **Enable MFA**: For the GCP account that creates service accounts

## Alternative: Workload Identity Federation (Recommended for Production)

For enhanced security, consider using Workload Identity Federation instead of service account keys:

```yaml
- name: Authenticate to Google Cloud
  uses: google-github-actions/auth@v2
  with:
    workload_identity_provider: 'projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/github/providers/github'
    service_account: 'github-actions@PROJECT_ID.iam.gserviceaccount.com'
```

This eliminates the need for long-lived credentials. See [Google's documentation](https://cloud.google.com/iam/docs/workload-identity-federation) for setup.

---

**Last Updated**: March 17, 2026
