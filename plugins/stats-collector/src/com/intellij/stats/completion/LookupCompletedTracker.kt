/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.stats.completion

import com.intellij.codeInsight.lookup.LookupAdapter
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.stats.personalization.UserFactorDescriptions
import com.intellij.stats.personalization.UserFactorStorage
import com.intellij.stats.personalization.UserFactorsManager

/**
 * @author Vitaliy.Bibaev
 */
class LookupCompletedTracker : LookupAdapter() {
    override fun lookupCanceled(event: LookupEvent?) {
        val lookup = event?.lookup as? LookupImpl ?: return
        val element = lookup.currentItem ?: return
        if (isSelectedByTyping(lookup, element)) {
            processTypedSelect(lookup, element)
        }
    }

    override fun itemSelected(event: LookupEvent?) {
        val lookup = event?.lookup as? LookupImpl ?: return
        val element = event.item ?: return
        processExplicitSelect(lookup, element)
    }

    private fun isSelectedByTyping(lookup: LookupImpl, element: LookupElement): Boolean =
            element.lookupString == lookup.itemPattern(element)

    private fun processElementSelected(lookup: LookupImpl, element: LookupElement) {
        val relevanceObjects =
                lookup.getRelevanceObjects(listOf(element), false)
        val relevanceMap = relevanceObjects[element]!!.associate { it.first to it.second }
        val userFactorsManager = UserFactorsManager.getInstance(lookup.project)
        relevanceMap.forEach { name, value -> userFactorsManager.getFeatureFactor(name)?.update(value) }
    }

    private fun processExplicitSelect(lookup: LookupImpl, element: LookupElement) {
        processElementSelected(lookup, element)

        UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.COMPLETION_FINISH_TYPE) { updater ->
            updater.fireExplicitCompletionPerformed()
        }

        val prefixLength = lookup.getPrefixLength(element)
        UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.PREFIX_LENGTH_ON_COMPLETION) { updater ->
            updater.fireCompletionPerformed(prefixLength)
        }

        val itemPosition = lookup.selectedIndex
        if (itemPosition != -1) {
            UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.SELECTED_ITEM_POSITION) { updater ->
                updater.fireCompletionPerformed(itemPosition)
            }
        }

        if (prefixLength > 1) {
            val pattern = lookup.itemPattern(element)
            val isMmemonicsUsed = !element.lookupString.startsWith(pattern)
            UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.MNEMONICS_USAGE) { updater ->
                updater.fireCompletionFinished(isMmemonicsUsed)
            }
        }
    }

    private fun processTypedSelect(lookup: LookupImpl, element: LookupElement) {
        processElementSelected(lookup, element)

        UserFactorStorage.applyOnBoth(lookup.project, UserFactorDescriptions.COMPLETION_FINISH_TYPE) { updater ->
            updater.fireTypedSelectPerformed()
        }
    }
}
