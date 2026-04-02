package com.spa.appointments.core.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    // Decodifica el payload del JWT y retorna el IdEmpresa
    fun getIdEmpresa(token: String): Int {
        return try {
            // El JWT tiene formato: header.payload.signature
            val parts   = token.split(".")
            if (parts.size < 2) return 0

            // El payload está en Base64 — lo decodificamos
            val payload = parts[1]
            val decoded = Base64.decode(
                payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '='),
                Base64.URL_SAFE or Base64.NO_WRAP
            )
            val json = JSONObject(String(decoded))
            json.optInt("IdEmpresa", 0)
        } catch (e: Exception) {
            0
        }
    }

    fun getNombreUsuario(token: String): String {
        return try {
            val parts   = token.split(".")
            if (parts.size < 2) return ""
            val payload = parts[1]
            val decoded = Base64.decode(
                payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '='),
                Base64.URL_SAFE or Base64.NO_WRAP
            )
            val json = JSONObject(String(decoded))
            // ClaimTypes.Name genera esta clave larga en .NET
            json.optString(
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name",
                ""
            )
        } catch (e: Exception) {
            ""
        }
    }
}