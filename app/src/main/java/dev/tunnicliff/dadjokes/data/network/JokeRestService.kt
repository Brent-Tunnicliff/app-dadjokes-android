// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.network

import dev.tunnicliff.network.RestService

interface JokeRestService {
    suspend fun getRandomJoke(): JokeDTO
    suspend fun getJokes(page: Int, limit: Int): JokePageDTO
}

class JokeRestServiceImpl(
    private val restService: RestService
) : JokeRestService {
    companion object {
        const val BASE_URL = "https://icanhazdadjoke.com"
    }

    override suspend fun getRandomJoke(): JokeDTO =
        restService.get(path = "", ofType = JokeDTO::class)

    override suspend fun getJokes(page: Int, limit: Int): JokePageDTO =
        restService.get(
            path = "search",
            parameters = mapOf(
                "page" to page.toString(),
                "limit" to limit.toString()
            ),
            ofType = JokePageDTO::class
        )
}