import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const body = await req.json();
    const { adminId, adminName, email, password } = body;

    const supabaseAdmin = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
      { auth: { persistSession: false } }
    );

    // 1) Verify Caller Identity
    const authHeader = req.headers.get("Authorization");
    const token = authHeader?.replace("Bearer ", "").trim();
    const { data: { user: caller }, error: authError } = await supabaseAdmin.auth.getUser(token);

    if (authError || !caller) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 2) Get target Admin's current profile
    const { data: targetAdmin, error: fetchError } = await supabaseAdmin
      .from("Admin")
      .select("auth_user_id, Email")
      .eq("AdminID", adminId)
      .single();

    if (fetchError || !targetAdmin) {
      return new Response(JSON.stringify({ error: "Admin not found" }), {
        status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 3) Check Permissions: Must be an Admin OR updating own profile
    const isSelfUpdate = caller.id === targetAdmin.auth_user_id;

    let isCallerAdmin = false;
    const { data: callerAdmin } = await supabaseAdmin
      .from("Admin")
      .select("AdminID")
      .eq("auth_user_id", caller.id)
      .maybeSingle();

    if (callerAdmin) isCallerAdmin = true;

    if (!isSelfUpdate && !isCallerAdmin) {
      return new Response(JSON.stringify({ error: "Forbidden: You can only edit your own profile or must be an admin" }), {
        status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 4) Update Auth User ONLY if email/password actually changed
    const updateData: any = {};
    if (email && email !== targetAdmin.Email) updateData.email = email;
    if (password) updateData.password = password;

    if (Object.keys(updateData).length > 0) {
      const { error: updateAuthError } = await supabaseAdmin.auth.admin.updateUserById(
        targetAdmin.auth_user_id,
        updateData
      );
      if (updateAuthError) {
        return new Response(JSON.stringify({ error: `Auth Error: ${updateAuthError.message}` }), {
          status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }
    }

    // 5) Update Admin profile
    const { error: dbError } = await supabaseAdmin
      .from("Admin")
      .update({ AdminName: adminName, Email: email })
      .eq("AdminID", adminId);

    if (dbError) {
      return new Response(JSON.stringify({ error: dbError.message }), {
        status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    return new Response(JSON.stringify({ success: true }), {
      status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  } catch (e) {
    return new Response(JSON.stringify({ error: (e as Error).message }), {
      status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  }
});
