@file:Suppress("MagicNumber", "TooGenericExceptionCaught", "LoopWithTooManyJumpStatements")

package xyz.luan.bpb.wordnik

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

internal class WordnikClient(
    private val apiKey: String? = System.getenv("WORDNIK_API_KEY"),
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
) {
  fun define(word: String): LookupResult {
    val key = apiKey?.trim().orEmpty()
    if (key.isEmpty()) {
      return LookupResult.Error("WORDNIK_API_KEY is not configured.")
    }

    val normalizedWord = word.trim().lowercase()
    val encoded = URLEncoder.encode(normalizedWord, StandardCharsets.UTF_8)
    val uri =
        URI.create(
            "https://api.wordnik.com/v4/word.json/$encoded/definitions" +
                "?limit=1&includeRelated=false&useCanonical=true&includeTags=false&api_key=$key"
        )

    val request = HttpRequest.newBuilder(uri).GET().build()

    return try {
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
      when (response.statusCode()) {
        200 -> {
          val definition = extractFirstDefinition(response.body())
          if (definition == null) LookupResult.Error("No definition found for '$normalizedWord'.")
          else LookupResult.Success(definition)
        }
        401,
        403 ->
            LookupResult.Error(
                "Invalid or unauthorized WORDNIK_API_KEY (HTTP ${response.statusCode()})."
            )
        else -> {
          val bodySnippet = response.body().replace("\n", " ").take(180)
          LookupResult.Error("Wordnik HTTP ${response.statusCode()}: $bodySnippet")
        }
      }
    } catch (t: Throwable) {
      LookupResult.Error("Wordnik request failed: ${t.message ?: t::class.simpleName}")
    }
  }

  private fun extractFirstDefinition(body: String): String? {
    val match = TEXT_REGEX.find(body) ?: return null
    return jsonUnescape(match.groupValues[1]).trim().ifEmpty { null }
  }

  private fun jsonUnescape(input: String): String {
    val sb = StringBuilder(input.length)
    var i = 0
    while (i < input.length) {
      val ch = input[i]
      if (ch != '\\') {
        sb.append(ch)
        i++
        continue
      }
      if (i + 1 >= input.length) break
      when (val next = input[i + 1]) {
        '\\' -> sb.append('\\')
        '"' -> sb.append('"')
        '/' -> sb.append('/')
        'b' -> sb.append('\b')
        'f' -> sb.append('\u000C')
        'n' -> sb.append('\n')
        'r' -> sb.append('\r')
        't' -> sb.append('\t')
        'u' -> {
          if (i + 5 < input.length) {
            val hex = input.substring(i + 2, i + 6)
            val code = hex.toIntOrNull(16)
            if (code != null) {
              sb.append(code.toChar())
              i += 4
            }
          }
        }
        else -> sb.append(next)
      }
      i += 2
    }
    return sb.toString()
  }

  internal sealed interface LookupResult {
    data class Success(val definition: String) : LookupResult

    data class Error(val message: String) : LookupResult
  }

  private companion object {
    val TEXT_REGEX = Regex("\\\"text\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\\\"])*)\\\"")
  }
}
