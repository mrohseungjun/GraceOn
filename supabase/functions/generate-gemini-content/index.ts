import "jsr:@supabase/functions-js/edge-runtime.d.ts"
import { createClient } from "npm:@supabase/supabase-js@2"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

const DAILY_LIMIT = 1
const DAILY_USAGE_DATE_KEY = "graceon_daily_free_generation_date"
const DAILY_USAGE_COUNT_KEY = "graceon_daily_free_generation_count"
const DAILY_USAGE_TIMEZONE = "Asia/Seoul"

interface GenerateContentRequest {
  prompt?: string
}

interface DailyUsageStatusResponse {
  dailyLimit: number
  usedToday: number
  remainingToday: number
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
        usedToday: usage.usedToday,
        remainingToday: usage.remainingToday,
      } satisfies DailyUsageStatusResponse,
      { status: 200, headers: corsHeaders },
    )
  }

  if (usage.usedToday >= DAILY_LIMIT) {
    return Response.json(
      { error: "오늘 무료 말씀 1회를 모두 사용했습니다. 내일 다시 시도해주세요." },
      { status: 429, headers: corsHeaders },
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

  const prompt = body.prompt?.trim()
  if (!prompt) {
    return Response.json(
      { error: "prompt is required" },
      { status: 400, headers: corsHeaders },
    )
  }

  const adminClient = createClient(supabaseUrl, supabaseServiceRoleKey)
  const reservedMetadata = {
    ...currentUserMetadata,
    [DAILY_USAGE_DATE_KEY]: todayInKorea(),
    [DAILY_USAGE_COUNT_KEY]: usage.usedToday + 1,
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
  const usageDate = String(userMetadata[DAILY_USAGE_DATE_KEY] ?? "")
  const usedToday =
    usageDate === todayInKorea()
      ? Number(userMetadata[DAILY_USAGE_COUNT_KEY] ?? 0)
      : 0

  return {
    usedToday,
    remainingToday: Math.max(DAILY_LIMIT - usedToday, 0),
  }
}
