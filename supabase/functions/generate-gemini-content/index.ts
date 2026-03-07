import "jsr:@supabase/functions-js/edge-runtime.d.ts"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

interface GenerateContentRequest {
  prompt?: string
}

interface GeminiGenerateResponse {
  candidates?: Array<{
    content?: {
      parts?: Array<{ text?: string }>
    }
  }>
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  if (request.method !== "POST") {
    return Response.json(
      { error: "Method not allowed" },
      { status: 405, headers: corsHeaders },
    )
  }

  const geminiApiKey = Deno.env.get("GEMINI_API_KEY") ?? ""
  if (!geminiApiKey) {
    return Response.json(
      { error: "Missing GEMINI_API_KEY secret" },
      { status: 500, headers: corsHeaders },
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
    return Response.json(
      { error: "Gemini request failed" },
      { status: geminiResponse.status, headers: corsHeaders },
    )
  }

  const payload = (await geminiResponse.json()) as GeminiGenerateResponse
  const text = payload.candidates?.[0]?.content?.parts?.[0]?.text?.trim()

  if (!text) {
    return Response.json(
      { error: "Empty response from Gemini" },
      { status: 502, headers: corsHeaders },
    )
  }

  return Response.json({ text }, { status: 200, headers: corsHeaders })
})
