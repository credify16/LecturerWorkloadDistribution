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
    const { adminId } = await req.json();

    const SUPABASE_URL = Deno.env.get("SUPABASE_URL");
    const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

    const supabaseAdmin = createClient(SUPABASE_URL!, SUPABASE_SERVICE_ROLE_KEY!, {
      auth: { persistSession: false },
    });

    // 1) Verify Caller is an Admin
    const authHeader = req.headers.get("Authorization");
    const token = authHeader?.replace("Bearer ", "").trim();
    const { data: { user: caller }, error: authError } = await supabaseAdmin.auth.getUser(token);

    if (authError || !caller) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    const { data: callerAdmin } = await supabaseAdmin
      .from("Admin")
      .select("AdminID")
      .eq("auth_user_id", caller.id)
      .maybeSingle();

    if (!callerAdmin) {
      return new Response(JSON.stringify({ error: "Forbidden: Only admins can delete other admins" }), {
        status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 2) Get the Auth User ID of the admin to be deleted
    const { data: targetAdmin, error: fetchError } = await supabaseAdmin
      .from("Admin")
      .select("auth_user_id")
      .eq("AdminID", adminId)
      .single();

    if (fetchError || !targetAdmin) {
      return new Response(JSON.stringify({ error: "Admin not found" }), {
        status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 3) Delete from Supabase Auth (This will cascade delete from Admin table if configured)
    const { error: deleteAuthError } = await supabaseAdmin.auth.admin.deleteUser(targetAdmin.auth_user_id);

    if (deleteAuthError) {
      return new Response(JSON.stringify({ error: deleteAuthError.message }), {
        status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 4) Explicitly delete from Admin table just in case Cascade isn't set up
    await supabaseAdmin.from("Admin").delete().eq("AdminID", adminId);

    return new Response(JSON.stringify({ success: true }), {
      status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  } catch (e) {
    return new Response(JSON.stringify({ error: e.message }), {
      status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  }
});
