package eu.kanade.presentation.category.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.core.preference.asToggleableState
import eu.kanade.presentation.category.buildCategoryGroups
import tachiyomi.core.common.preference.CheckboxState
import tachiyomi.domain.category.model.Category
import tachiyomi.presentation.core.components.material.padding

internal fun CheckboxState<Category>.hasActiveSelection(): Boolean = when (this) {
    is CheckboxState.State.Checked -> true
    is CheckboxState.State.None -> false
    is CheckboxState.TriState.Include -> true
    is CheckboxState.TriState.Exclude -> true
    is CheckboxState.TriState.None -> false
}

@Composable
fun CategoryTreeSelectionList(
    categories: List<Category>,
    selectionById: Map<Long, CheckboxState<Category>>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = remember(categories) { buildCategoryGroups(categories) }
    val expandedState = remember(categories, selectionById) {
        mutableStateMapOf<String, Boolean>().apply {
            groups.filter { !it.isTopLevel }.forEach { group ->
                val shouldExpand = group.children.any { child ->
                    selectionById[child.category.id]?.hasActiveSelection() == true
                }
                if (shouldExpand) {
                    put(group.parentName, true)
                }
            }
        }
    }

    Column(modifier = modifier) {
        groups.forEach { group ->
            if (group.isTopLevel) {
                val child = group.children.first()
                val checkbox = selectionById[child.category.id]
                if (checkbox != null) {
                    CategoryTreeCheckboxRow(
                        label = child.childName,
                        checkbox = checkbox,
                        indented = false,
                        onToggle = { onToggle(child.category.id) },
                    )
                }
            } else {
                val isExpanded = expandedState[group.parentName] ?: false
                val highlighted = group.children.any { child ->
                    selectionById[child.category.id]?.hasActiveSelection() == true
                }

                CategoryTreeGroupHeader(
                    parentName = group.parentName,
                    childCount = group.children.size,
                    totalItemCount = null,
                    isExpanded = isExpanded,
                    highlighted = highlighted,
                    onClick = {
                        expandedState[group.parentName] = !isExpanded
                    },
                )

                if (isExpanded) {
                    group.children.forEach { child ->
                        val checkbox = selectionById[child.category.id]
                        if (checkbox != null) {
                            CategoryTreeCheckboxRow(
                                label = child.childName,
                                checkbox = checkbox,
                                indented = true,
                                onToggle = { onToggle(child.category.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTreeCheckboxRow(
    label: String,
    checkbox: CheckboxState<Category>,
    indented: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indented) 28.dp else 0.dp)
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (checkbox) {
            is CheckboxState.TriState -> {
                TriStateCheckbox(
                    state = checkbox.asToggleableState(),
                    onClick = onToggle,
                )
            }
            is CheckboxState.State -> {
                Checkbox(
                    checked = checkbox.isChecked,
                    onCheckedChange = { onToggle() },
                )
            }
        }

        Text(
            text = label,
            modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
        )
    }
}
