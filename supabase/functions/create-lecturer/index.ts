import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

Deno.serve(async (req) => {
  // Handle CORS for Android requests
  if (req.method === "OPTIONS") {
    return new Response("ok", {
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
      },
    })
  }

  try {
    const payload = await req.json()
    const { lecturerId, lecturerName, email, password, position, role, btsa, credit, type, deptId, progId } = payload

    const supabaseAdmin = createClient(
      Deno.env.get("SUPABASE_URL") ?? "",
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
    )

    // 1) Verify caller identity
    const authHeader = req.headers.get("Authorization")
    if (!authHeader) {
      return new Response(JSON.stringify({ error: "Unauthorized: missing Authorization header" }), {
        status: 401,
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
      })
    }

    const token = authHeader.replace("Bearer ", "").trim()
    const { data: { user: caller }, error: authError } = await supabaseAdmin.auth.getUser(token)

    if (authError || !caller) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401,
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
      })
    }

    // 2) Verify caller permissions
    // Must be Admin OR Course Coordinator
    const { data: adminCheck } = await supabaseAdmin
      .from("Admin")
      .select("AdminID")
      .eq("auth_user_id", caller.id)
      .maybeSingle()

    let isAuthorized = !!adminCheck

    if (!isAuthorized) {
        const { data: coordinatorCheck } = await supabaseAdmin
            .from("Lecturer")
            .select("ProgrammeID")
            .eq("auth_user_id", caller.id)
            .eq("LecturerRole", "Course Coordinator")
            .maybeSingle()

        if (coordinatorCheck) {
            // Coordinators can only create lecturers in their own programme
            if (coordinatorCheck.ProgrammeID === progId) {
                isAuthorized = true
            } else {
                return new Response(JSON.stringify({ error: "Forbidden: Coordinators can only create lecturers for their own programme" }), {
                    status: 403,
                    headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
                })
            }
        }
    }

    if (!isAuthorized) {
      return new Response(JSON.stringify({ error: "Forbidden: Admin or Coordinator access required" }), {
        status: 403,
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
      })
    }

    // 3) Create the User in Supabase Auth
    const { data: newUser, error: createAuthError } = await supabaseAdmin.auth.admin.createUser({
      email,
      password,
      email_confirm: true,
    })

    if (createAuthError) {
      return new Response(JSON.stringify({ error: createAuthError.message }), {
        status: 400,
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
      })
    }

    // 4) Insert into public."Lecturer"
    const { error: dbError } = await supabaseAdmin
      .from("Lecturer")
      .insert({
        LecturerID: lecturerId,
        LecturerName: lecturerName,
        Email: email,
        Position: position,
        LecturerRole: role,
        NormalBTSA: btsa,
        NormalCredit: credit,
        EmploymentType: type,
        DepartmentID: deptId,
        ProgrammeID: progId,
        auth_user_id: newUser.user.id
      })

    if (dbError) {
      await supabaseAdmin.auth.admin.deleteUser(newUser.user.id) // rollback
      return new Response(JSON.stringify({ error: dbError.message }), {
        status: 400,
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
      })
    }

    return new Response(JSON.stringify({ success: true }), {
      status: 200,
      headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
    })
  } catch (e) {
    return new Response(JSON.stringify({ error: (e as Error).message }), {
      status: 500,
      headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
    })
  }
})
