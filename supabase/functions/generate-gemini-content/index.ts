import "jsr:@supabase/functions-js/edge-runtime.d.ts"
import { createClient } from "npm:@supabase/supabase-js@2"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

const DAILY_LIMIT = 1
const REWARDED_DAILY_GRANT_LIMIT = 3
const REWARDED_CREDIT_BALANCE_LIMIT = 3
const DAILY_USAGE_DATE_KEY = "graceon_daily_free_generation_date"
const DAILY_USAGE_COUNT_KEY = "graceon_daily_free_generation_count"
const REWARDED_GRANT_DATE_KEY = "graceon_rewarded_generation_date"
const REWARDED_GRANT_COUNT_KEY = "graceon_rewarded_generation_grant_count"
const REWARDED_CREDIT_COUNT_KEY = "graceon_rewarded_generation_credit_count"
const DAILY_USAGE_TIMEZONE = "Asia/Seoul"

interface GenerateContentRequest {
  prompt?: string
  action?: "grant_reward"
}

interface DailyUsageStatusResponse {
  dailyLimit: number
  usedToday: number
  remainingToday: number
  rewardedCredits: number
  rewardedAvailableToday: number
}

interface GeminiGenerateResponse {
  candidates?: Array<{
    content?: {
      parts?: Array<{ text?: string }>
    }
  }>
}

const todayInKorea = () =>
  new Intl.DateTimeFormat("en-CA", { timeZone: DAILY_USAGE_TIMEZONE }).format(new Date())

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  if (request.method !== "POST" && request.method !== "GET") {
    return Response.json(
      { error: "Method not allowed" },
      { status: 405, headers: corsHeaders },
    )
  }

  const geminiApiKey = Deno.env.get("GEMINI_API_KEY") ?? ""
  const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? ""
  const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? ""
  const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
  if (!geminiApiKey) {
    return Response.json(
      { error: "Missing GEMINI_API_KEY secret" },
      { status: 500, headers: corsHeaders },
    )
  }
  if (!supabaseUrl || !supabaseAnonKey || !supabaseServiceRoleKey) {
    return Response.json(
      { error: "Missing Supabase runtime configuration" },
      { status: 500, headers: corsHeaders },
    )
  }

  const authorization = request.headers.get("Authorization")
  if (!authorization?.startsWith("Bearer ")) {
    return Response.json(
      { error: "Authorization header is required" },
      { status: 401, headers: corsHeaders },
    )
  }

  const userClient = createClient(supabaseUrl, supabaseAnonKey, {
    global: {
      headers: {
        Authorization: authorization,
      },
    },
  })

  const {
    data: { user },
    error: userError,
  } = await userClient.auth.getUser()

  if (userError || !user) {
    console.error("Supabase user lookup failed", userError)
    return Response.json(
      { error: "Authentication is required" },
      { status: 401, headers: corsHeaders },
    )
  }

  const currentUserMetadata = (user.user_metadata ?? {}) as Record<string, unknown>
  const usage = resolveDailyUsage(currentUserMetadata)

  if (request.method === "GET") {
    return Response.json(
      {
        dailyLimit: DAILY_LIMIT,
        usedToday: usage.freeUsedToday,
        remainingToday: usage.remainingFreeToday,
        rewardedCredits: usage.rewardedCredits,
        rewardedAvailableToday: usage.rewardedAvailableToday,
      } satisfies DailyUsageStatusResponse,
      { status: 200, headers: corsHeaders },
    )
  }

  let body: GenerateContentRequest
  try {
    body = await request.json()
  } catch {
    return Response.json(
      { error: "Invalid JSON body" },
      { status: 400, headers: corsHeaders },
    )
  }

  if (body.action === "grant_reward") {
    if (usage.rewardedAvailableToday <= 0) {
      const message =
        usage.rewardedCredits >= REWARDED_CREDIT_BALANCE_LIMIT
          ? `보너스 횟수는 최대 ${REWARDED_CREDIT_BALANCE_LIMIT}회까지 보관할 수 있습니다. 먼저 사용한 뒤 다시 광고를 시청해주세요.`
          : "오늘 광고로 받을 수 있는 추가 횟수를 모두 받았습니다. 내일 다시 시도해주세요."

      return Response.json(
        { error: message },
        { status: 429, headers: corsHeaders },
      )
    }

    const adminClient = createClient(supabaseUrl, supabaseServiceRoleKey)
    const rewardedMetadata = {
      ...currentUserMetadata,
      [REWARDED_GRANT_DATE_KEY]: todayInKorea(),
      [REWARDED_GRANT_COUNT_KEY]: usage.rewardedGrantedToday + 1,
      [REWARDED_CREDIT_COUNT_KEY]: Math.min(
        usage.rewardedCredits + 1,
        REWARDED_CREDIT_BALANCE_LIMIT,
      ),
    }

    const { error: rewardGrantError } = await adminClient.auth.admin.updateUserById(user.id, {
      user_metadata: rewardedMetadata,
    })

    if (rewardGrantError) {
      console.error("Failed to grant rewarded credit", rewardGrantError)
      return Response.json(
        { error: "광고 보상을 반영하지 못했습니다. 잠시 후 다시 시도해주세요." },
        { status: 500, headers: corsHeaders },
      )
    }

    return Response.json(
      {
        dailyLimit: DAILY_LIMIT,
        usedToday: usage.freeUsedToday,
        remainingToday: usage.remainingFreeToday,
        rewardedCredits: Math.min(
          usage.rewardedCredits + 1,
          REWARDED_CREDIT_BALANCE_LIMIT,
        ),
        rewardedAvailableToday: Math.min(
          REWARDED_DAILY_GRANT_LIMIT - (usage.rewardedGrantedToday + 1),
          Math.max(REWARDED_CREDIT_BALANCE_LIMIT - (usage.rewardedCredits + 1), 0),
        ),
      } satisfies DailyUsageStatusResponse,
      { status: 200, headers: corsHeaders },
    )
  }

  const prompt = body.prompt?.trim()
  if (!prompt) {
    return Response.json(
      { error: "prompt is required" },
      { status: 400, headers: corsHeaders },
    )
  }

  if (usage.remainingFreeToday <= 0 && usage.rewardedCredits <= 0) {
    return Response.json(
      usage.rewardedAvailableToday > 0
        ? {
            error: "오늘 무료 말씀 1회를 모두 사용했습니다. 광고를 보고 보너스 1회를 추가할 수 있습니다.",
            rewardedEligible: true,
          }
        : {
            error: "오늘 무료 말씀과 광고 보상을 모두 사용했습니다. 내일 다시 시도해주세요.",
            rewardedEligible: false,
          },
      { status: 429, headers: corsHeaders },
    )
  }

  const adminClient = createClient(supabaseUrl, supabaseServiceRoleKey)
  const reservedMetadata =
    usage.remainingFreeToday > 0
      ? {
          ...currentUserMetadata,
          [DAILY_USAGE_DATE_KEY]: todayInKorea(),
          [DAILY_USAGE_COUNT_KEY]: usage.freeUsedToday + 1,
        }
      : {
          ...currentUserMetadata,
          [REWARDED_CREDIT_COUNT_KEY]: Math.max(usage.rewardedCredits - 1, 0),
        }

  const { error: reserveUsageError } = await adminClient.auth.admin.updateUserById(user.id, {
    user_metadata: reservedMetadata,
  })

  if (reserveUsageError) {
    console.error("Failed to reserve daily generation usage", reserveUsageError)
    return Response.json(
      { error: "무료 사용량을 확인하지 못했습니다. 잠시 후 다시 시도해주세요." },
      { status: 500, headers: corsHeaders },
    )
  }

  const geminiResponse = await fetch(
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": geminiApiKey,
      },
      body: JSON.stringify({
        contents: [
          {
            parts: [{ text: prompt }],
          },
        ],
      }),
    },
  )

  if (!geminiResponse.ok) {
    const errorText = await geminiResponse.text()
    console.error("Gemini request failed", geminiResponse.status, errorText)
    await adminClient.auth.admin.updateUserById(user.id, {
      user_metadata: currentUserMetadata,
    })
    return Response.json(
      { error: "Gemini request failed" },
      { status: geminiResponse.status, headers: corsHeaders },
    )
  }

  const payload = (await geminiResponse.json()) as GeminiGenerateResponse
  const text = payload.candidates?.[0]?.content?.parts?.[0]?.text?.trim()

  if (!text) {
    await adminClient.auth.admin.updateUserById(user.id, {
      user_metadata: currentUserMetadata,
    })
    return Response.json(
      { error: "Empty response from Gemini" },
      { status: 502, headers: corsHeaders },
    )
  }

  return Response.json({ text }, { status: 200, headers: corsHeaders })
})

function resolveDailyUsage(userMetadata: Record<string, unknown>) {
  const today = todayInKorea()
  const freeUsageDate = String(userMetadata[DAILY_USAGE_DATE_KEY] ?? "")
  const rewardedGrantDate = String(userMetadata[REWARDED_GRANT_DATE_KEY] ?? "")
  const freeUsedToday =
    freeUsageDate === today
      ? sanitizeCount(userMetadata[DAILY_USAGE_COUNT_KEY])
      : 0
  const rewardedGrantedToday =
    rewardedGrantDate === today
      ? sanitizeCount(userMetadata[REWARDED_GRANT_COUNT_KEY])
      : 0
  const rewardedCredits = sanitizeCount(userMetadata[REWARDED_CREDIT_COUNT_KEY])
  const rewardedAvailableByDailyLimit = Math.max(
    REWARDED_DAILY_GRANT_LIMIT - rewardedGrantedToday,
    0,
  )
  const rewardedAvailableByBalance = Math.max(
    REWARDED_CREDIT_BALANCE_LIMIT - rewardedCredits,
    0,
  )

  return {
    freeUsedToday,
    remainingFreeToday: Math.max(DAILY_LIMIT - freeUsedToday, 0),
    rewardedCredits,
    rewardedGrantedToday,
    rewardedAvailableToday: Math.min(
      rewardedAvailableByDailyLimit,
      rewardedAvailableByBalance,
    ),
  }
}

function sanitizeCount(value: unknown): number {
  const parsed = Number(value ?? 0)
  if (!Number.isFinite(parsed) || parsed <= 0) return 0
  return Math.floor(parsed)
}
