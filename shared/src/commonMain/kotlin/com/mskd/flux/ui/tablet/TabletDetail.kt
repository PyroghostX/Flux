package com.mskd.flux.ui.tablet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.ui.blurContent
import com.mskd.flux.ui.components.ProgressStatusBar
import com.mskd.flux.ui.components.ProgressStatusChip
import com.mskd.flux.ui.grayScale
import com.mskd.flux.ui.image.FluxImage
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.content_unavailable
import flux.shared.generated.resources.ic_play
import flux.shared.generated.resources.ic_replay
import flux.shared.generated.resources.mark_as_not_watched
import flux.shared.generated.resources.mark_as_watched
import flux.shared.generated.resources.play
import flux.shared.generated.resources.remaining_time
import flux.shared.generated.resources.resume
import flux.shared.generated.resources.rewatch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/*
 * Large-screen detail pages laid out like Plex for a landscape car tablet: the backdrop fills the
 * screen, the title, a big Play button and the summary sit top left, and the seasons or episodes run
 * along the bottom as one row you swipe sideways. Nothing needs a scroll to reach Play.
 */

private val SIDE_PADDING = 32.dp
private val PLAY_BUTTON_HEIGHT = 72.dp

/**
 * The page: [header] top left over the backdrop, [bottomRow] along the bottom of the first screen,
 * and [below] (cast...) further down for whoever scrolls.
 */
@Composable
fun TabletDetailLayout(
    backdropPath: String,
    topPadding: Dp,
    header: @Composable ColumnScope.() -> Unit,
    bottomRow: (@Composable () -> Unit)? = null,
    below: LazyListScope.() -> Unit = {},
) {

    val background = MaterialTheme.colorScheme.background

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val screenHeight = maxHeight

        FluxImage(
            modifier = Modifier
                .fillMaxSize()
                .alpha(.85f),
            path = backdropPath,
            contentDescription = "backdrop"
        )

        // Darken from the left (behind the text) and from the bottom (behind the row)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to background.copy(alpha = .95f),
                        .45f to background.copy(alpha = .75f),
                        .75f to background.copy(alpha = .15f),
                        1f to Color.Transparent,
                    )
                )
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        .45f to Color.Transparent,
                        1f to background,
                    )
                )
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            item {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight)
                ) {

                    Spacer(modifier = Modifier.height(topPadding))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(.55f)
                            .padding(start = SIDE_PADDING, end = FluxUI.Space.medium),
                        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
                        content = header
                    )

                    if (bottomRow != null) {
                        Spacer(modifier = Modifier.height(FluxUI.Space.medium))
                        bottomRow()
                    }

                    Spacer(modifier = Modifier.height(FluxUI.Space.large))
                }

            }

            below()

            item { Spacer(modifier = Modifier.height(FluxUI.Space.bottomScreen)) }

        }

    }

}

/** The summary, scrolling on its own inside whatever room the header leaves it. */
@Composable
fun ColumnScope.TabletOverview(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState())
    ) {
        content()
    }
}

/** Big Play / Resume / Rewatch with the time left above it and a smaller watched toggle beside it. */
@Composable
fun TabletPlayButtons(
    media: Media,
    label: String? = null,
    onPlay: () -> Unit,
    onToggleWatched: (() -> Unit)? = null,
) {

    val text = when (media.status) {
        Status.WATCHED -> stringResource(Res.string.rewatch)
        Status.IS_WATCHING -> stringResource(Res.string.resume)
        else -> stringResource(if (media.isAvailable) Res.string.play else Res.string.content_unavailable)
    }

    Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)) {

        if (media.status == Status.IS_WATCHING) {
            Row(
                modifier = Modifier.width(320.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
            ) {
                ProgressStatusBar(
                    modifier = Modifier.weight(1f),
                    isVisible = true,
                    progress = { media.progressPercent }
                )
                Text.Card.Label(
                    text = stringResource(
                        Res.string.remaining_time,
                        (media.duration.minToMs - media.currentTime).timeDescription(withoutSeconds = true)
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {

            Button(
                modifier = Modifier
                    .height(PLAY_BUTTON_HEIGHT)
                    .widthIn(min = 240.dp),
                onClick = onPlay,
                enabled = media.isAvailable,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) {
                if (media.isAvailable) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(if (media.status == Status.WATCHED) Res.drawable.ic_replay else Res.drawable.ic_play),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(FluxUI.Space.medium))
                }
                androidx.compose.material3.Text(
                    text = listOfNotNull(text, label).joinToString("  "),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            if (onToggleWatched != null) {
                OutlinedButton(
                    modifier = Modifier.height(56.dp),
                    onClick = onToggleWatched,
                    shape = CircleShape
                ) {
                    androidx.compose.material3.Text(
                        text = stringResource(if (media.status == Status.WATCHED) Res.string.mark_as_not_watched else Res.string.mark_as_watched),
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
            }

        }

    }

}

/** "S1 · E3" for an Episode, nothing for a Movie. */
fun Media.episodeLabel(): String? = (this as? Episode)?.let { "S${it.season} · E${it.number}" }

/** One episode in the bottom row: its still, number and title, progress; tap plays it. */
@Composable
fun TabletEpisodeCard(
    episode: Episode,
    isSelected: Boolean,
    isBlurred: Boolean,
    onClick: () -> Unit,
    dropDownMenu: @Composable ((isVisible: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null,
) {

    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.width(280.dp),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(FluxUI.Ratio.landscape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = dropDownMenu != null }
                ),
            shape = FluxUI.shapes.corners,
            border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
            shadowElevation = FluxUI.Elevation.itemShadow
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                FluxImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .blurContent(blurred = isBlurred)
                        .let { if (episode.isAvailable) it else it.grayScale() },
                    media = episode,
                    contentDescription = episode.title
                )

                ProgressStatusChip(isWatched = episode.status == Status.WATCHED)

                ProgressStatusBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    isVisible = episode.status == Status.IS_WATCHING,
                    progress = { episode.progressPercent }
                )

            }

            dropDownMenu?.invoke(showMenu) { showMenu = false }

        }

        Text.Card.Title(
            modifier = Modifier.fillMaxWidth(),
            text = "${episode.number}. ${episode.title}",
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }

}

/** Title of a bottom row, lined up with the header text. */
@Composable
fun TabletRowTitle(text: String) {
    Text.Content.Title(
        modifier = Modifier
            .padding(start = SIDE_PADDING, bottom = FluxUI.Space.small)
            .heightIn(min = 24.dp),
        text = text,
        color = MaterialTheme.colorScheme.onBackground
    )
}

/** Side padding of the bottom rows, matching the header. */
val TabletRowPadding = PaddingValues(horizontal = SIDE_PADDING)
