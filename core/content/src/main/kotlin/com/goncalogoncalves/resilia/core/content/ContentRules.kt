package com.goncalogoncalves.resilia.core.content

import com.goncalogoncalves.resilia.core.domain.model.ActivityOrigin
import com.goncalogoncalves.resilia.core.domain.model.AlertLevel
import com.goncalogoncalves.resilia.core.domain.model.AlertRegion
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.KitQuantityKind
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Review
import com.goncalogoncalves.resilia.core.domain.model.ReviewStatus

/**
 * Semantic rules that a JSON Schema cannot express (cross-file references,
 * official-source requirement, review metadata). Run in CI and in unit tests.
 */
object ContentRules {

    private val ID_PATTERN = Regex("^[a-z0-9]+(?:[-.][a-z0-9]+)*$")
    private val DATE_PATTERN = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    private val NUMBER_PATTERN = Regex("^\\+?[0-9]+(?: [0-9]+)*$")

    fun check(bundle: ContentBundle): List<String> {
        val errors = mutableListOf<String>()
        fun err(where: String, msg: String) = errors.add("$where: $msg")

        fun checkText(where: String, text: LocalizedText?) {
            text ?: return
            text.missingLanguages().forEach { err(where, "missing or blank text for '${it.tag}'") }
        }

        fun checkReview(where: String, review: Review) {
            if (review.status == ReviewStatus.REVIEWED) {
                if (review.reviewedAt.isNullOrBlank()) err(where, "reviewed but 'reviewedAt' is empty")
                if (review.reviewedBy.isNullOrBlank()) err(where, "reviewed but 'reviewedBy' is empty")
            }
            review.reviewedAt?.let {
                if (!DATE_PATTERN.matches(it)) err(where, "reviewedAt '$it' is not YYYY-MM-DD")
            }
        }

        // --- Sources
        val sourcesById = bundle.sources.associateBy { it.id }
        duplicates(bundle.sources.map { it.id }).forEach { err("sources", "duplicate id '$it'") }
        bundle.sources.forEach { s ->
            val where = "source '${s.id}'"
            if (!ID_PATTERN.matches(s.id)) err(where, "invalid id")
            checkText("$where.title", s.title)
            if (!s.url.startsWith("https://")) err(where, "url must use https")
            if (!DATE_PATTERN.matches(s.accessedAt)) err(where, "accessedAt is not YYYY-MM-DD")
            s.publishedAt?.let { if (!DATE_PATTERN.matches(it)) err(where, "publishedAt is not YYYY-MM-DD") }
        }

        // --- Contacts: never a number without a source
        duplicates(bundle.contacts.map { it.id }).forEach { err("contacts", "duplicate id '$it'") }
        bundle.contacts.forEach { c ->
            val where = "contact '${c.id}'"
            if (!ID_PATTERN.matches(c.id)) err(where, "invalid id")
            if (!NUMBER_PATTERN.matches(c.number)) err(where, "number '${c.number}' must be digits, spaces or a leading +")
            checkText("$where.name", c.name)
            checkText("$where.purpose", c.purpose)
            checkText("$where.hours", c.hours)
            checkText("$where.cost", c.cost)
            checkReview(where, c.review)
            if (c.sourceIds.isEmpty()) err(where, "every contact needs at least one source")
            c.sourceIds.filterNot(sourcesById::containsKey).forEach { err(where, "unknown sourceId '$it'") }
        }

        // --- Kit: every item and every number needs an official source
        fun checkOfficial(where: String, ids: List<String>) {
            ids.filterNot(sourcesById::containsKey).forEach { err(where, "unknown sourceId '$it'") }
            if (ids.none { sourcesById[it]?.entity?.isOfficial == true }) {
                err(where, "needs at least one official source")
            }
        }
        bundle.kit?.let { kit ->
            checkOfficial("kit.minimumDays", kit.minimumDaysSourceIds)
            checkOfficial("kit.reviewIntervalMonths", kit.reviewSourceIds)
            if (kit.minimumDays !in kit.dayOptions) err("kit.dayOptions", "must include minimumDays (${kit.minimumDays})")
            kit.dayOptions.filter { it < kit.minimumDays }
                .forEach { err("kit.dayOptions", "$it is below the official minimum of ${kit.minimumDays}") }
            duplicates(kit.items.map { it.id }).forEach { err("kit", "duplicate id '$it'") }
            kit.items.forEach { item ->
                val where = "kit item '${item.id}'"
                if (!ID_PATTERN.matches(item.id)) err(where, "invalid id")
                val prefix = item.list.name.lowercase() + "."
                if (!item.id.startsWith(prefix)) err(where, "id must start with '$prefix'")
                checkText("$where.name", item.name)
                checkText("$where.detail", item.detail)
                checkText("$where.note", item.note)
                checkReview(where, item.review)
                checkOfficial(where, item.sourceIds)
                item.quantity?.let { q ->
                    when (q.kind) {
                        KitQuantityKind.PER_PERSON_PER_DAY ->
                            if ((q.amount ?: 0.0) <= 0.0 || q.unit == null) {
                                err(where, "perPersonPerDay needs a positive amount and a unit")
                            }
                        KitQuantityKind.FOR_DURATION ->
                            if (q.amount != null || q.unit != null) err(where, "forDuration must not have an amount")
                    }
                }
            }
        }
        val kitItemIds = bundle.kit?.items?.map { it.id }?.toSet()

        // --- Family plan: every hint (advice) needs an official source
        bundle.plan?.let { plan ->
            duplicates(plan.sections.map { it.id }).forEach { err("plan", "duplicate section '$it'") }
            plan.sections.forEach { sec ->
                val where = "plan section '${sec.id}'"
                if (!ID_PATTERN.matches(sec.id)) err(where, "invalid id")
                checkText("$where.hint", sec.hint)
                checkReview(where, sec.review)
                if (sec.hint != null) checkOfficial(where, sec.sourceIds)
                else sec.sourceIds.filterNot(sourcesById::containsKey).forEach { err(where, "unknown sourceId '$it'") }
            }
        }

        // --- Short guides (battery, emotional support): same step rules as scenarios
        fun checkGuide(g: com.goncalogoncalves.resilia.core.domain.model.ToolGuide) {
            val where = "guide '${g.id}'"
            if (!ID_PATTERN.matches(g.id)) err(where, "invalid id")
            checkText("$where.title", g.title)
            checkText("$where.summary", g.summary)
            checkReview(where, g.review)
            g.sourceIds.filterNot(sourcesById::containsKey).forEach { err(where, "unknown sourceId '$it'") }
            duplicates(g.steps.map { it.id }).forEach { err(where, "duplicate step id '$it'") }
            g.steps.forEach { step ->
                val sw = "step '${step.id}'"
                if (!ID_PATTERN.matches(step.id)) err(sw, "invalid id")
                if (!step.id.startsWith("${g.id}.")) err(sw, "id must start with '${g.id}.'")
                checkText("$sw.text", step.text)
                checkText("$sw.detail", step.detail)
                checkText("$sw.note", step.note)
                checkReview(sw, step.review)
                step.sourceIds.filterNot(sourcesById::containsKey).forEach { err(sw, "unknown sourceId '$it'") }
                if (step.kind.requiresOfficialSource &&
                    step.sourceIds.none { sourcesById[it]?.entity?.isOfficial == true }
                ) {
                    err(sw, "kind '${step.kind.name.lowercase()}' requires at least one official source")
                }
            }
        }
        bundle.battery?.let(::checkGuide)

        // --- Emotional support: health advice, so every step needs an official source
        bundle.support?.let { sup ->
            checkText("support.note", sup.note)
            duplicates(sup.guides.map { it.id }).forEach { err("support", "duplicate guide '$it'") }
            sup.guides.forEach { g ->
                checkGuide(g)
                g.steps.filterNot { it.kind.requiresOfficialSource }
                    .forEach { err("step '${it.id}'", "support steps must be kind 'health'") }
            }
            val contactIds = bundle.contacts.map { it.id }.toSet()
            sup.contactIds.filterNot(contactIds::contains).forEach { err("support", "unknown contactId '$it'") }
        }

        // --- Activities: declared origin, known materials, no advice without an official source
        val materialIds = bundle.materials.map { it.id }.toSet()
        duplicates(bundle.materials.map { it.id }).forEach { err("materials", "duplicate id '$it'") }
        bundle.materials.forEach { m ->
            if (!ID_PATTERN.matches(m.id)) err("material '${m.id}'", "invalid id")
            checkText("material '${m.id}'.name", m.name)
        }
        duplicates(bundle.activities.map { it.id }).forEach { err("activities", "duplicate id '$it'") }
        bundle.activities.forEach { a ->
            val where = "activity '${a.id}'"
            if (!ID_PATTERN.matches(a.id)) err(where, "invalid id")
            checkText("$where.title", a.title)
            checkText("$where.summary", a.summary)
            checkText("$where.variants", a.variants)
            checkText("$where.originNote", a.originNote)
            a.instructions.forEachIndexed { i, t -> checkText("$where.instructions[$i]", t) }
            checkReview(where, a.review)
            a.ageMax?.let { if (it < a.ageMin) err(where, "ageMax is below ageMin") }
            a.peopleMax?.let { if (it < a.peopleMin) err(where, "peopleMax is below peopleMin") }
            a.materials.filterNot(materialIds::contains).forEach { err(where, "unknown material '$it'") }
            a.sourceIds.filterNot(sourcesById::containsKey).forEach { err(where, "unknown sourceId '$it'") }
            when (a.origin) {
                ActivityOrigin.PUBLIC_DOMAIN ->
                    if (a.originNote == null) err(where, "public-domain activities need an originNote naming the work")
                ActivityOrigin.GUIDANCE ->
                    if (a.sourceIds.none { sourcesById[it]?.entity?.isOfficial == true }) {
                        err(where, "activities based on guidance need at least one official source")
                    }
                else -> Unit
            }
        }

        // --- Scenarios
        val scenarioIds = bundle.scenarios.map { it.id }.toSet()
        duplicates(bundle.scenarios.map { it.id }).forEach { err("scenarios", "duplicate id '$it'") }
        val allStepIds = bundle.scenarios.flatMap { sc -> sc.phases.all.map { it.id } }
        duplicates(allStepIds).forEach { err("steps", "duplicate step id '$it'") }

        bundle.scenarios.forEach { sc ->
            val where = "scenario '${sc.id}'"
            if (!ID_PATTERN.matches(sc.id)) err(where, "invalid id")
            checkText("$where.title", sc.title)
            checkText("$where.summary", sc.summary)
            checkText("$where.typicalDuration", sc.typicalDuration)
            checkReview(where, sc.review)
            ContentLanguage.entries.forEach { lang ->
                if (sc.keywords[lang.tag].isNullOrEmpty()) err(where, "no keywords for '${lang.tag}'")
            }
            sc.sourceIds.filterNot(sourcesById::containsKey)
                .forEach { err(where, "unknown sourceId '$it'") }
            sc.relatedScenarioIds.filterNot(scenarioIds::contains)
                .forEach { err(where, "unknown relatedScenarioId '$it'") }

            val ownStepIds = sc.phases.all.map { it.id }.toSet()
            sc.emergencyNow.filterNot(ownStepIds::contains)
                .forEach { err(where, "emergencyNow references unknown step '$it'") }

            sc.phases.all.forEach { step ->
                val sw = "step '${step.id}'"
                if (!ID_PATTERN.matches(step.id)) err(sw, "invalid id")
                if (!step.id.startsWith("${sc.id}.")) err(sw, "id must start with '${sc.id}.'")
                checkText("$sw.text", step.text)
                checkText("$sw.detail", step.detail)
                checkText("$sw.note", step.note)
                checkReview(sw, step.review)
                step.sourceIds.filterNot(sourcesById::containsKey)
                    .forEach { err(sw, "unknown sourceId '$it'") }
                if (kitItemIds != null) {
                    step.relatedKitItemIds.filterNot(kitItemIds::contains)
                        .forEach { err(sw, "unknown relatedKitItemId '$it'") }
                }
                if (step.kind.requiresOfficialSource) {
                    val hasOfficial = step.sourceIds.any { sourcesById[it]?.entity?.isOfficial == true }
                    if (!hasOfficial) {
                        err(sw, "kind '${step.kind.name.lowercase()}' requires at least one official source")
                    }
                }
            }
        }
        // --- Alerts (IPMA): known areas, labels for every level, triggers that point at real types
        val alerts = bundle.alerts
        if (alerts != null) {
            val w = "alerts"
            if (alerts.sourceIds.none { sourcesById[it]?.entity?.isOfficial == true }) err(w, "needs an official source")
            alerts.sourceIds.filterNot(sourcesById::containsKey).forEach { err(w, "unknown sourceId '$it'") }
            duplicates(alerts.areas.map { it.id }).forEach { err(w, "duplicate area '$it'") }
            alerts.areas.forEach { checkText("$w.area '${it.id}'", it.name) }
            val areasById = alerts.areas.associateBy { it.id }
            duplicates(alerts.municipalities.map { it.dico }).forEach { err(w, "duplicate municipality '$it'") }
            alerts.municipalities.forEach { m ->
                val area = areasById[m.areaId]
                if (area == null) err("$w.municipality '${m.dico}'", "unknown areaId '${m.areaId}'")
                else if (area.region != AlertRegion.CONTINENT) err("$w.municipality '${m.dico}'", "fire risk is mainland only")
            }
            duplicates(alerts.warningTypes.map { it.id }).forEach { err(w, "duplicate warning type '$it'") }
            alerts.warningTypes.forEach { checkText("$w.type '${it.id}'", it.name) }
            AlertLevel.entries.filter { it != AlertLevel.UNKNOWN }.forEach { level ->
                val info = alerts.level(level)
                if (info == null) err(w, "no label for level '${level.name.lowercase()}'")
                else {
                    checkText("$w.level '${level.name.lowercase()}'.name", info.name)
                    checkText("$w.level '${level.name.lowercase()}'.meaning", info.meaning)
                }
            }
            (1..5).forEach { if (alerts.fireRisk(it) == null) err(w, "no label for fire risk $it") }
            alerts.fireRiskLevels.forEach { checkText("$w.fireRisk ${it.level}", it.name) }
            checkReview(w, alerts.review)
        }
        val typeIds = alerts?.warningTypes?.map { it.id }?.toSet().orEmpty()
        bundle.scenarios.forEach { sc ->
            sc.alertTriggers.forEach { trigger ->
                val where = "scenario '${sc.id}'"
                val (kind, value) = trigger.split(":", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                when (kind) {
                    "ipma" -> if (value !in typeIds) err(where, "alertTrigger '$trigger': unknown IPMA warning type")
                    "rcm" -> if (value.toIntOrNull() !in 1..5) err(where, "alertTrigger '$trigger': fire risk must be 1-5")
                    else -> err(where, "alertTrigger '$trigger': unknown kind '$kind'")
                }
            }
        }
        // --- SNS hospitals: official source, unique ids, dates
        bundle.hospitals?.let { h ->
            val w = "hospitals"
            if (h.sourceIds.none { sourcesById[it]?.entity?.isOfficial == true }) err(w, "needs an official source")
            h.sourceIds.filterNot(sourcesById::containsKey).forEach { err(w, "unknown sourceId '$it'") }
            if (!DATE_PATTERN.matches(h.dataModified)) err(w, "dataModified is not YYYY-MM-DD")
            if (!DATE_PATTERN.matches(h.retrievedAt)) err(w, "retrievedAt is not YYYY-MM-DD")
            duplicates(h.hospitals.map { it.id }).forEach { err(w, "duplicate hospital '$it'") }
            h.hospitals.forEach { if (!ID_PATTERN.matches(it.id)) err("$w '${it.id}'", "invalid id") }
            checkReview(w, h.review)
        }
        duplicates(bundle.scenarios.flatMap { it.alertTriggers }).forEach {
            err("scenarios", "alertTrigger '$it' is used by more than one scenario")
        }
        return errors
    }

    private fun duplicates(ids: List<String>): Set<String> =
        ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
}
