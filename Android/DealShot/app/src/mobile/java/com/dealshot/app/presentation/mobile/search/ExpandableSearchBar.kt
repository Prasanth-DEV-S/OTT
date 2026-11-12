package com.dealshot.app.presentation.mobile.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.TextMuted

/**
 * Expandable search bar that smoothly expands across the toolbar
 * when opened, featuring back navigation, text query input, clear button,
 * and responsive touch feedback.
 *
 * NOTE: when [isExpanded] is true the caller must give this composable
 * the full available toolbar width (e.g. Modifier.weight(1f) or fillMaxWidth()).
 */
@Composable
fun ExpandableSearchBar(
    isExpanded: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onClearQuery: () -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Local field state: keeps cursor/IME composition/paste stable and guarantees
    // typed characters render immediately, even if the parent updates `query` late.
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }

    // Sync external changes (e.g. ViewModel reset or tag selection) into the local state.
    LaunchedEffect(query) {
        if (query != fieldValue.text) {
            fieldValue = TextFieldValue(query, TextRange(query.length))
        }
    }

    // Single close routine: clear focus, hide keyboard, then notify the parent.
    val closeSearch = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onCloseSearch()
    }

    // System back button / gesture closes the search bar the same way as the arrow.
    BackHandler(enabled = isExpanded) {
        closeSearch()
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            // Wait one frame so the BasicTextField is attached before requesting focus.
            withFrameNanos { }
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {
            }
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    Box(
        modifier = modifier
            .then(if (isExpanded) Modifier.fillMaxWidth() else Modifier)
            .animateContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (!isExpanded) {
            // Collapsed: Standard header icon button
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search videos",
                    tint = GoldPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        } else {
            // Expanded: Full width search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(CardBgDark.copy(alpha = 0.95f))
                    .border(
                        width = 1.2.dp,
                        color = GoldPrimary.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(23.dp)
                    )
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back / Collapse button
                IconButton(
                    onClick = { closeSearch() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close search",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Search Icon prefix
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Query Text Input
                BasicTextField(
                    value = fieldValue,
                    onValueChange = { newValue ->
                        fieldValue = newValue
                        if (newValue.text != query) {
                            onQueryChange(newValue.text)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 80.dp)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(GoldPrimary),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            val text = fieldValue.text
                            if (text.isNotBlank()) {
                                onSearchSubmit(text)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (fieldValue.text.isEmpty()) {
                                Text(
                                    text = "Search videos, genres...",
                                    color = TextMuted.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Clear button (appears smoothly when query is not empty)
                AnimatedVisibility(
                    visible = fieldValue.text.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C251C))
                            .clickable {
                                fieldValue = TextFieldValue("")
                                onClearQuery()
                                try {
                                    focusRequester.requestFocus()
                                } catch (_: Exception) {
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = GoldTextLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}