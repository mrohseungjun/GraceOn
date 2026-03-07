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

5. Deploy the edge function
```bash
supabase functions deploy generate-gemini-content --no-verify-jwt
```

6. Put the deployed function URL into the app

Android `/Users/oseungjun/AndroidStudioProjects/GraceOn/local.properties`
```properties
GRACEON_API_BASE_URL=https://<your-project-ref>.supabase.co/functions/v1/generate-gemini-content
```

iOS `/Users/oseungjun/AndroidStudioProjects/GraceOn/iosApp/Configuration/Config.local.xcconfig`
```xcconfig
GRACEON_API_BASE_URL=https://<your-project-ref>.supabase.co/functions/v1/generate-gemini-content
```

7. Rebuild the app
