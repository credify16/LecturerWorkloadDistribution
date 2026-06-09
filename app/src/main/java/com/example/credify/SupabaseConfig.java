package com.example.credify;

import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.SupabaseClientBuilder;
import io.github.jan.supabase.auth.Auth;
import io.github.jan.supabase.functions.Functions;
import io.github.jan.supabase.postgrest.Postgrest;
import io.github.jan.supabase.storage.Storage;
import kotlin.Unit;

public class SupabaseConfig {

    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    private static SupabaseClient client;

    public static synchronized SupabaseClient getClient() {
        if (client == null) {

            SupabaseClientBuilder builder =
                    new SupabaseClientBuilder(SUPABASE_URL, SUPABASE_KEY);

            builder.install(Auth.Companion, config -> Unit.INSTANCE);
            builder.install(Postgrest.Companion, config -> Unit.INSTANCE);
            builder.install(Storage.Companion, config -> Unit.INSTANCE);
            builder.install(Functions.Companion, config -> Unit.INSTANCE);

            client = builder.build();
        }

        return client;
    }
}