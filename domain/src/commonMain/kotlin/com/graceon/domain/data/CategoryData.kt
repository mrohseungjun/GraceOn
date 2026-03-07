package com.graceon.domain.data

import com.graceon.domain.model.Category
import com.graceon.domain.model.ColorType
import com.graceon.domain.model.DetailWorry
import com.graceon.domain.model.IconType

/**
 * 고민 카테고리 정적 데이터
 */
object CategoryData {
    const val CUSTOM_CATEGORY_ID = "custom"

    val categories = listOf(
        Category(
            id = "career",
            title = "진로 / 직장",
            iconType = IconType.BRIEFCASE,
            colorType = ColorType.BLUE,
            description = "취업, 이직, 번아웃, 실수",
            details = listOf(
                DetailWorry(
                    id = "job_seeking",
                    title = "취업 준비",
                    defaultVerse = "너희를 향한 나의 생각을 내가 아나니 평안이요 재앙이 아니니라 (예레미야 29:11)"
                ),
                DetailWorry(
                    id = "turnover",
                    title = "이직 고민",
                    defaultVerse = "사람이 마음으로 자기의 길을 계획할지라도 그의 걸음을 인도하시는 이는 여호와시니라 (잠언 16:9)"
                ),
                DetailWorry(
                    id = "burnout",
                    title = "번아웃 / 지침",
                    defaultVerse = "수고하고 무거운 짐 진 자들아 다 내게로 오라 내가 너희를 쉬게 하리라 (마태복음 11:28)"
                ),
                DetailWorry(
                    id = "failure",
                    title = "실패 / 실수",
                    defaultVerse = "대저 의인은 일곱 번 넘어질지라도 다시 일어나려니와 악인은 재앙으로 말미암아 엎드러지느니라 (잠언 24:16)"
                )
            )
        ),
        Category(
            id = "study",
            title = "학업 / 시험",
            iconType = IconType.BRIEFCASE,
            colorType = ColorType.YELLOW,
            description = "시험, 성적, 집중, 진학",
            details = listOf(
                DetailWorry(
                    id = "exam_pressure",
                    title = "시험 압박",
                    defaultVerse = "아무 것도 염려하지 말고 다만 모든 일에 기도와 간구로 너희 구할 것을 하나님께 아뢰라 (빌립보서 4:6)"
                ),
                DetailWorry(
                    id = "grades",
                    title = "성적 걱정",
                    defaultVerse = "너는 마음을 다하여 여호와를 신뢰하고 네 명철을 의지하지 말라 (잠언 3:5)"
                ),
                DetailWorry(
                    id = "focus",
                    title = "집중이 안 됨",
                    defaultVerse = "오직 여호와를 앙망하는 자는 새 힘을 얻으리니 (이사야 40:31)"
                ),
                DetailWorry(
                    id = "future_school",
                    title = "진학 / 선택",
                    defaultVerse = "너의 행사를 여호와께 맡기라 그리하면 네가 경영하는 것이 이루어지리라 (잠언 16:3)"
                )
            )
        ),
        Category(
            id = "relationship",
            title = "인간 관계",
            iconType = IconType.USER,
            colorType = ColorType.PINK,
            description = "외로움, 갈등, 상실, 비교",
            details = listOf(
                DetailWorry(
                    id = "loneliness",
                    title = "외로움",
                    defaultVerse = "두려워하지 말라 내가 너와 함께 함이라 놀라지 말라 나는 네 하나님이 됨이라 (이사야 41:10)"
                ),
                DetailWorry(
                    id = "conflict",
                    title = "갈등 / 다툼",
                    defaultVerse = "모든 겸손과 온유로 하고 오래 참음으로 사랑 가운데서 서로 용납하고 (에베소서 4:2)"
                ),
                DetailWorry(
                    id = "breakup",
                    title = "이별 / 상실",
                    defaultVerse = "여호와는 마음이 상한 자를 가까이 하시고 충심으로 통회하는 자를 구원하시는도다 (시편 34:18)"
                ),
                DetailWorry(
                    id = "envy",
                    title = "비교 / 질투",
                    defaultVerse = "우리는 헛된 영광을 구하여 서로 노엽게 하거나 서로 투기하지 말지니라 (갈라디아서 5:26)"
                )
            )
        ),
        Category(
            id = "family",
            title = "가족 / 가정",
            iconType = IconType.USER,
            colorType = ColorType.BLUE,
            description = "부모, 부부, 자녀, 가정 문제",
            details = listOf(
                DetailWorry(
                    id = "parent_conflict",
                    title = "부모와의 갈등",
                    defaultVerse = "네 부모를 공경하라 그리하면 네 하나님 여호와가 네게 준 땅에서 네 생명이 길리라 (출애굽기 20:12)"
                ),
                DetailWorry(
                    id = "marriage",
                    title = "부부 문제",
                    defaultVerse = "서로 친절하게 하며 불쌍히 여기며 서로 용서하기를 하나님이 그리스도 안에서 너희를 용서하심 같이 하라 (에베소서 4:32)"
                ),
                DetailWorry(
                    id = "children",
                    title = "자녀 걱정",
                    defaultVerse = "마땅히 행할 길을 아이에게 가르치라 그리하면 늙어도 그것을 떠나지 아니하리라 (잠언 22:6)"
                ),
                DetailWorry(
                    id = "home_peace",
                    title = "가정의 평안",
                    defaultVerse = "오직 나와 내 집은 여호와를 섬기겠노라 (여호수아 24:15)"
                )
            )
        ),
        Category(
            id = "life",
            title = "삶 / 미래",
            iconType = IconType.SUN,
            colorType = ColorType.YELLOW,
            description = "미래, 건강, 방향, 일상 불안",
            details = listOf(
                DetailWorry(
                    id = "anxiety",
                    title = "미래 불안",
                    defaultVerse = "그러므로 내일 일을 위하여 염려하지 말라 내일 일은 내일이 염려할 것이요 (마태복음 6:34)"
                ),
                DetailWorry(
                    id = "health",
                    title = "건강 / 육체",
                    defaultVerse = "여호와여 내게 은혜를 베푸소서 내가 수척하였사오니 여호와여 나의 뼈가 떨리오니 나를 고치소서 (시편 6:2)"
                ),
                DetailWorry(
                    id = "direction",
                    title = "방향성 상실",
                    defaultVerse = "주의 말씀은 내 발에 등이요 내 길에 빛이니이다 (시편 119:105)"
                ),
                DetailWorry(
                    id = "daily_fatigue",
                    title = "일상에 지침",
                    defaultVerse = "피곤한 자에게는 능력을 주시며 무능한 자에게는 힘을 더하시나니 (이사야 40:29)"
                )
            )
        ),
        Category(
            id = "finance",
            title = "재정 / 현실",
            iconType = IconType.SUN,
            colorType = ColorType.PURPLE,
            description = "돈, 빚, 생계, 현실 부담",
            details = listOf(
                DetailWorry(
                    id = "money",
                    title = "재정 / 돈",
                    defaultVerse = "나의 하나님이 그리스도 예수 안에서 영광 가운데 그 풍성한 대로 너희 모든 쓸 것을 채우시리라 (빌립보서 4:19)"
                ),
                DetailWorry(
                    id = "debt",
                    title = "빚 / 부담",
                    defaultVerse = "너의 짐을 여호와께 맡기라 그가 너를 붙드시고 (시편 55:22)"
                ),
                DetailWorry(
                    id = "living_cost",
                    title = "생계 걱정",
                    defaultVerse = "그러므로 무엇을 먹을까 무엇을 마실까 무엇을 입을까 염려하지 말라 (마태복음 6:31)"
                ),
                DetailWorry(
                    id = "job_instability",
                    title = "현실적 압박",
                    defaultVerse = "하나님은 우리의 피난처시요 힘이시니 환난 중에 만날 큰 도움이시라 (시편 46:1)"
                )
            )
        ),
        Category(
            id = "emotion",
            title = "감정 / 회복",
            iconType = IconType.SPARKLE,
            colorType = ColorType.PINK,
            description = "불안, 우울, 무기력, 상처",
            details = listOf(
                DetailWorry(
                    id = "panic",
                    title = "불안 / 초조",
                    defaultVerse = "내가 여호와께 간구하매 내게 응답하시고 내 모든 두려움에서 나를 건지셨도다 (시편 34:4)"
                ),
                DetailWorry(
                    id = "sadness",
                    title = "슬픔 / 우울",
                    defaultVerse = "저녁에는 울음이 깃들일지라도 아침에는 기쁨이 오리로다 (시편 30:5)"
                ),
                DetailWorry(
                    id = "hurt",
                    title = "상처 / 서운함",
                    defaultVerse = "상한 갈대를 꺾지 아니하며 꺼져가는 등불을 끄지 아니하시기를 (이사야 42:3)"
                ),
                DetailWorry(
                    id = "numbness",
                    title = "무기력 / 공허함",
                    defaultVerse = "내 영혼아 네가 어찌하여 낙심하며 어찌하여 내 속에서 불안하여 하는가 너는 하나님께 소망을 두라 (시편 42:11)"
                )
            )
        ),
        Category(
            id = "faith",
            title = "신앙 / 마음",
            iconType = IconType.HEART,
            colorType = ColorType.PURPLE,
            description = "죄책감, 의심, 영적 침체, 기도",
            details = listOf(
                DetailWorry(
                    id = "guilt",
                    title = "죄책감",
                    defaultVerse = "만일 우리가 우리 죄를 자백하면 그는 미쁘시고 의로우사 우리 죄를 사하시며 (요한일서 1:9)"
                ),
                DetailWorry(
                    id = "doubt",
                    title = "의심 / 회의",
                    defaultVerse = "너희 중에 누구든지 지혜가 부족하거든 모든 사람에게 후히 주시고 꾸짖지 아니하시는 하나님께 구하라 (야고보서 1:5)"
                ),
                DetailWorry(
                    id = "dryness",
                    title = "영적 무기력",
                    defaultVerse = "오직 여호와를 앙망하는 자는 새 힘을 얻으리니 독수리가 날개치며 올라감 같을 것이요 (이사야 40:31)"
                ),
                DetailWorry(
                    id = "prayer",
                    title = "응답 기다림",
                    defaultVerse = "너는 내게 부르짖으라 내가 네게 응답하겠고 네가 알지 못하는 크고 은밀한 일을 네게 보이리라 (예레미야 33:3)"
                )
            )
        ),
        Category(
            id = CUSTOM_CATEGORY_ID,
            title = "직접 고민 쓰기",
            iconType = IconType.EDIT,
            colorType = ColorType.BLUE,
            description = "내 고민을 직접 적고 말씀을 받아볼래요",
            details = emptyList()
        )
    )
}
