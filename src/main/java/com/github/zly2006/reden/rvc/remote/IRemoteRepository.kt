package com.github.zly2006.reden.rvc.remote

interface IRemoteRepository {
    fun deleteRepo()
    val gitUrl: String
}
