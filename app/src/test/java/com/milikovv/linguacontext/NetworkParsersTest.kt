package com.milikovv.linguacontext

import com.milikovv.linguacontext.data.remote.DictService
import com.milikovv.linguacontext.data.remote.OllamaService
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateRequest
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateResponse
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.test.Test

import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals


class NetworkParsersTest {
    @Test
    fun `Dict response parsing`() = runTest {
        val mockedWebServer = MockWebServer()
        val mockedResponse = MockResponse().setResponseCode(200).setBody(
            """
              [
                {
                  "word": "hello",
                  "phonetic": "həˈləʊ",
                  "phonetics": [
                    {
                      "text": "həˈləʊ",
                      "audio": "//ssl.gstatic.com/dictionary/static/sounds/20200429/hello--_gb_1.mp3"
                    },
                    {
                      "text": "hɛˈləʊ"
                    }
                  ],
                  "origin": "early 19th century: variant of earlier hollo ; related to holla.",
                  "meanings": [
                    {
                      "partOfSpeech": "exclamation",
                      "definitions": [
                        {
                          "definition": "used as a greeting or to begin a phone conversation.",
                          "example": "hello there, Katie!",
                          "synonyms": [],
                          "antonyms": []
                        }
                      ]
                    },
                    {
                      "partOfSpeech": "noun",
                      "definitions": [
                        {
                          "definition": "an utterance of ‘hello’; a greeting.",
                          "example": "she was getting polite nods and hellos from people",
                          "synonyms": [],
                          "antonyms": []
                        }
                      ]
                    },
                    {
                      "partOfSpeech": "verb",
                      "definitions": [
                        {
                          "definition": "say or shout ‘hello’.",
                          "example": "I pressed the phone button and helloed",
                          "synonyms": [],
                          "antonyms": []
                        }
                      ]
                    }
                  ]
                }
              ]
            """.trimIndent()
        )
        mockedWebServer.enqueue(mockedResponse)
        mockedWebServer.start()
        mockedWebServer.url("/")

        val retrofit = createRetrofitInstance(mockedWebServer.url("/").toString())
        val testApi = retrofit.create(DictService::class.java)

        val actualResponse = testApi.getEnWordData("Hello")
        assertEquals("hello", actualResponse.first().word)
        assertEquals("həˈləʊ", actualResponse.first().phonetic)

        mockedWebServer.shutdown()
    }

    @Test
    fun `Ollama response parsing`() = runTest {
        val testData = "The sky appears blue due to a phenomenon called Rayleigh scattering."

        val mockedWebServer = MockWebServer()
        val mockedResponse = MockResponse().setResponseCode(200).setBody(
            """
                {
                  "model": "llama3.1",
                  "created_at": "2024-06-27T12:00:00Z",
                  "response": "$testData",
                  "done": true,
                  "total_duration": 1234567890,
                  "load_duration": 123456789,
                  "prompt_eval_count": 10,
                  "prompt_eval_duration": 12345678,
                  "eval_count": 50,
                  "eval_duration": 123456789
                }
            """.trimIndent()
        )
        mockedWebServer.enqueue(mockedResponse)
        mockedWebServer.start()
        mockedWebServer.url("/")

        val retrofit = createRetrofitInstance(mockedWebServer.url("/").toString())
        val testApi = retrofit.create(OllamaService::class.java)

        val actualResponse: OllamaGenerateResponse = testApi.generate(OllamaGenerateRequest(
            model = "qwen3:32b",
            prompt = "Some prompt text"
        ))
        assertEquals(testData, actualResponse.response)

        mockedWebServer.shutdown()
    }

    private fun createRetrofitInstance(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .client(OkHttpClient.Builder()
                .connectTimeout(0, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .callTimeout(0, TimeUnit.MILLISECONDS)
                .build())
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
