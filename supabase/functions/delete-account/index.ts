import "jsr:@supabase/functions-js/edge-runtime.d.ts"
import { createClient } from "npm:@supabase/supabase-js@2"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
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

  const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? ""
  const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? ""
  const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""

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

  const adminClient = createClient(supabaseUrl, supabaseServiceRoleKey)
  const { error: deleteError } = await adminClient.auth.admin.deleteUser(user.id)

  if (deleteError) {
    console.error("Failed to delete user", deleteError)
    return Response.json(
      { error: "계정을 삭제하지 못했습니다. 잠시 후 다시 시도해주세요." },
      { status: 500, headers: corsHeaders },
    )
  }

  return Response.json(
    { success: true },
    { status: 200, headers: corsHeaders },
  )
})
