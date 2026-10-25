package dev.lelonio.square.data.local

import dev.lelonio.square.data.AppleCatalog
import org.junit.Assert.*
import org.junit.Test

class LocalAlbumMatchingTest {
    private val mama = LocalTrack(7, "", "Mama", "My Chemical Romance", "The Black Parade", year = "2006")
    private val art = AppleCatalog.Album(null, null, motionSquareUrl = "https://catalog/shared")
    private val album = LocalAlbumCandidate("1156310650", "The Black Parade", "My Chemical Romance", 2006, 14, artwork = art)
    @Test fun explicitLocalMotionWorksOnMeteredWifiButOfflineAndAutomaticPoliciesRemain() {
        assertTrue(LocalMotionPolicy.catalogAllowed(false, false, true))
        assertFalse(LocalMotionPolicy.catalogAllowed(true, false, true))
        assertFalse(LocalMotionPolicy.catalogAllowed(false, false, false))
        assertTrue(LocalMotionPolicy.catalogAllowed(false, true, false))
    }
    @Test fun singleOwnedSongFindsAlbumWithoutTrackRelationshipsOrDuration() {
        assertEquals(art, LocalAlbumMatching.motion(mama, listOf(album))?.second)
    }
    @Test fun songTitleIsOptionalAndAlbumArtistCanSupplyIdentity() {
        assertEquals(art, LocalAlbumMatching.motion(mama.copy(title = "", artist = "", albumArtist = "My Chemical Romance"), listOf(album))?.second)
        assertNull(LocalAlbumMatching.motion(mama, listOf(album.copy(title = "Greatest Hits"))))
    }
    @Test fun deluxeRemasteredExpandedAndAnniversaryArePlausibleEditions() {
        for (label in listOf("Deluxe Version", "2016 Remastered", "Expanded Edition", "20th Anniversary Edition"))
            assertNotNull(label, LocalAlbumMatching.score(mama, album.copy(title = "The Black Parade ($label)")))
    }
    @Test fun exactAlbumOutranksApproximateAndEditionNames() {
        val deluxe = album.copy(id = "2", title = "The Black Parade (Deluxe Version)")
        assertEquals(album, LocalAlbumMatching.ranked(mama, listOf(deluxe, album)).first())
        assertNotNull(LocalAlbumMatching.score(mama.copy(album = "The Black Parde"), album))
    }
    @Test fun anotherArtistLiveTributeOrUnrelatedAlbumNeverMatches() {
        assertNull(LocalAlbumMatching.score(mama, album.copy(artist = "Another band")))
        for (title in listOf("The Black Parade (Live)", "The Black Parade - Instrumental", "Another Album"))
            assertNull(LocalAlbumMatching.score(mama, album.copy(title = title)))
    }
    @Test fun albumArtistIsPrimaryForCollaborationsAndNormalizesPunctuation() {
        assertNotNull(LocalAlbumMatching.score(mama.copy(artist = "Guest", albumArtist = "MY CHEMICAL ROMANCE"), album))
        assertNotNull(LocalAlbumMatching.score(mama.copy(album = "The Black-Parade"), album))
    }
    @Test fun sharedMamaSquareWinsOverConflictingPortraitEditions() {
        val first = album.copy(artwork = art.copy(motionUrl = "https://catalog/tall-a"))
        val second = first.copy(id = "209388277", artwork = art.copy(motionUrl = "https://catalog/tall-b"))
        val result = LocalAlbumMatching.motion(mama, listOf(first, second))?.second
        assertEquals(art, result)
    }
    @Test fun verifiedDeluxeCanProvideMotionWhenOriginalHasNone() {
        val none = album.copy(artwork = AppleCatalog.Album(null, null))
        assertEquals(art, LocalAlbumMatching.motion(mama, listOf(none, album.copy(id="2",title="The Black Parade (Deluxe Version)")))?.second)
    }
    @Test fun songMembershipAndYearAreAdditionalEvidenceRatherThanRequirements() {
        val other = album.copy(id="2", year=2007)
        val verified = album.copy(songTitles=listOf("Mama"))
        assertEquals(verified, LocalAlbumMatching.ranked(mama, listOf(other,verified)).first())
    }
    @Test fun contradictoryEquallyBackedEditionsUseStaticFallback() {
        val other = album.copy(id="2", artwork=art.copy(motionSquareUrl="https://other/video"))
        assertNull(LocalAlbumMatching.motion(mama,listOf(album,other))?.second)
        assertNull(LocalAlbumMatching.motion(mama,listOf(album.copy(artist="Other"))))
    }
}
