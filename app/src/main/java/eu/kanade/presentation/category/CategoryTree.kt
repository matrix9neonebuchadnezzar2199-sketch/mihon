package eu.kanade.presentation.category

import tachiyomi.domain.category.model.Category

data class CategoryGroup(
    val parentName: String,
    val children: List<CategoryChild>,
    val isTopLevel: Boolean,
)

data class CategoryChild(
    val category: Category,
    val childName: String,
    val originalIndex: Int,
)

/**
 * カテゴリ名の最初の `/` で親子グループを組み立てる（2 段階まで）。
 * 同一親が 2 件以上のときのみネストグループ化する。
 */
fun buildCategoryGroups(categories: List<Category>): List<CategoryGroup> {
    val groups = mutableListOf<CategoryGroup>()
    val groupMap = mutableMapOf<String, MutableList<CategoryChild>>()

    categories.forEachIndexed { index, category ->
        val name = category.name
        val slashIndex = name.indexOf('/')

        if (slashIndex > 0 && slashIndex < name.length - 1) {
            val parentName = name.substring(0, slashIndex).trim()
            val childName = name.substring(slashIndex + 1).trim()
            if (parentName.isNotEmpty() && childName.isNotEmpty()) {
                groupMap.getOrPut(parentName) { mutableListOf() }
                    .add(CategoryChild(category, childName, index))
            }
        }
    }

    val usedGroups = mutableSetOf<String>()

    categories.forEachIndexed { index, category ->
        val name = category.name
        val slashIndex = name.indexOf('/')

        if (slashIndex > 0 && slashIndex < name.length - 1) {
            val parentName = name.substring(0, slashIndex).trim()
            if (parentName.isNotEmpty() && parentName !in usedGroups) {
                val children = groupMap[parentName]
                if (children != null && children.size > 1) {
                    groups.add(
                        CategoryGroup(
                            parentName = parentName,
                            children = children,
                            isTopLevel = false,
                        ),
                    )
                } else if (children != null) {
                    groups.add(
                        CategoryGroup(
                            parentName = children[0].category.name,
                            children = children,
                            isTopLevel = true,
                        ),
                    )
                }
                usedGroups.add(parentName)
            }
        } else {
            val alreadyAdded = groups.any { g ->
                g.isTopLevel && g.children.any { it.originalIndex == index }
            }
            if (!alreadyAdded) {
                groups.add(
                    CategoryGroup(
                        parentName = name,
                        children = listOf(
                            CategoryChild(category, name, index),
                        ),
                        isTopLevel = true,
                    ),
                )
            }
        }
    }

    return groups
}
