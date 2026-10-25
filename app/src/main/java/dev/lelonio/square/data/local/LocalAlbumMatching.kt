package dev.lelonio.square.data.local

import dev.lelonio.square.data.AppleCatalog
import java.text.Normalizer
import java.util.Locale

data class LocalAlbumCandidate(val id: String, val title: String, val artist: String,
    val year: Int? = null, val trackCount: Int? = null, val songTitles: List<String> = emptyList(),
    val artwork: AppleCatalog.Album? = null)

/** Album identity is independent of how many of its songs happen to be owned locally. */
internal object LocalAlbumMatching {
    private val edition = Regex("(?i)\\s*[\\[(][^\\])]*(deluxe|expanded|remaster|anniversary|edition|version)[^\\])]*[\\])]\\s*")
    private val incompatible = listOf("live", "karaoke", "instrumental", "acappella", "a cappella", "remix", "tribute")
    private fun words(s: String) = Normalizer.normalize(s, Normalizer.Form.NFKD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).replace('&', ' ')
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim().replace(Regex("\\s+"), " ")
    private fun base(s: String) = words(s.replace(edition, " ").replace(Regex("(?i)\\s+-\\s+.*(deluxe|expanded|remaster|anniversary|edition|version).*$"), ""))
    private fun similarity(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        var previous = IntArray(b.length + 1) { it }
        for (i in a.indices) {
            val current = IntArray(b.length + 1); current[0] = i + 1
            for (j in b.indices) current[j + 1] = minOf(current[j] + 1, previous[j + 1] + 1, previous[j] + if (a[i] == b[j]) 0 else 1)
            previous = current
        }
        return 1.0 - previous[b.length].toDouble() / maxOf(a.length, b.length)
    }
    fun score(track: LocalTrack, candidate: LocalAlbumCandidate): Int? {
        if (track.album.isBlank() || candidate.id.isBlank() || words(candidate.artist) != words(track.albumArtist.ifBlank { track.artist })) return null
        if (incompatible.any { Regex("\\b${Regex.escape(it)}\\b").containsMatchIn(words(track.album)) != Regex("\\b${Regex.escape(it)}\\b").containsMatchIn(words(candidate.title)) }) return null
        val a = base(track.album); val b = base(candidate.title)
        val identity = when {
            words(track.album) == words(candidate.title) -> 100
            a.isNotBlank() && a == b -> 90
            a.length >= 8 && b.length >= 8 && similarity(a, b) >= 0.88 -> 75
            else -> return null
        }
        val song = candidate.songTitles.any { words(it) == words(track.title) }
        return identity + (if (song) 6 else 0) + (if (track.year.toIntOrNull() == candidate.year && candidate.year != null) 2 else 0) +
            (if (track.track in 1..(candidate.trackCount ?: 0)) 1 else 0)
    }
    fun ranked(track: LocalTrack, candidates: List<LocalAlbumCandidate>) = candidates.distinctBy { it.id }
        .mapNotNull { c -> score(track, c)?.let { c to it } }.sortedByDescending { it.second }.map { it.first }

    fun motion(track: LocalTrack, candidates: List<LocalAlbumCandidate>): Pair<String, AppleCatalog.Album?>? {
        val ranked = ranked(track, candidates)
        if (ranked.isEmpty()) return null
        val usable = ranked.filter { it.artwork?.let { a -> a.motionUrl != null || a.motionSquareUrl != null } == true }
        if (usable.isEmpty()) return ranked.joinToString(",") { it.id } to null
        val shared = LocalVisualMatching.sharedMotion(usable.mapNotNull { it.artwork })
        if (shared != null) return usable.joinToString(",") { it.id } to shared
        val best = usable.first()
        val tied = usable.filter { score(track, it) == score(track, best) }
        return best.id to if (tied.size == 1) best.artwork else null
    }
}
