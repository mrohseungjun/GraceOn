# 🔑 Gemini API Key 설정 가이드

## ✨ 빠른 시작 (권장 방법)

### 1단계: Gemini API Key 발급

1. [Google AI Studio](https://aistudio.google.com/app/apikey) 접속
2. Google 계정으로 로그인
3. **"Get API Key"** 또는 **"Create API Key"** 클릭
4. 새 프로젝트 생성 또는 기존 프로젝트 선택
5. 생성된 API Key 복사 (예: `AIzaSy...`)

### 2단계: API Key 설정

#### 방법 A: local.properties 사용 (🔒 안전, 권장)

1. 프로젝트 루트 폴더에서 `local.properties` 파일 열기
   - 파일이 없다면 새로 생성
   
2. 다음 줄 추가:
```properties
GEMINI_API_KEY=AIzaSy여기에_발급받은_API_KEY_붙여넣기
```

3. **현재 바로 사용 가능!** `app/src/main/kotlin/com/graceon/di/AppModule.kt`에서:
```kotlin
single { 
    GeminiApiClient(
        apiKey = "여기에_발급받은_API_KEY_직접_입력"  // 임시로 여기에 입력
    ) 
}
```

⚠️ **중요**: 위 방법은 테스트용입니다. 실제 배포 시에는 아래 BuildConfig 방법을 사용하세요!

#### 방법 B: BuildConfig 사용 (🚀 프로덕션용)

1. `local.properties`에 API Key 추가 (위와 동일)

2. `app/build.gradle.kts` 파일 수정:
```kotlin
android {
    defaultConfig {
        // 기존 설정...
        
        buildConfigField(
            "String", 
            "GEMINI_API_KEY", 
            "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\""
        )
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

3. `app/src/main/kotlin/com/graceon/di/AppModule.kt` 수정:
```kotlin
single { 
    GeminiApiClient(
        apiKey = BuildConfig.GEMINI_API_KEY
    ) 
}
```

4. Gradle Sync 실행

## 🎯 현재 상태 확인

### API Key가 제대로 설정되었는지 확인하는 방법:

1. 앱 실행
2. 고민 선택 (카테고리 또는 AI 모드)
3. "고민 넣고 뽑기" 버튼 클릭
4. "AI가 말씀을 찾는 중..." 메시지 확인
5. 처방전이 나타나면 성공! ✅

### 오류가 발생하면:

**Logcat에서 확인할 내용:**
```
- "API key not valid" → API Key가 잘못됨
- "Network error" → 인터넷 연결 확인
- "Quota exceeded" → API 사용량 초과
```

## 🔐 보안 주의사항

### ✅ 해야 할 것:
- `local.properties`에 API Key 저장 (Git에 자동으로 무시됨)
- BuildConfig 사용하여 안전하게 관리
- API Key를 환경변수로 관리

### ❌ 하지 말아야 할 것:
- 소스 코드에 직접 하드코딩 (특히 Git에 커밋)
- 공개 저장소에 API Key 노출
- API Key를 다른 사람과 공유

## 📊 Gemini API 사용량 확인

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. API & Services → Dashboard
3. Gemini API 사용량 확인

**무료 할당량:**
- 분당 15 요청
- 일일 1,500 요청
- 월 100만 토큰

## 🐛 문제 해결

### 1. "Unresolved reference: BuildConfig"
```bash
# Gradle Sync 실행
./gradlew clean build
```

### 2. API Key가 비어있음
```kotlin
// AppModule.kt에서 확인
single { 
    GeminiApiClient(
        apiKey = BuildConfig.GEMINI_API_KEY.also { 
            println("API Key: ${it.take(10)}...") // 처음 10자만 로그 출력
        }
    ) 
}
```

### 3. 네트워크 오류
- AndroidManifest.xml에 인터넷 권한 확인:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 💡 팁

1. **개발 중**: `AppModule.kt`에 직접 입력하여 빠르게 테스트
2. **배포 전**: BuildConfig로 전환하여 안전하게 관리
3. **팀 작업**: 각자 `local.properties`에 자신의 API Key 설정

## 📞 도움이 필요하면

- [Gemini API 문서](https://ai.google.dev/docs)
- [Google AI Studio](https://aistudio.google.com/)
- 프로젝트 Issues 탭에 질문 남기기
