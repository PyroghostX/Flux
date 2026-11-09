package com.mskd.flux.features.show.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.shownBackdropPath
import com.mskd.flux.features.artwork.ui.composables.ArtworkTitle
import com.mskd.flux.features.artwork.ui.composables.GenresTags
import com.mskd.flux.features.people.ui.components.people.PeopleList
import com.mskd.flux.features.show.presentation.ShowIntent
import com.mskd.flux.features.show.ui.composables.SeasonItem
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.LandscapePreview
import com.mskd.flux.ui.tablet.TabletDetailLayout
import com.mskd.flux.ui.tablet.TabletOverview
import com.mskd.flux.ui.tablet.TabletPlayButtons
import com.mskd.flux.ui.tablet.TabletRowPadding
import com.mskd.flux.ui.tablet.TabletRowTitle
import com.mskd.flux.ui.tablet.episodeLabel
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.firstEpisodeToWatch
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.no_summary
import flux.shared.generated.resources.seasons
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.MaterialTheme

/** Show page on a tablet: Play (next episode) top left, the seasons along the bottom. */
@Composable
fun ShowContentLarge(
    fullShow: FullArtwork.FullShow,
    people: List<Person>,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ShowIntent) -> Unit
) {

    val nextEpisode = fullShow.episodes.filter { it.isAvailable }.firstEpisodeToWatch()

    TabletDetailLayout(
        backdropPath = fullShow.artwork.shownBackdropPath.ifBlank { fullShow.artwork.imagePath },
        topPadding = scaffoldInnerPadding.calculateTopPadding(),
        header = {

            ArtworkTitle(
                modifier = Modifier.clickable { sendIntent(ShowIntent.OnArtworkImageClick) },
                artwork = fullShow.artwork,
            )

            GenresTags(genres = fullShow.genres)

            if (nextEpisode != null) {
                TabletPlayButtons(
                    media = nextEpisode,
                    label = nextEpisode.episodeLabel(),
                    onPlay = { sendIntent(ShowIntent.PlayNext) },
                )
            }

            TabletOverview {
                Text.Content.Body(
                    text = fullShow.artwork.description.ifEmpty { stringResource(Res.string.no_summary) },
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

        },
        bottomRow = {

            Column {

                TabletRowTitle(text = stringResource(Res.string.seasons))

                LazyRow(
                    contentPadding = TabletRowPadding,
                    horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
                ) {
                    items(items = fullShow.seasons, key = { it.id }) { season ->
                        SeasonItem(
                            modifier = Modifier.width(SEASON_WIDTH),
                            season = season,
                            episodes = fullShow.episodes.filter { it.season == season.season },
                            onClick = { rgb -> sendIntent(ShowIntent.OnSeasonClick(season = season.season, rgb = rgb)) },
                            onLongPress = { sendIntent(ShowIntent.ShowSeasonPreview(season = season)) }
                        )
                    }
                }

            }

        },
        below = {
            if (people.isNotEmpty()) {
                item { PeopleList(people = people, onClick = { sendIntent(ShowIntent.OnPersonClick(person = it)) }) }
            }
        }
    )

}

private val SEASON_WIDTH = 150.dp

@LandscapePreview
@Composable
fun ShowContentLarge_Preview() {
    FluxTheme {
        ShowContentLarge(
            fullShow = MediaMockups.fullShow,
            people = PeopleMockup.all,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}
