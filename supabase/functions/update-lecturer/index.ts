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
    const { lecturerId, lecturerName, position, role, btsa, credit, type, deptId, email, progId } = body;

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

    // 2) Get target Lecturer's current profile
    const { data: targetLecturer, error: fetchError } = await supabaseAdmin
      .from("Lecturer")
      .select("auth_user_id, Email, ProgrammeID")
      .eq("LecturerID", lecturerId)
      .single();

    if (fetchError || !targetLecturer) {
      return new Response(JSON.stringify({ error: "Lecturer not found" }), {
        status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 3) Check Permissions: Must be an Admin OR updating own profile OR Coordinator of same programme
    const isSelfUpdate = caller.id === targetLecturer.auth_user_id;

    let isAuthorized = isSelfUpdate;

    if (!isAuthorized) {
        // Check Admin
        const { data: callerAdmin } = await supabaseAdmin
          .from("Admin")
          .select("AdminID")
          .eq("auth_user_id", caller.id)
          .maybeSingle();

        if (callerAdmin) isAuthorized = true;
    }

    if (!isAuthorized) {
        // Check Coordinator
        const { data: callerCoord } = await supabaseAdmin
            .from("Lecturer")
            .select("ProgrammeID")
            .eq("auth_user_id", caller.id)
            .eq("LecturerRole", "Course Coordinator")
            .maybeSingle();

        if (callerCoord && callerCoord.ProgrammeID === targetLecturer.ProgrammeID) {
            isAuthorized = true;
        }
    }

    if (!isAuthorized) {
      return new Response(JSON.stringify({ error: "Forbidden: You do not have permission to update this lecturer" }), {
        status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 4) Update Auth User email ONLY if actually changed
    if (email && email !== targetLecturer.Email) {
      const { error: updateAuthError } = await supabaseAdmin.auth.admin.updateUserById(
        targetLecturer.auth_user_id,
        { email: email }
      );
      if (updateAuthError) {
        return new Response(JSON.stringify({ error: `Auth Error: ${updateAuthError.message}` }), {
          status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" }
        });
      }
    }

    // 5) Update Lecturer profile
    const { error: dbError } = await supabaseAdmin
      .from("Lecturer")
      .update({
        LecturerName: lecturerName,
        Position: position,
        LecturerRole: role,
        NormalBTSA: btsa,
        NormalCredit: credit,
        EmploymentType: type,
        DepartmentID: deptId,
        Email: email,
        ProgrammeID: progId
      })
      .eq("LecturerID", lecturerId);

    if (dbError) {
      return new Response(JSON.stringify({ error: `DB Error: ${dbError.message}` }), {
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
