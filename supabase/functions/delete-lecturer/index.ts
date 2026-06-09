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
    const { lecturerId } = await req.json();

    const SUPABASE_URL = Deno.env.get("SUPABASE_URL");
    const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

    const supabaseAdmin = createClient(SUPABASE_URL!, SUPABASE_SERVICE_ROLE_KEY!, {
      auth: { persistSession: false },
    });

    // 1) Verify Caller Identity
    const authHeader = req.headers.get("Authorization");
    const token = authHeader?.replace("Bearer ", "").trim();
    const { data: { user: caller }, error: authError } = await supabaseAdmin.auth.getUser(token);

    if (authError || !caller) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 2) Get target Lecturer info
    const { data: targetLecturer, error: fetchError } = await supabaseAdmin
      .from("Lecturer")
      .select("auth_user_id, ProgrammeID, LecturerRole")
      .eq("LecturerID", lecturerId)
      .single();

    if (fetchError || !targetLecturer) {
      return new Response(JSON.stringify({ error: "Lecturer not found" }), {
        status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 3) Check Permissions
    let isAuthorized = false;

    // Check Admin
    const { data: callerAdmin } = await supabaseAdmin
      .from("Admin")
      .select("AdminID")
      .eq("auth_user_id", caller.id)
      .maybeSingle();

    if (callerAdmin) {
        isAuthorized = true;
    } else {
        // Check Coordinator
        const { data: callerCoord } = await supabaseAdmin
            .from("Lecturer")
            .select("ProgrammeID")
            .eq("auth_user_id", caller.id)
            .eq("LecturerRole", "Course Coordinator")
            .maybeSingle();

        if (callerCoord && callerCoord.ProgrammeID === targetLecturer.ProgrammeID) {
            // Coordinator can delete lecturers in their programme, but NOT other coordinators
            if (targetLecturer.LecturerRole !== "Course Coordinator") {
                isAuthorized = true;
            } else {
                return new Response(JSON.stringify({ error: "Forbidden: Coordinators cannot delete other coordinators" }), {
                    status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" }
                });
            }
        }
    }

    if (!isAuthorized) {
      return new Response(JSON.stringify({ error: "Forbidden: Admin or Coordinator access required" }), {
        status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 4) Delete from Supabase Auth
    const { error: deleteAuthError } = await supabaseAdmin.auth.admin.deleteUser(targetLecturer.auth_user_id);

    if (deleteAuthError) {
      return new Response(JSON.stringify({ error: deleteAuthError.message }), {
        status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 5) Explicitly delete from Lecturer table
    await supabaseAdmin.from("Lecturer").delete().eq("LecturerID", lecturerId);

    return new Response(JSON.stringify({ success: true }), {
      status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  } catch (e) {
    return new Response(JSON.stringify({ error: (e as Error).message }), {
      status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  }
});
