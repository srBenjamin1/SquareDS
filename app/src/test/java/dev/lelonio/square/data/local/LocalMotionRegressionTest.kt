package dev.lelonio.square.data.local

import dev.lelonio.square.data.AppleCatalog
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Test

class LocalMotionRegressionTest {
    private val track = LocalTrack(7, "content://audio/7", "Song", "Artist", "Album")
    private val candidate = LocalSongCandidate("11", "Song", "Artist", "Album", 0, "22", "33")
    private val art = AppleCatalog.Album(null, null, motionUrl = "https://catalog/video.m3u8")
    private val now = 1_000_000_000L
    private val evidence = LocalMotionPolicy.evidence(track, "us")
    private fun match(age: Long, artwork: AppleCatalog.Album? = art) =
        LocalMotionMatch(track.uri, evidence, now - age, "33", artwork)

    @Test fun oneSongQueriesTitleArtistAndAlbumWithoutAlbumCompleteness() = runBlocking {
        val queries = mutableListOf<String>()
        val found = LocalMotionPolicy.candidates(track) { queries += it; listOf(candidate) }
        assertEquals(listOf("Artist Song Album"), queries)
        assertEquals("33", LocalVisualMatching.uniqueAlbum(track, found!!))
    }
    @Test fun mamaFromAnIncompleteBlackParadePrefersTheTaggedAlbum() = runBlocking {
        val mama = track.copy(title = "Mama", artist = "My Chemical Romance", album = "The Black Parade")
        val original = LocalSongCandidate("1156310780", mama.title, mama.artist, mama.album, 0, "14748659", "1156310650")
        val deluxe = original.copy(id = "other-song", album = "The Black Parade (Deluxe Version)", albumId = "355446080")
        val found = LocalMotionPolicy.candidates(mama) { listOf(deluxe, original) }
        assertEquals("1156310650", LocalVisualMatching.uniqueAlbum(mama, found!!))
    }
    @Test fun sameTaggedAlbumCanUseMotionWhenAnotherCatalogEditionOmitsIt() {
        val mama = track.copy(title = "Mama", artist = "My Chemical Romance", album = "The Black Parade")
        val original = LocalSongCandidate("1156310780", mama.title, mama.artist, mama.album, 279027, "14748659", "1156310650")
        val alternate = original.copy(id = "209388883", albumId = "209388277", durationMs = 279040)
        assertNull(LocalVisualMatching.sharedMotion(listOf(art, AppleCatalog.Album(null, null))))
        assertEquals(art, LocalMotionPolicy.motion(mama, listOf(original, alternate), listOf(art, AppleCatalog.Album(null, null))))
    }
    @Test fun missingAlbumOrContradictoryVideosStillUseStaticFallback() {
        assertNull(LocalMotionPolicy.motion(track.copy(album = ""), listOf(candidate), listOf(art, AppleCatalog.Album(null, null))))
        assertNull(LocalMotionPolicy.motion(track, listOf(candidate), listOf(art, art.copy(motionUrl = "https://other/video.m3u8"))))
        assertNull(LocalMotionPolicy.motion(track, listOf(candidate), listOf(AppleCatalog.Album(null, null))))
    }
    @Test fun mamaSharedSquareRemainsUsableInLocalPortraitWithoutChoosingAConflictingTallVideo() {
        val a = art.copy(motionUrl = "https://catalog/tall-a", motionSquareUrl = "https://catalog/shared-square")
        val b = a.copy(motionUrl = "https://catalog/tall-b")
        val result = LocalMotionPolicy.motion(track, listOf(candidate), listOf(a, b))
        assertNull(result?.motionUrl)
        assertEquals("https://catalog/shared-square", result?.motionSquareUrl)
        val policy = dev.lelonio.square.ui.player.PlayerVisualPolicy
        val apple = dev.lelonio.square.data.PreferencesStore.CoverSource.APPLE_MUSIC
        assertEquals("https://catalog/shared-square", policy.motionUrl(result, false, true, apple, true, false, localTrack = true))
        assertEquals("https://catalog/shared-square", policy.motionUrl(result, true, true, apple, true, false, localTrack = true))
        assertNull(policy.motionUrl(result, false, true, apple, true, false))
        assertNull(policy.motionUrl(result, false, false, apple, true, false, localTrack = true))
        assertNull(policy.motionUrl(result, false, true, apple, true, true, localTrack = true))
        assertNull(LocalMotionPolicy.motion(track, emptyList(), listOf(a)))
    }
    @Test fun missingAlbumUsesSongIdentityAndNeedsNoDuration() = runBlocking {
        val queries = mutableListOf<String>()
        val found = LocalMotionPolicy.candidates(track.copy(album = "")) { queries += it; listOf(candidate) }
        assertEquals(listOf("Artist Song"), queries)
        assertEquals("33", LocalVisualMatching.uniqueAlbum(track, found!!))
    }
    @Test fun targetedMissFallsBackButExactAlbumOutranksCompilation() = runBlocking {
        var calls = 0
        val compilation = candidate.copy(albumId = "44", album = "Compilation")
        val found = LocalMotionPolicy.candidates(track) {
            if (++calls == 1) listOf(compilation) else listOf(candidate, compilation)
        }
        assertEquals(2, calls)
        assertEquals("33", LocalVisualMatching.uniqueAlbum(track, found!!))
    }
    @Test fun networkFailureIsNotAnEmptyCatalogResult() = runBlocking {
        assertNull(LocalMotionPolicy.candidates(track) { null })
        assertEquals(emptyList<LocalSongCandidate>(), LocalMotionPolicy.candidates(track) { emptyList() })
    }
    @Test fun namesakesAndLiveVersionsCannotProvideMotion() {
        assertNull(LocalVisualMatching.uniqueAlbum(track, listOf(candidate.copy(artist = "Namesake"))))
        assertNull(LocalVisualMatching.uniqueAlbum(track, listOf(candidate.copy(name = "Song (Live)"))))
        assertNull(LocalVisualMatching.sharedMotion(listOf(art, art.copy(motionUrl = "https://other/video.m3u8"))))
    }
    @Test fun positiveMotionUrlsAreRefreshedInsteadOfCachedForever() {
        assertTrue(LocalMotionPolicy.reusable(match(60_000), evidence, now))
        assertFalse(LocalMotionPolicy.reusable(match(6 * 60 * 60_000L), evidence, now))
    }
    @Test fun negativeMatchesRetryAfterFifteenMinutes() {
        assertTrue(LocalMotionPolicy.reusable(match(60_000, null), evidence, now))
        assertFalse(LocalMotionPolicy.reusable(match(15 * 60_000, null), evidence, now))
    }
    @Test fun emptySearchCacheCannotProlongANegativeMotionMatchForADay() {
        assertTrue(LocalMotionPolicy.candidatesReusable(now - 60_000, emptyList(), now))
        assertFalse(LocalMotionPolicy.candidatesReusable(now - 15 * 60_000, emptyList(), now))
        assertTrue(LocalMotionPolicy.candidatesReusable(now - 60_000, listOf(candidate), now))
        assertFalse(LocalMotionPolicy.candidatesReusable(now - 6 * 60 * 60_000L, listOf(candidate), now))
    }
    @Test fun oldCacheStorefrontChangesAndRetaggingInvalidateOnlyMotion() {
        val old = match(0).copy(evidence = LocalVisualMatching.evidence(listOf(track)))
        assertFalse(LocalMotionPolicy.reusable(old, evidence, now))
        assertFalse(LocalMotionPolicy.reusable(match(0), LocalMotionPolicy.evidence(track, "es"), now))
        assertFalse(LocalMotionPolicy.reusable(match(0), LocalMotionPolicy.evidence(track.copy(album = "Other"), "us"), now))
        assertFalse(LocalMotionPolicy.reusable(match(-1), evidence, now))
    }
    @Test fun emptyPreferredVideoRowDoesNotHideUsableTallVideo() {
        val attributes = Json.parseToJsonElement("""{"editorialVideo":{
            "motionDetailTall":{"previewFrame":{}},
            "motionTallVideo3x4":{"video":"https://catalog/tall.m3u8"},
            "motionDetailSquare":{"video":"https://catalog/square.m3u8"}}}""").jsonObject
        val result = AppleCatalog.localMotionFromAttributes(attributes)
        assertEquals("https://catalog/tall.m3u8", result.motionUrl)
        assertEquals("https://catalog/square.m3u8", result.motionSquareUrl)
        assertNull(result.coverUrl)
    }
    @Test fun invalidMotionUrlsKeepTheStaticCoverFallback() {
        val attributes = Json.parseToJsonElement("""{"editorialVideo":{
            "motionDetailTall":{"video":"http://unsafe/video"},
            "motionDetailSquare":{"video":""}}}""").jsonObject
        val result = AppleCatalog.localMotionFromAttributes(attributes)
        assertNull(result.motionUrl)
        assertNull(result.motionSquareUrl)
    }
}
