import "jsr:@supabase/functions-js/edge-runtime.d.ts"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

type SupportedPlatform = "android" | "ios"

interface AppVersionConfigResponse {
  latestVersion: string
  minimumSupportedVersion: string
  optionalTitle: string
  optionalMessage: string
  requiredTitle: string
  requiredMessage: string
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  if (request.method !== "GET") {
    return Response.json(
      { error: "Method not allowed" },
      { status: 405, headers: corsHeaders },
    )
  }

  const url = new URL(request.url)
  const platform = url.searchParams.get("platform")?.trim().toLowerCase()

  if (platform !== "android" && platform !== "ios") {
    return Response.json(
      { error: "platform query parameter must be android or ios" },
      { status: 400, headers: corsHeaders },
    )
  }

  return Response.json(
    resolveVersionConfig(platform),
    { status: 200, headers: corsHeaders },
  )
})

function resolveVersionConfig(platform: SupportedPlatform): AppVersionConfigResponse {
  const prefix = platform === "android" ? "ANDROID" : "IOS"

  return {
    latestVersion: Deno.env.get(`${prefix}_LATEST_VERSION`) ?? "",
    minimumSupportedVersion: Deno.env.get(`${prefix}_MINIMUM_SUPPORTED_VERSION`) ?? "",
    optionalTitle: Deno.env.get(`${prefix}_OPTIONAL_UPDATE_TITLE`) ?? "새 버전이 있어요",
    optionalMessage:
      Deno.env.get(`${prefix}_OPTIONAL_UPDATE_MESSAGE`) ??
      "업데이트하고 더 안정적인 GraceOn을 사용해보세요.",
    requiredTitle: Deno.env.get(`${prefix}_REQUIRED_UPDATE_TITLE`) ?? "업데이트가 필요해요",
    requiredMessage:
      Deno.env.get(`${prefix}_REQUIRED_UPDATE_MESSAGE`) ??
      "계속 사용하려면 최신 버전으로 업데이트해주세요.",
  }
}
