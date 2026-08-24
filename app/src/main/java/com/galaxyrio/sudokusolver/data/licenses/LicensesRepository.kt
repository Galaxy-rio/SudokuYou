package com.galaxyrio.sudokusolver.data.licenses

import android.content.Context
import com.galaxyrio.sudokusolver.R
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

data class LibraryLicense(
    val id: String,
    val name: String,
    val artifactId: String,
    val version: String?,
    val developers: List<String>,
    val website: String?,
    val licenseName: String?,
    val licenseUrl: String?,
    val description: String?,
)

class LicensesRepository(context: Context) {
    private val applicationContext = context.applicationContext

    suspend fun getLibraries(): List<LibraryLicense> = withContext(Dispatchers.IO) {
        val libraries = Libs.Builder()
            .withJson(applicationContext, R.raw.aboutlibraries)
            .build()
            .libraries

        libraries.map { library ->
            val license = library.licenses.firstOrNull()
            val website = library.website.toWebUrlOrNull()
                ?: library.scm?.url.toWebUrlOrNull()
            LibraryLicense(
                id = library.uniqueId,
                name = library.name,
                artifactId = library.artifactId,
                version = library.artifactVersion,
                developers = library.developers.mapNotNull { it.name },
                website = website,
                licenseName = license?.name,
                licenseUrl = license?.url.toWebUrlOrNull()
                    ?: license?.spdxId.toSpdxLicenseUrlOrNull(),
                description = library.description,
            )
        }.sortedBy { it.name.lowercase() }
    }
}

private fun String?.toWebUrlOrNull(): String? {
    val value = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val uri = runCatching { URI(value) }.getOrNull() ?: return null
    val isWebScheme = uri.scheme.equals("https", ignoreCase = true) ||
        uri.scheme.equals("http", ignoreCase = true)
    return value.takeIf { isWebScheme && !uri.host.isNullOrBlank() }
}

private fun String?.toSpdxLicenseUrlOrNull(): String? {
    val id = this?.trim()?.takeIf(SPDX_ID_PATTERN::matches) ?: return null
    return "https://spdx.org/licenses/$id.html"
}

private val SPDX_ID_PATTERN = Regex("[A-Za-z0-9][A-Za-z0-9.+-]*")
