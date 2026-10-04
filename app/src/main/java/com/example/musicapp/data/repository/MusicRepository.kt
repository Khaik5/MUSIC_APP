package com.example.musicapp.data.repository

import com.example.musicapp.R
import com.example.musicapp.data.model.FeaturedAlbum
import com.example.musicapp.data.model.Music
object MusicRepository {

    private val songs = listOf(
        Music(
            id = "90s_album",
            title = "90s Album",
            artist = "Sơn Tùng MTP",
            meta = "23k",
            imageRes = R.drawable.art_mtp,
            audioRes = R.raw.noi_nay_co_anh,
            durationMs = 176_000,
            album = "90s Collection"
        ),
        Music(
            id = "brooklyn",
            title = "Brooklyn",
            artist = "Miyagi & Andy Panda feat. TumaniYO",
            meta = "2:34",
            imageRes = R.drawable.art_brooklyn,
            audioRes = R.raw.noi_nay_co_anh,
            durationMs = 154_000,
            album = "Brooklyn"
        ),
        Music(
            id = "can_you_feel_my_heart",
            title = "Can You Feel My Heart",
            artist = "Bring Me The Horizon",
            meta = "2:34",
            imageRes = R.drawable.art_can_you_feel,
            audioRes = R.raw.noi_nay_co_anh,
            durationMs = 154_000,
            album = "Sempiternal"
        ),
        Music(
            id = "cancer",
            title = "Cancer",
            artist = "twenty one pilots",
            meta = "643k",
            imageRes = R.drawable.art_cancer,
            audioRes = R.raw.noi_nay_co_anh,
            durationMs = 206_000,
            album = "Cancer"
        ),
        Music(
            id = "waste_my_time",
            title = "Waste My Time",
            artist = "Oliver Tree",
            meta = "2:34",
            imageRes = R.drawable.art_waste_my_time,
            audioRes = R.raw.noi_nay_co_anh,
            durationMs = 154_000,
            album = "Waste My Time"
        ),
        Music(
            id = "blinding_lights",
            title = "Blinding Lights",
            artist = "The Weeknd",
            meta = "2:56",
            imageRes = R.drawable.art_blinding_square,
            audioRes = R.raw.noi_nay_co_anh,
            durationMs = 176_000,
            album = "After Hours"
        )
    )

    private val featuredAlbums = listOf(
        FeaturedAlbum(
            id = "igor",
            title = "IGOR Alternative",
            artist = "Tyler, The Creator",
            imageRes = R.drawable.art_igor
        ),
        FeaturedAlbum(
            id = "starboy",
            title = "Starboy",
            artist = "The Weeknd",
            imageRes = R.drawable.art_starboy
        ),
        FeaturedAlbum(
            id = "anti",
            title = "Anti",
            artist = "Rihanna",
            imageRes = R.drawable.art_anti
        )
    )

    fun getFeaturedAlbums(): List<FeaturedAlbum> = featuredAlbums

    fun getTrendingSongs(): List<Music> = songs

    fun getSong(id: String): Music? = songs.firstOrNull { it.id == id }

    fun search(query: String): List<Music> {
        val normalized = query.trim()
        if (normalized.isBlank()) return songs.take(5)
        return songs.filter { song ->
            song.title.contains(normalized, ignoreCase = true) ||
                song.artist.contains(normalized, ignoreCase = true) ||
                song.album.contains(normalized, ignoreCase = true)
        }
    }

    fun getQueue(): List<Music> = songs
}
