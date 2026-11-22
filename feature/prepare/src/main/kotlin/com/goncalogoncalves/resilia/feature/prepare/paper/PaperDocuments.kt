package com.goncalogoncalves.resilia.feature.prepare.paper

import android.content.Context
import android.graphics.RectF
import androidx.core.graphics.withTranslation
import com.goncalogoncalves.resilia.core.designsystem.R as DsR
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.Household
import com.goncalogoncalves.resilia.core.domain.model.KitCalculator
import com.goncalogoncalves.resilia.core.domain.model.KitLine
import com.goncalogoncalves.resilia.core.domain.model.KitList
import com.goncalogoncalves.resilia.core.domain.model.KitQuantityKind
import com.goncalogoncalves.resilia.core.domain.model.Member
import com.goncalogoncalves.resilia.core.domain.model.Phase
import com.goncalogoncalves.resilia.core.domain.model.PlanContact
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.of
import com.goncalogoncalves.resilia.feature.prepare.R
import com.goncalogoncalves.resilia.feature.prepare.formatAmount
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.goncalogoncalves.resilia.core.designsystem.component.labelRes
import com.goncalogoncalves.resilia.core.domain.model.Place
import java.time.format.FormatStyle

/** Everything the paper documents print. Built from the stores at the moment of printing. */
internal data class PaperData(
    val bundle: ContentBundle,
    val language: ContentLanguage,
    val plan: FamilyPlan,
    val members: List<Member>,
    val contacts: List<PlanContact>,
    val household: Household,
    val today: LocalDate,
    val places: List<Place> = emptyList(),
)

internal enum class PaperKind(val fileName: String) {
    A4("resilia-plano.pdf"),
    WALLET("resilia-cartao.pdf"),
    BOOKLET("resilia-guias.pdf"),
}

internal object PaperDocuments {

    fun write(context: Context, kind: PaperKind, data: PaperData, out: File) {
        val footer = context.getString(R.string.paper_footer, data.today.display())
        val w = PdfWriter(context, footer)
        when (kind) {
            PaperKind.A4 -> a4(context, w, data)
            PaperKind.WALLET -> wallet(context, w, data)
            PaperKind.BOOKLET -> booklet(context, w, data)
        }
        w.writeTo(out)
    }

    private fun LocalDate.display() = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

    private fun Source.label(context: Context): String = publisher ?: when (entity.name) {
        "GOV" -> context.getString(DsR.string.ds_entity_gov)
        "EU" -> context.getString(DsR.string.ds_entity_eu)
        "CVP" -> context.getString(DsR.string.ds_entity_cvp)
        "SNS24" -> "SNS 24"
        "UTILITY", "OTHER" -> context.getString(DsR.string.ds_entity_other)
        else -> entity.name
    }

    private fun sourcesLine(context: Context, data: PaperData, ids: List<String>): String {
        val names = ids.mapNotNull(data.bundle::source).map { it.label(context) }.distinct()
        return if (names.isEmpty()) "" else context.getString(R.string.paper_source, names.joinToString(", "))
    }

    // --- A4 card: the whole plan + the kit list
    private fun a4(c: Context, w: PdfWriter, d: PaperData) {
        val p = d.plan
        w.newPage()
        w.text(c.getString(R.string.paper_a4_title), size = 22f, isBold = true, after = 2f)
        w.text(c.getString(R.string.paper_generated, d.today.display()), size = 9f, after = 10f)
        w.boxed(c.getString(R.string.paper_call_112))

        w.heading(c.getString(R.string.plan_members))
        if (d.members.isEmpty()) w.field(c.getString(R.string.plan_field_name), "")
        d.members.forEach { m ->
            w.text(listOf(m.name, m.relation, m.phone).filter { it.isNotBlank() }.joinToString(" · "), isBold = true, after = 2f)
            if (m.notes.isNotBlank()) w.text(m.notes, indent = 12f, after = 2f)
            if (m.medicalOnPaper && !m.medical.isEmpty) {
                val med = m.medical
                listOf(
                    R.string.plan_medical_blood to med.bloodType,
                    R.string.plan_medical_allergies to med.allergies,
                    R.string.plan_medical_medication to med.medication,
                    R.string.plan_medical_conditions to med.conditions,
                    R.string.plan_medical_doctor to med.doctor,
                ).filter { it.second.isNotBlank() }.forEach { (label, value) ->
                    w.text("${c.getString(label)}: $value", size = 9.5f, indent = 12f, after = 1f)
                }
            }
            w.space(4f)
        }

        w.heading(c.getString(R.string.plan_meeting))
        w.field(c.getString(R.string.plan_meeting_near), p.meetingNear)
        w.field(c.getString(R.string.plan_meeting_far), p.meetingFar)

        w.heading(c.getString(R.string.plan_contact_person))
        w.field(c.getString(R.string.plan_field_name), p.contactName)
        w.field(c.getString(R.string.plan_field_phone), p.contactPhone)
        w.field(c.getString(R.string.plan_field_address), p.contactAddress)

        w.heading(c.getString(R.string.plan_roles))
        w.field(c.getString(R.string.plan_roles_helps), p.helpsOthers)
        w.field(c.getString(R.string.plan_roles_kit), p.bringsKit)
        w.field(c.getString(R.string.plan_roles_calls), p.callsEmergency)

        w.heading(c.getString(R.string.plan_exits))
        w.field(c.getString(R.string.plan_exits_main), p.exitMain)
        w.field(c.getString(R.string.plan_exits_alt), p.exitAlternative)

        w.heading(c.getString(R.string.plan_shutoff))
        w.field(c.getString(R.string.plan_shutoff_power), p.shutoffPower)
        w.field(c.getString(R.string.plan_shutoff_water), p.shutoffWater)
        w.field(c.getString(R.string.plan_shutoff_gas), p.shutoffGas)

        w.heading(c.getString(R.string.plan_shelter))
        w.field(c.getString(R.string.plan_shelter_field), p.shelter)

        if (d.places.isNotEmpty()) {
            w.heading(c.getString(R.string.paper_places))
            d.places.forEach { pl ->
                w.text("${c.getString(pl.type.labelRes())}: ${pl.name}", isBold = true, after = 1f)
                val line = listOfNotNull(pl.address.takeIf { it.isNotBlank() }, pl.position?.format()).joinToString(" · ")
                if (line.isNotEmpty()) w.text(line, size = 9f, indent = 12f, after = 4f)
            }
        }

        w.heading(c.getString(R.string.plan_contacts))
        d.bundle.contacts.forEach { ct ->
            w.text("${ct.name.resolve(d.language)}: ${ct.number}", isBold = true, after = 1f)
            w.text(ct.purpose.resolve(d.language) + " · " + sourcesLine(c, d, ct.sourceIds), size = 9f, indent = 12f, after = 4f)
        }
        d.contacts.forEach { ct -> w.text("${ct.name}: ${ct.phone}", isBold = true) }

        kitSection(c, w, d)

        w.space(8f)
        w.text(c.getString(DsR.string.ds_disclaimer_footer), size = 9f)
    }

    private fun kitLineText(c: Context, line: KitLine, d: PaperData): String {
        val name = line.item.name.resolve(d.language)
        val q = line.item.quantity ?: return name
        return when (q.kind) {
            KitQuantityKind.PER_PERSON_PER_DAY -> c.getString(
                R.string.paper_kit_litres, name, formatAmount(line.total ?: 0.0), formatAmount(q.amount ?: 0.0),
            )
            KitQuantityKind.FOR_DURATION -> c.getString(
                R.string.paper_kit_duration, name,
                c.resources.getQuantityString(R.plurals.prepare_people, line.people, line.people),
                c.resources.getQuantityString(R.plurals.prepare_days, line.days, line.days),
            )
        }
    }

    private fun kitSection(c: Context, w: PdfWriter, d: PaperData) {
        val kit = d.bundle.kit ?: return
        KitList.entries.forEach { list ->
            w.heading(c.getString(if (list == KitList.HOME) R.string.prepare_list_home else R.string.prepare_list_bag))
            KitCalculator.calculate(kit, d.household, list).forEach { line ->
                w.text("[  ]  " + kitLineText(c, line, d), after = 1f)
                w.text(sourcesLine(c, d, line.item.sourceIds), size = 8.5f, indent = 18f, after = 3f)
            }
        }
    }

    // --- Wallet card: front and back side by side on A4, to cut out and fold
    private fun wallet(c: Context, w: PdfWriter, d: PaperData) {
        val canvas = w.newPage().canvas
        w.text(c.getString(R.string.paper_wallet_title), size = 16f, isBold = true, after = 2f)
        w.text(c.getString(R.string.paper_wallet_instructions), size = 9.5f)

        val cw = PdfWriter.CARD_WIDTH
        val ch = PdfWriter.CARD_HEIGHT
        val left = (PdfWriter.PAGE_WIDTH - 2 * cw) / 2f
        val top = w.y + 16f
        val front = RectF(left, top, left + cw, top + ch)
        val back = RectF(left + cw, top, left + 2 * cw, top + ch)

        canvas.drawRect(RectF(front.left, front.top, back.right, back.bottom), PdfWriter.cutPaint)
        canvas.drawLine(front.right, front.top, front.right, front.bottom, PdfWriter.foldPaint)

        fun block(r: RectF, lines: List<Pair<String, Boolean>>, firstSize: Float) {
            var y = r.top + 8f
            val width = (r.width() - 16f).toInt()
            lines.forEachIndexed { i, (txt, isBold) ->
                val size = if (i == 0) firstSize else 9f
                val l = w.layout(txt, w.paint(size, isBold), width, maxLines = 2)
                if (y + l.height > r.bottom - 6f) return
                canvas.withTranslation(r.left + 8f, y) { l.draw(this) }
                y += l.height + 5f
            }
        }

        val p = d.plan
        val blank = "____________________"
        block(
            front,
            listOf(
                c.getString(R.string.paper_call_112) to true,
                c.getString(R.string.paper_wallet_contact, p.contactName.ifBlank { blank }, p.contactPhone.ifBlank { "" }).trim() to true,
                c.getString(R.string.paper_wallet_meeting_near, p.meetingNear.ifBlank { blank }) to false,
                c.getString(R.string.paper_wallet_meeting_far, p.meetingFar.ifBlank { blank }) to false,
            ),
            firstSize = 13f,
        )
        val chosen = d.contacts.filter { it.onWalletCard }.take(PlanContact.WALLET_MAX)
        val contactLines = chosen.map { "${it.name}: ${it.phone}" to false } +
            List(PlanContact.WALLET_MAX - chosen.size) { "$blank  $blank" to false }
        block(
            back,
            listOf(c.getString(R.string.paper_wallet_contacts) to true) + contactLines +
                listOf(c.getString(R.string.paper_wallet_disclaimer) to false),
            firstSize = 10f,
        )
    }

    // --- Guide booklet: every guide, every step with its source
    private fun booklet(c: Context, w: PdfWriter, d: PaperData) {
        w.newPage()
        w.text(c.getString(R.string.paper_booklet_title), size = 22f, isBold = true, after = 2f)
        w.text(c.getString(R.string.paper_generated, d.today.display()), size = 9f, after = 10f)
        w.boxed(c.getString(R.string.paper_call_112))
        w.text(c.getString(DsR.string.ds_disclaimer_footer), size = 9.5f, after = 10f)
        w.heading(c.getString(R.string.paper_booklet_contents))
        d.bundle.scenarios.forEach { w.text("·  " + it.title.resolve(d.language), size = 12f) }

        val phaseLabel = mapOf(
            Phase.BEFORE to R.string.paper_phase_before,
            Phase.DURING to R.string.paper_phase_during,
            Phase.AFTER to R.string.paper_phase_after,
        )
        d.bundle.scenarios.forEach { sc ->
            w.newPage()
            w.text(sc.title.resolve(d.language), size = 20f, isBold = true, after = 4f)
            w.text(sc.summary.resolve(d.language), after = 8f)
            Phase.entries.forEach { phase ->
                val steps = sc.phases.of(phase)
                if (steps.isEmpty()) return@forEach
                w.heading(c.getString(phaseLabel.getValue(phase)))
                steps.forEachIndexed { i, step ->
                    w.text("${i + 1}. ${step.text.resolve(d.language)}", isBold = true, after = 1f)
                    step.detail?.let { w.text(it.resolve(d.language), size = 10f, indent = 14f, after = 1f) }
                    step.note?.let { w.text(it.resolve(d.language), size = 9f, indent = 14f, after = 1f) }
                    val meta = sourcesLine(c, d, step.sourceIds).ifBlank { c.getString(R.string.paper_no_source) }
                    w.text(meta, size = 8.5f, indent = 14f, after = 5f)
                }
            }
        }

        w.newPage()
        w.heading(c.getString(R.string.paper_sources_title))
        val used = d.bundle.scenarios.flatMap { sc -> sc.sourceIds + sc.phases.all.flatMap { it.sourceIds } }.distinct()
        used.mapNotNull(d.bundle::source).forEach { s ->
            w.text("${s.label(c)}: ${s.title.resolve(d.language)}", isBold = true, size = 9.5f, after = 1f)
            val dates = listOfNotNull(
                s.publishedAt?.let { c.getString(R.string.paper_source_published, it) },
                c.getString(R.string.paper_source_accessed, s.accessedAt),
            ).joinToString(" · ")
            w.text("${s.url}\n$dates", size = 8.5f, indent = 12f, after = 5f)
        }
    }
}
