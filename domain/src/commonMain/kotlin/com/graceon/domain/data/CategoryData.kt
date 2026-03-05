package com.graceon.domain.data

import com.graceon.domain.model.Category
import com.graceon.domain.model.ColorType
import com.graceon.domain.model.DetailWorry
import com.graceon.domain.model.IconType

/**
 * 카테고리 정적 데이터
 */
object CategoryData {
    val categories = listOf(
        Category(
            id = "career",
            title = "진로 / 직장",
            iconType = IconType.BRIEFCASE,
            colorType = ColorType.BLUE,
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
            id = "relationship",
            title = "인간 관계",
            iconType = IconType.USER,
            colorType = ColorType.PINK,
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
            id = "life",
            title = "삶 / 미래",
            iconType = IconType.SUN,
            colorType = ColorType.YELLOW,
            details = listOf(
                DetailWorry(
                    id = "anxiety",
                    title = "미래 불안",
                    defaultVerse = "그러므로 내일 일을 위하여 염려하지 말라 내일 일은 내일이 염려할 것이요 (마태복음 6:34)"
                ),
                DetailWorry(
                    id = "money",
                    title = "재정 / 돈",
                    defaultVerse = "나의 하나님이 그리스도 예수 안에서 영광 가운데 그 풍성한 대로 너희 모든 쓸 것을 채우시리라 (빌립보서 4:19)"
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
                )
            )
        ),
        Category(
            id = "faith",
            title = "신앙 / 마음",
            iconType = IconType.HEART,
            colorType = ColorType.PURPLE,
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
                    title = "무기력함",
                    defaultVerse = "오직 여호와를 앙망하는 자는 새 힘을 얻으리니 독수리가 날개치며 올라감 같을 것이요 (이사야 40:31)"
                ),
                DetailWorry(
                    id = "prayer",
                    title = "응답 기다림",
                    defaultVerse = "너는 내게 부르짖으라 내가 네게 응답하겠고 네가 알지 못하는 크고 은밀한 일을 네게 보이리라 (예레미야 33:3)"
                )
            )
        )
    )
}
