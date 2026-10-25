package dev.lelonio.square.data.local

/** Motion URLs can expire independently of the track's tags or artist association. */
internal object LocalMotionPolicy {
    fun catalogAllowed(offline: Boolean, unmetered: Boolean, explicitMotion: Boolean): Boolean =
        !offline && (unmetered || explicitMotion)

    private const val POSITIVE_MS = 6 * 60 * 60_000L
    private const val NEGATIVE_MS = 15 * 60_000L

    fun evidence(track: LocalTrack, storefront: String) =
        localKey("motion-album-v4", "$storefront/${LocalVisualMatching.evidence(listOf(track))}")

    fun reusable(match: LocalMotionMatch, evidence: String, now: Long): Boolean {
        return match.evidence == evidence && fresh(match.checkedAt,
            match.artwork?.let { it.motionUrl != null || it.motionSquareUrl != null } == true, now)
    }

    fun candidatesReusable(checkedAt: Long, candidates: List<LocalSongCandidate>, now: Long): Boolean =
        fresh(checkedAt, candidates.isNotEmpty(), now)

    private fun fresh(checkedAt: Long, positive: Boolean, now: Long): Boolean {
        val age = now - checkedAt
        return age >= 0 && age < if (positive) POSITIVE_MS else NEGATIVE_MS
    }

    /** Editions of the exact tagged album may omit video; omission is not conflicting artwork. */
    fun motion(track: LocalTrack, candidates: List<LocalSongCandidate>, albums: List<dev.lelonio.square.data.AppleCatalog.Album>): dev.lelonio.square.data.AppleCatalog.Album? {
        val ranked = LocalVisualMatching.ranked(track, candidates)
        if (ranked.isEmpty()) return null
        val exactAlbum = track.album.isNotBlank() && ranked.isNotEmpty() && ranked.all {
            normalizedLocal(it.album) == normalizedLocal(track.album)
        }
        val usable = if (exactAlbum) albums.filter { it.motionUrl != null || it.motionSquareUrl != null } else albums
        // Conflicting usable resources still require a shared URL; never pick a random edition.
        return LocalVisualMatching.sharedMotion(usable)
    }

    /** Search the tagged album first; a single owned song supplies all the evidence needed. */
    suspend fun candidates(track: LocalTrack, search: suspend (String) -> List<LocalSongCandidate>?): List<LocalSongCandidate>? {
        if (track.title.isBlank() || track.artist.isBlank()) return emptyList()
        val tagged = if (track.album.isNotBlank()) {
            search("${track.artist} ${track.title} ${track.album}") ?: return null
        } else emptyList()
        if (tagged.any { LocalVisualMatching.song(track, it) &&
                normalizedLocal(track.album) == normalizedLocal(it.album) }) return tagged
        val general = search("${track.artist} ${track.title}") ?: return null
        return (tagged + general).distinct()
    }
}
