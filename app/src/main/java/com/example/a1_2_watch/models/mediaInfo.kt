package com.example.a1_2_watch.models


data class Movie(
    val id: Int,
    val title: String?,
    val overview: String,
    val poster_path: String?,
    val vote_average: Double,
    val runtime : Int
)

data class Show(
    val id: Int,
    val name: String?,
    val overview: String,
    val poster_path: String?,
    val vote_average: Double,
    val episode_run_time : List<Int>
)

data class Anime(
    val id: Int,
    val attributes: Attributes
)

