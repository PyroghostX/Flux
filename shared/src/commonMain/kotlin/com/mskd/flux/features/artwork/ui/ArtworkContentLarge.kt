package com.mskd.flux.features.artwork.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.shownBackdropPath
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.artwork.ui.composables.ArtworkOverview
import com.mskd.flux.features.artwork.ui.composables.ArtworkTitle
import com.mskd.flux.features.people.ui.components.people.PeopleList
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.LandscapePreview
import com.mskd.flux.ui.media.episode.EpisodeDropDownMenu
import com.mskd.flux.ui.tablet.TabletDetailLayout
import com.mskd.flux.ui.tablet.TabletEpisodeCard
import com.mskd.flux.ui.tablet.TabletOverview
import com.mskd.flux.ui.tablet.TabletPlayButtons
import com.mskd.flux.ui.tablet.TabletRowPadding
import com.mskd.flux.ui.tablet.TabletRowTitle
import com.mskd.flux.ui.tablet.episodeLabel
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episodes
import org.jetbrains.compose.resources.stringResource

/** Movie or season page on a tablet: Play top left, a season's episodes along the bottom. */
@Composable
fun ArtworkContentLarge(
    fullArtwork: FullArtwork,
    people: List<Person>,
    selectedMedia: Media,
    selectedSeason: Int?,
    overview: ArtworkOverviewContent,
    isOverviewSpoiler: Boolean,
    isOverviewBlurred: Boolean,
    expandedEpisodeId: Long?,
    spoilerEpisodeIds: Set<Long>,
    blurredEpisodeIds: Set<Long>,
    isArtworkImageBlurred: Boolean,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    val episodes = (fullArtwork as? FullArtwork.FullShow)
        ?.episodes
        ?.filter { it.season == selectedSeason }
        ?.sortedBy { it.number }
        .orEmpty()

    TabletDetailLayout(
        backdropPath = fullArtwork.artwork.shownBackdropPath.ifBlank { fullArtwork.artwork.imagePath },
        topPadding = scaffoldInnerPadding.calculateTopPadding(),
        header = {

            when (fullArtwork) {
                is FullArtwork.FullMovie -> ArtworkTitle(
                    modifier = Modifier.clickable { sendIntent(ArtworkIntent.OnArtworkImageClick) },
                    artwork = fullArtwork.artwork,
                )
                is FullArtwork.FullShow -> Column {
                    Text.Content.Title(
                        text = fullArtwork.artwork.title,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text.MainTitle(
                        text = fullArtwork.seasons.find { it.season == selectedSeason }?.title ?: fullArtwork.artwork.title,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            TabletPlayButtons(
                media = selectedMedia,
                label = selectedMedia.episodeLabel(),
                onPlay = { sendIntent(ArtworkIntent.PlayMedia(media = selectedMedia)) },
                onToggleWatched = { sendIntent(ArtworkIntent.ChangeWatchStatus(media = selectedMedia)) },
            )

            TabletOverview {
                ArtworkOverview(
                    overview = overview,
                    isSpoiler = isOverviewSpoiler,
                    isBlurred = isOverviewBlurred,
                    onToggleSpoiler = { sendIntent(ArtworkIntent.ToggleOverviewSpoiler) },
                )
            }

        },
        bottomRow = if (episodes.isEmpty()) null else {
            {
                EpisodesRow(
                    episodes = episodes,
                    selectedMedia = selectedMedia,
                    blurredEpisodeIds = blurredEpisodeIds,
                    sendIntent = sendIntent,
                )
            }
        },
        below = {
            if (people.isNotEmpty()) {
                item { PeopleList(people = people, onClick = { sendIntent(ArtworkIntent.OnPersonClick(person = it)) }) }
            }
        }
    )

}

/** The season's episodes in one sideways row, starting at the selected one; tap plays, long press opens the menu. */
@Composable
private fun EpisodesRow(
    episodes: List<Episode>,
    selectedMedia: Media,
    blurredEpisodeIds: Set<Long>,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = episodes.indexOfFirst { it.id == selectedMedia.mediaId }.coerceAtLeast(0)
    )

    Column {

        TabletRowTitle(text = stringResource(Res.string.episodes))

        LazyRow(
            state = listState,
            contentPadding = TabletRowPadding,
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {
            items(items = episodes, key = { it.id }) { episode ->
                TabletEpisodeCard(
                    episode = episode,
                    isSelected = episode.id == selectedMedia.mediaId,
                    isBlurred = episode.id in blurredEpisodeIds,
                    onClick = { sendIntent(ArtworkIntent.PlayMedia(media = episode)) },
                    dropDownMenu = { isVisible, onDismissRequest ->
                        EpisodeDropDownMenu(
                            isVisible = isVisible,
                            episode = episode,
                            onDismissRequest = onDismissRequest,
                            sendIntent = sendIntent
                        )
                    }
                )
            }
        }

    }

}

@LandscapePreview
@Composable
fun ArtworkContentLargeMovie_Preview() {
    FluxTheme {
        ArtworkContentLarge(
            fullArtwork = MediaMockups.fullMovie,
            people = PeopleMockup.all,
            selectedMedia = MediaMockups.movie,
            selectedSeason = null,
            overview = ArtworkOverviewContent.MovieOverview(MediaMockups.fullMovie.movie, MediaMockups.fullMovie.genres),
            isOverviewSpoiler = false,
            isOverviewBlurred = false,
            expandedEpisodeId = null,
            spoilerEpisodeIds = emptySet(),
            blurredEpisodeIds = emptySet(),
            isArtworkImageBlurred = false,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}

@LandscapePreview
@Composable
fun ArtworkContentLargeShow_Preview() {
    FluxTheme {
        ArtworkContentLarge(
            fullArtwork = MediaMockups.fullShow,
            people = PeopleMockup.all,
            selectedMedia = MediaMockups.episode1,
            selectedSeason = 1,
            overview = ArtworkOverviewContent.Summary(MediaMockups.season1.description),
            isOverviewSpoiler = false,
            isOverviewBlurred = false,
            expandedEpisodeId = null,
            spoilerEpisodeIds = emptySet(),
            blurredEpisodeIds = emptySet(),
            isArtworkImageBlurred = false,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}
