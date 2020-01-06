package helpers

import CasinoLib.services.Logger

object RequestProcess {
    fun validateContentType(contentType: String?): Boolean {
        Logger.log(service = "Auth", message = "Validating Content-Type header: $contentType")
        if (contentType == null) return false
        if (!contentType.contains("application/json")) return false
        return true
    }

    fun isBodyNull(requestBody: String?) : Boolean {
        Logger.log(service = "Auth", message = "Checking if requestBody $requestBody is null")
        return (requestBody == null)
    }
}