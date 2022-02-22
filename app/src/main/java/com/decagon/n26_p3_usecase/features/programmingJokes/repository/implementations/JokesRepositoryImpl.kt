package com.decagon.n26_p3_usecase.features.programmingJokes.repository.implementations

import com.decagon.n26_p3_usecase.features.programmingJokes.remoteService.JokesApiService
import com.decagon.n26_p3_usecase.features.programmingJokes.repository.contracts.JokesRepository
import com.decagon.n26_p3_usecase.features.programmingJokes.model.JokeModelRaw
import retrofit2.Response
import javax.inject.Inject

class JokesRepositoryImpl  @Inject constructor(private val jokesAPiService: JokesApiService) : JokesRepository {

    override suspend fun getAnyJoke(): Response<JokeModelRaw> = jokesAPiService.getAnyJoke()

    override suspend fun getProgrammingJoke(): Response<JokeModelRaw> = jokesAPiService.getProgrammingJoke()

}

