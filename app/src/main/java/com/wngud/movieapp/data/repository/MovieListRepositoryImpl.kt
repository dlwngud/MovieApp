package com.wngud.movieapp.data.repository

import com.wngud.movieapp.data.local.movie.MovieDatabase
import com.wngud.movieapp.data.mapper.toMovie
import com.wngud.movieapp.data.mapper.toMovieEntity
import com.wngud.movieapp.data.remote.MovieApi
import com.wngud.movieapp.domain.model.Movie
import com.wngud.movieapp.domain.repository.MovieListRepository
import com.wngud.movieapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MovieListRepositoryImpl @Inject constructor(
    private val movieApi: MovieApi,
    private val movieDatabase: MovieDatabase
) : MovieListRepository {

    override suspend fun getMovieList(
        forceFetchFromRemote: Boolean,
        category: String,
        page: Int
    ): Flow<Resource<List<Movie>>> {
        return flow {
            emit(Resource.Loading(true))
            val localMovieList = movieDatabase.movieDao.getMovieByCategory(category)
            val shouldLoadLocalMovie = localMovieList.isNotEmpty() && !forceFetchFromRemote

            if (shouldLoadLocalMovie) {
                emit(Resource.Success(
                    data = localMovieList.map { movieEntity ->
                        movieEntity.toMovie(category)
                    }
                ))
                emit(Resource.Loading(false))
                return@flow
            }

            val movieListFromApi = try {
                movieApi.getMovieList(category, page)
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading movie"))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading movie"))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading movie"))
                return@flow
            }

            val movieEntities = movieListFromApi.results.let {
                it.map { movieDto -> movieDto.toMovieEntity(category) }
            }

            movieDatabase.movieDao.upsertMovieList(movieEntities)
            emit(Resource.Success(movieEntities.map { movieEntity -> movieEntity.toMovie(category) }))
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getMovie(id: Int): Flow<Resource<Movie>> {
        return flow {
            emit(Resource.Loading(true))

            val movieEntity = movieDatabase.movieDao.getMovieById(id)

            if(movieEntity != null){
                emit(Resource.Success(movieEntity.toMovie(movieEntity.category)))
                emit(Resource.Loading(false))
                return@flow
            }

            emit(Resource.Error("Error no such movie"))
            emit(Resource.Loading(false))
        }
    }
}