# 힐링 말씀 (Grace Note)

AI 기반 성경 말씀 말씀 앱 - Kotlin Compose Multiplatform + Clean Architecture + MVI

## 프로젝트 개요

사용자의 고민을 듣고 AI가 적절한 성경 말씀과 위로의 메시지, 기도문을 제공하는 Kotlin Compose Multiplatform 앱입니다.

## 아키텍처

### Clean Architecture + MVI Pattern

```
composeApp/             # Compose Multiplatform 진입점(Android/iOS)
├── di/                # Koin DI 설정
├── navigation/        # Navigation Graph
└── MainActivity.kt

feature/               # Feature 모듈 (UI Layer)
├── feature-onboarding/# 온보딩
├── feature-worry/    # 고민 선택 화면
├── feature-gacha/    # 가챠 애니메이션
├── feature-result/   # 결과 화면
└── feature-saved/    # 저장된 말씀

domain/               # Domain Layer (비즈니스 로직)
├── model/           # 도메인 모델
├── repository/      # Repository 인터페이스
└── usecase/         # UseCase

data/                # Data Layer
└── repository/      # Repository 구현체

core/                # Core 모듈
├── core-ui/         # 디자인 시스템, 공통 UI 컴포넌트
├── core-network/    # Ktor 클라이언트, API
└── core-common/     # 공통 유틸리티

build-logic/         # 컨벤션 플러그인
└── convention/      # Gradle 컨벤션 플러그인
```

## 기술 스택

### UI
- **Jetpack Compose**: 선언형 UI
- **Material3**: 디자인 시스템
- **Navigation Compose**: 화면 전환

### Architecture
- **Clean Architecture**: 계층 분리
- **MVI Pattern**: 단방향 데이터 흐름
- **StateFlow**: 상태 관리

### Network & Data
- **Ktor Client**: HTTP 클라이언트
- **Kotlinx Serialization**: JSON 직렬화
- **Supabase Edge Functions**: Gemini 프록시
- **Gemini API**: AI 말씀 생성

### DI & Async
- **Koin**: 의존성 주입
- **Coroutines**: 비동기 처리

### Build
- **Version Catalog**: 의존성 관리
- **Convention Plugins**: 빌드 로직 재사용

## 모듈 의존성 그래프

```
app
 ├─> feature-onboarding
 ├─> feature-worry
 ├─> feature-gacha
 ├─> feature-result
 ├─> feature-saved
 ├─> domain
 ├─> data
 │    ├─> core-network
 │    └─> core-common
 └─> core-ui
      └─> core-common
```

## 시작하기

### 1. 프록시 URL 설정

앱은 Gemini를 직접 호출하지 않고 Supabase Edge Function을 호출합니다.

Android `local.properties`
```properties
GRACEON_API_BASE_URL=https://<your-project-ref>.supabase.co/functions/v1/generate-gemini-content
SUPABASE_ANON_KEY=<your-supabase-anon-key>
```

iOS `iosApp/Configuration/Config.local.xcconfig`
```xcconfig
// In xcconfig, '//' starts a comment, so use https:/$()/...
GRACEON_API_BASE_URL=https:/$()/<your-project-ref>.supabase.co/functions/v1/generate-gemini-content
SUPABASE_ANON_KEY=<your-supabase-anon-key>
```

Supabase Edge Function 설정 절차는 `SUPABASE_EDGE_FUNCTIONS_SETUP.md`를 참고하세요.

### 2. 빌드 및 실행

```bash
./gradlew :composeApp:assembleDebug
```

## 주요 기능

### 1. 고민 선택
- **카테고리 모드**: 진로/직장, 인간관계, 삶/미래, 신앙/마음
- **AI 모드**: 자유롭게 고민 작성

### 2. 가챠 애니메이션
- 가챠 머신 애니메이션
- 화면 진입 시 자동으로 말씀 생성 시작

### 3. 말씀 결과
- 성경 말씀 + 위로 메시지
- AI 기도문 생성
- 말씀 공유 기능

## 디자인 시스템

모든 UI 컴포넌트와 테마는 `core-ui` 모듈에서 관리됩니다.

### 컬러 팔레트
- **Primary**: Indigo (#4F46E5)
- **Secondary**: Purple (#9333EA)
- **Category Colors**: Blue, Pink, Yellow, Purple gradients

### 컴포넌트
- `GradientCard`: 그라데이션 카드
- `GraceNoteTheme`: 앱 테마

## MVI Pattern

각 Feature는 다음 구조를 따릅니다:

```kotlin
// Contract
object XxxContract {
    data class State(...)           // UI 상태 (Immutable)
    sealed interface Intent { ... } // 사용자 의도
    sealed interface Effect { ... } // 일회성 이벤트
}

// ViewModel
class XxxViewModel : ViewModel() {
    val state: StateFlow<State>
    val effect: Flow<Effect>
    fun handleIntent(intent: Intent)
}

// Screen
@Composable
fun XxxScreen(viewModel: XxxViewModel)
```

## 개발 가이드

### 새로운 Feature 추가 시

1. `feature/feature-xxx` 모듈 생성
2. `build.gradle.kts`에 컨벤션 플러그인 적용:
   ```kotlin
   plugins {
       id("graceon.android.feature")
   }
   ```
3. MVI Contract 정의
4. ViewModel 구현
5. Composable Screen 작성
6. Navigation에 추가

### 의존성 추가 시

1. `gradle/libs.versions.toml`에 버전 정의
2. 필요한 모듈의 `build.gradle.kts`에서 참조
3. **절대 직접 버전 명시 금지**

## 라이선스

MIT License
