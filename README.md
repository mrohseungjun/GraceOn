# GraceOn

AI 기반 성경 말씀 추천 앱입니다. 사용자의 고민을 입력하거나 카테고리를 고르면, Supabase Edge Function을 통해 생성한 말씀 카드와 위로 메시지, 기도문을 보여줍니다. 프로젝트는 Kotlin Multiplatform + Compose Multiplatform을 중심으로 구성되어 있고, iOS에서는 Swift 셸이 Compose 화면을 감싸며 광고와 시스템 연동 일부를 담당합니다.

## 핵심 기능

- 이메일/Google 로그인
- 고민 카테고리 선택 또는 자유 입력
- 말씀 생성 가챠 애니메이션
- 결과 카드, 기도문 생성, 공유, 저장
- 저장한 말씀 목록 조회 및 삭제
- 리워드 광고 시청 후 오늘 사용 횟수 추가
- 홈/결과 인라인 배너 광고
- 매일 오전 9시 오늘의 말씀 알림
- 앱 실행 시 선택 업데이트/강제 업데이트 체크

## 기술 스택

- Kotlin Multiplatform
- Compose Multiplatform + Material 3
- Coroutines + StateFlow
- Ktor Client + kotlinx.serialization
- Supabase Auth + Supabase Edge Functions
- Google Mobile Ads
- Gradle Version Catalog + convention plugins

## 현재 구조

```text
GraceOn/
├── composeApp/                  # 공통 앱 엔트리, 내비게이션, 플랫폼 브리지
│   └── src/
│       ├── commonMain/          # App, GraceOnRoot, NavGraph, update 모델, 공통 광고 슬롯
│       ├── androidMain/         # Android 의존성 조합, 알림 스케줄러, AdMob Android
│       └── iosMain/             # iOS 의존성 조합, MainViewController 연결
├── feature/
│   ├── feature-onboarding/      # 로그인/회원가입/비밀번호 재설정
│   ├── feature-worry/           # 홈, 고민 입력, 일일 사용량
│   ├── feature-gacha/           # 말씀 생성 애니메이션
│   ├── feature-result/          # 결과 카드, 기도문, 공유
│   ├── feature-saved/           # 저장한 말씀 목록
│   └── feature-profile/         # 마이 화면, 알림/테마/광고 보상
├── domain/                      # 모델, repository 계약, use case
├── data/                        # repository 구현, 로컬 저장소, preferences
├── core/
│   ├── core-common/             # Result, 에러, 공통 유틸
│   ├── core-network/            # Supabase Auth, Ktor 클라이언트, 프록시 호출
│   └── core-ui/                 # 테마, 공통 Scaffold, 바텀바, 카드 컴포넌트
├── iosApp/                      # SwiftUI 호스트 앱, iOS 광고/공유/알림 브리지
├── supabase/
│   ├── functions/
│   │   ├── generate-gemini-content/
│   │   └── app-version-config/
│   └── config.toml
├── build-logic/                 # Gradle convention plugins
└── gradle/libs.versions.toml    # 버전 카탈로그
```

## 아키텍처 요약

이 프로젝트는 전형적인 Clean Architecture를 느슨하게 적용한 KMP 구조입니다.

- `feature/*`
  - 사용자 화면과 화면별 상태 관리
  - `feature-worry`, `feature-gacha`, `feature-result`, `feature-saved`는 MVI 스타일 ViewModel/Contract를 사용
  - `feature-onboarding`, `feature-profile`은 화면 성격상 로컬 상태를 더 많이 사용
- `domain`
  - 앱이 사용하는 핵심 모델과 use case
  - repository 인터페이스를 정의
- `data`
  - repository 구현체
  - 저장된 말씀, 인증 상태, 알림/테마 설정 같은 로컬 데이터 접근
- `core-network`
  - Supabase Auth와 Edge Function 호출
  - Gemini 프록시와 앱 버전 설정 조회 담당
- `composeApp`
  - 앱의 실제 진입점
  - `GraceOnDependencies`로 플랫폼별 의존성을 수동 조합
  - 공통 Navigation과 앱 전역 상태를 소유
- `iosApp`
  - SwiftUI 컨테이너
  - Compose 화면 호스팅
  - iOS 리워드 광고, 배너 오버레이, 공유, URL 열기, 로컬 알림 브리지

의존성 방향은 대체로 아래처럼 유지합니다.

```text
composeApp
 ├── feature/*
 ├── domain
 ├── data
 └── core/*

feature/* -> domain, core-ui, core-common
data      -> domain, core-common, core-network
domain    -> core-common
```

## 화면 흐름

```text
Login
  -> Worry(Home)
  -> Gacha
  -> Result

Worry(Home)
  -> Saved
  -> Profile
```

- 홈에서는 무료 사용량, 최근 저장한 말씀, 인라인 광고를 함께 보여줍니다.
- 결과 화면에서는 말씀 카드, 기도문, 홈 복귀 액션 아래 인라인 광고를 보여줍니다.
- 프로필에서는 앱 버전, 알림, 테마, 리워드 광고 진입점을 제공합니다.

## 광고와 업데이트 체크 구조

### 광고

- Android
  - `composeApp/src/androidMain`에서 `AdView`와 리워드 광고 매니저를 직접 사용
- iOS
  - `iosApp/iosApp/RewardedAdManager.swift`
  - `iosApp/iosApp/InlineBannerAdOverlay.swift`

현재 광고 슬롯은 다음 3개를 기준으로 동작합니다.

- `ADMOB_REWARDED_AD_UNIT_ID`
- `ADMOB_HOME_BANNER_AD_UNIT_ID`
- `ADMOB_RESULT_BANNER_AD_UNIT_ID`

### 앱 업데이트 체크

앱 시작 시 `app-version-config` Edge Function을 호출해서 플랫폼별 버전 정책을 읽습니다.

- `latestVersion`
  - 현재 앱보다 높으면 선택 업데이트 팝업 노출
- `minimumSupportedVersion`
  - 현재 앱보다 높으면 강제 업데이트 팝업 노출

비교와 분기 로직은 `composeApp/src/commonMain/kotlin/com/graceon/update/AppUpdateModels.kt`에 있고, 실제 팝업 노출은 `composeApp/src/commonMain/kotlin/com/graceon/GraceOnRoot.kt`에서 처리합니다.

## 로컬 설정

앱은 Gemini를 직접 호출하지 않고 Supabase Edge Function을 통해 프록시 호출합니다. Android와 iOS 모두 로컬 설정 파일이 필요합니다.

### Android `local.properties`

```properties
GRACEON_API_BASE_URL=https://<your-project-ref>.supabase.co/functions/v1/generate-gemini-content
SUPABASE_ANON_KEY=<your-supabase-anon-key>

ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
ADMOB_REWARDED_AD_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
ADMOB_HOME_BANNER_AD_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/aaaaaaaaaa
ADMOB_RESULT_BANNER_AD_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/bbbbbbbbbb

ANDROID_APP_STORE_URL=https://play.google.com/store/apps/details?id=com.graceon
```

### iOS `iosApp/Configuration/Config.local.xcconfig`

`xcconfig`에서는 `//`가 주석이라 URL을 `https:/$()/...` 형태로 적어야 합니다.

```xcconfig
GRACEON_API_BASE_URL=https:/$()/your-project-ref.supabase.co/functions/v1/generate-gemini-content
SUPABASE_ANON_KEY=your-supabase-anon-key

ADMOB_APP_ID=ca-app-pub-ios-app-id
ADMOB_REWARDED_AD_UNIT_ID=ca-app-pub-ios-rewarded-unit-id
ADMOB_HOME_BANNER_AD_UNIT_ID=ca-app-pub-ios-home-banner-unit-id
ADMOB_RESULT_BANNER_AD_UNIT_ID=ca-app-pub-ios-result-banner-unit-id

IOS_APP_STORE_URL=https:/$()/apps.apple.com/kr/app/your-app-name/id1234567890
```

## Supabase 설정

### 1. 프로젝트 연결

```bash
supabase login
supabase link --project-ref <your-project-ref>
```

### 2. Gemini Key 등록

```bash
supabase secrets set GEMINI_API_KEY=your_gemini_api_key
```

### 3. 앱 업데이트 버전 정책 등록

```bash
supabase secrets set \
  ANDROID_LATEST_VERSION=1.1.0 \
  ANDROID_MINIMUM_SUPPORTED_VERSION=1.0.0 \
  IOS_LATEST_VERSION=1.1.0 \
  IOS_MINIMUM_SUPPORTED_VERSION=1.0.0
```

선택/강제 업데이트 문구도 별도로 덮어쓸 수 있습니다.

```bash
supabase secrets set \
  ANDROID_OPTIONAL_UPDATE_TITLE="새 버전이 있어요" \
  ANDROID_OPTIONAL_UPDATE_MESSAGE="새 기능과 안정성 개선이 포함되어 있어요." \
  ANDROID_REQUIRED_UPDATE_TITLE="업데이트가 필요해요" \
  ANDROID_REQUIRED_UPDATE_MESSAGE="계속 사용하려면 최신 버전으로 업데이트해주세요." \
  IOS_OPTIONAL_UPDATE_TITLE="새 버전이 있어요" \
  IOS_OPTIONAL_UPDATE_MESSAGE="새 기능과 안정성 개선이 포함되어 있어요." \
  IOS_REQUIRED_UPDATE_TITLE="업데이트가 필요해요" \
  IOS_REQUIRED_UPDATE_MESSAGE="계속 사용하려면 최신 버전으로 업데이트해주세요."
```

### 4. Edge Function 배포

```bash
supabase functions deploy generate-gemini-content --no-verify-jwt
supabase functions deploy app-version-config --no-verify-jwt
```

더 자세한 배포 절차는 `SUPABASE_EDGE_FUNCTIONS_SETUP.md`를 참고하세요.

## 빌드 및 실행

### Android

```bash
./gradlew :composeApp:assembleDebug
```

설치까지 하려면:

```bash
./gradlew :composeApp:installDebug
```

### iOS

공통 프레임워크 빌드:

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Xcode 프로젝트 열기:

```bash
open iosApp/iosApp.xcodeproj
```

CLI 빌드:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme GraceNote -configuration Debug -sdk iphonesimulator build CODE_SIGNING_ALLOWED=NO
```

## 모듈별 역할 정리

### `composeApp`

- `App.kt`
  - 공통 앱 진입 함수
- `GraceOnRoot.kt`
  - 인증 상태, 시작 화면, 앱 시작 버전 체크
- `navigation/NavGraph.kt`
  - 전체 화면 흐름과 탭 이동
- `GraceOnDependencies.kt`
  - 수동 의존성 컨테이너
- `update/AppUpdateModels.kt`
  - 버전 비교와 선택/강제 업데이트 분기

### `feature`

- `feature-onboarding`
  - 로그인, 회원가입, 인증 메일 재발송, 비밀번호 재설정
- `feature-worry`
  - 홈, 고민 입력, 최근 저장 말씀, 무료 사용량
- `feature-gacha`
  - 생성 중 애니메이션
- `feature-result`
  - 말씀 결과, 기도문, 공유, 저장
- `feature-saved`
  - 저장된 말씀 목록/삭제
- `feature-profile`
  - 버전 정보, 알림, 테마, 리워드 광고, 로그아웃

### `core`

- `core-common`
  - Result, 공통 에러, 리워드 결과 모델
- `core-network`
  - Supabase 인증, 세션 저장, Edge Function 호출
- `core-ui`
  - 앱 테마, 공통 Scaffold, 카드, 배경, 바텀바

### `build-logic`

직접 의존성을 흩뿌리지 않도록 convention plugin으로 Compose/KMP/Android 설정을 공통화합니다.

- `graceon.compose.multiplatform`
- `graceon.android.compose`
- `graceon.android.application`
- `graceon.android.library`
- `graceon.android.feature`

## 개발 메모

- Compose 공통 의존성은 `gradle/libs.versions.toml`과 `build-logic`에서 관리합니다.
- `compose.*` 축약 accessor 대신 직접 정의한 version catalog 의존성을 사용합니다.
- iOS 광고는 Compose 공통 코드만으로 끝나지 않고 Swift 브리지가 함께 필요합니다.
- 앱 업데이트 팝업은 스토어 URL이 비어 있으면 노출되지 않습니다.

## 검증에 자주 쓰는 명령어

```bash
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinIosSimulatorArm64
xcodebuild -project iosApp/iosApp.xcodeproj -scheme GraceNote -configuration Debug -sdk iphonesimulator build CODE_SIGNING_ALLOWED=NO
```

## 라이선스

MIT License
