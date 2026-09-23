package com.redwings.widget.data.api

import retrofit2.http.GET
import retrofit2.http.Path

interface NhlApiService {
    @GET("v1/club-schedule-season/{team}/{season}")
    suspend fun getClubScheduleSeason(
        @Path("team") team: String = "DET",
        @Path("season") season: String
    ): NhlScheduleResponse

    @GET("v1/club-schedule-season/{team}/now")
    suspend fun getClubScheduleNow(
        @Path("team") team: String = "DET"
    ): NhlScheduleResponse

    @GET("v1/scoreboard/{team}/now")
    suspend fun getScoreboardNow(
        @Path("team") team: String = "DET"
    ): NhlScoreboardResponse

    @GET("v1/standings/now")
    suspend fun getStandingsNow(): NhlStandingsResponse
}
