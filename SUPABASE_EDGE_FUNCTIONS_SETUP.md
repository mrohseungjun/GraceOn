# Supabase Edge Function setup

1. Install Supabase CLI
```bash
brew install supabase/tap/supabase
```

2. Login
```bash
supabase login
```

3. Link this project
```bash
cd /Users/oseungjun/AndroidStudioProjects/GraceOn
supabase link --project-ref <your-project-ref>
```

4. Store the Gemini key as a secret
```bash
supabase secrets set GEMINI_API_KEY=your_gemini_api_key
```

4-1. Store app update config secrets
```bash
supabase secrets set \
  ANDROID_LATEST_VERSION=1.1.0 \
  ANDROID_MINIMUM_SUPPORTED_VERSION=1.0.0 \
  IOS_LATEST_VERSION=1.1.0 \
  IOS_MINIMUM_SUPPORTED_VERSION=1.0.0
```

5. Enable Anonymous sign-ins in Supabase Dashboard
- `Authentication > Providers > Anonymous`
- Turn it on before testing the app

6. Deploy the edge functions
```bash
supabase functions deploy generate-gemini-content --no-verify-jwt
supabase functions deploy app-version-config --no-verify-jwt
```

7. Put the deployed function URL and anon key into the app

Android `/Users/oseungjun/AndroidStudioProjects/GraceOn/local.properties`
```properties
GRACEON_API_BASE_URL=https://<your-project-ref>.supabase.co/functions/v1/generate-gemini-content
SUPABASE_ANON_KEY=<your-supabase-anon-key>
ANDROID_APP_STORE_URL=https://play.google.com/store/apps/details?id=com.graceon
```

iOS `/Users/oseungjun/AndroidStudioProjects/GraceOn/iosApp/Configuration/Config.local.xcconfig`
```xcconfig
// In xcconfig, '//' starts a comment, so use https:/$()/...
GRACEON_API_BASE_URL=https:/$()/<your-project-ref>.supabase.co/functions/v1/generate-gemini-content
SUPABASE_ANON_KEY=<your-supabase-anon-key>
IOS_APP_STORE_URL=https:/$()/apps.apple.com/kr/app/your-app-name/id1234567890
```

8. Rebuild the app
