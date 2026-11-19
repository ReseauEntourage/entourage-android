package social.entourage.android.suggestion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import social.entourage.android.api.json.AppJson
import social.entourage.android.api.request.NextStepDto
import social.entourage.android.api.request.NextStepEnvelope
import social.entourage.android.api.request.NextStepsEnvelope
import java.util.Locale

/**
 * Décodage et adaptation du contrat du moteur (design.md, décision 12). Les JSON reprennent la
 * forme de `V1::NextStepSerializer` et des sérialiseurs v1 des cibles.
 */
class NextStepMapperTest {

    private fun hero(json: String): NextStepDto = AppJson.decode<NextStepEnvelope>(json).nextStep!!

    private fun envelope(kind: String, id: Int? = 12, target: String = "null", targets: String = "null", link: String = "null", reasons: String = "[]") =
        """{"next_step":{"id":${id ?: "null"},"kind":"$kind","mode":"segment","placement":"hero","target":$target,"targets":$targets,"link":$link,"reasons":$reasons,"expires_at":"2026-11-19T10:00:00.000Z"}}"""

    private fun map(json: String) = NextStepMapper.toSuggestion(hero(json), Locale.FRANCE)

    @Test
    fun inPersonOutingBecomesAnInPersonEvent() {
        val s = map(
            envelope(
                "outing", 12,
                target = """{"id":5,"title":"Café solidaire","online":false,"metadata":{"starts_at":"2026-11-20T14:00:00.000+01:00","display_address":"Paris 11e"}}""",
                reasons = """[{"code":"near_you","text":"C'est à 800 m de chez vous."},{"code":"popular","text":"3 personnes y participent déjà."}]""",
            ),
        )!!
        assertEquals(SuggestionType.EVENT.key, s.typeKey)
        assertEquals(SuggestionVariant.IN_PERSON, s.variant)
        assertEquals(SuggestionCta.PARTICIPATE, s.cta)
        assertEquals("Café solidaire", s.title)
        assertEquals(5, s.targetId)
        assertEquals(12, s.engineId)
        assertEquals(listOf("near_you", "popular"), s.reasons.map { it.code })
        assertEquals("C'est à 800 m de chez vous.", s.reasons.first().text)
        assertEquals("Paris 11e", s.metas.last().text)
    }

    @Test
    fun onlineOutingIsAPapotageShownAsARemoteEvent() {
        val s = map(envelope("outing", target = """{"id":6,"title":"Papotage solidaire","online":true}"""))!!
        assertEquals(SuggestionVariant.REMOTE, s.variant)
        assertEquals(SuggestionCta.REGISTER, s.cta)
        assertTrue(s.metas.any { it.textRes != null })
    }

    @Test
    fun neighborhoodIsLocalOrNational() {
        val local = map(envelope("neighborhood", target = """{"id":7,"name":"Voisins du 11e","national":false,"address":{"display_address":"Paris 11e"}}"""))!!
        assertEquals(SuggestionVariant.LOCAL, local.variant)
        assertEquals(SuggestionCta.JOIN, local.cta)
        val national = map(envelope("neighborhood", target = """{"id":8,"name":"Cuisine et partage","national":true}"""))!!
        assertEquals(SuggestionVariant.NATIONAL, national.variant)
        assertTrue(national.metas.isEmpty())
    }

    @Test
    fun actionKeepsTheUserWordsUntouched() {
        val s = map(envelope("action", target = """{"id":9,"title":"Besoin d'un téléphone","description":"Bonjour à tous…","author":{"id":3,"display_name":"Nino D."}}"""))!!
        assertEquals(SuggestionType.ENTRAIDE.key, s.typeKey)
        assertEquals("Besoin d'un téléphone", s.title)
        assertEquals("Bonjour à tous…", s.body)
        assertEquals("Nino D.", s.personName)
        assertEquals(SuggestionCta.ANSWER, s.cta)
    }

    @Test
    fun poiBecomesAPlace() {
        val s = map(envelope("poi", target = """{"id":10,"name":"Bagagerie du 20e","address":"3 rue X, Paris","hours":"Ouvert jusqu'à 18h"}"""))!!
        assertEquals(SuggestionType.PLACE.key, s.typeKey)
        assertEquals("Bagagerie du 20e", s.title)
        assertEquals(SuggestionCta.SEE_PLACE, s.cta)
        assertEquals(10, s.targetId)
    }

    /** Réponse réelle de préprod (artifact du 16/11) : cible `action`, sans clé racine, `metadata` vide. */
    @Test
    fun realPreprodActionTargetDecodes() {
        val target = """{"id":149820,"uuid":"eWaSLFcwbiFk","uuid_v2":"eWaSLFcwbiFk","status":"open","section":"clothes","title":"Je cherche chaussure d'hiver homme 42","title_translations":null,"description":"Je cherche chaussure d'hiver homme 42","description_translations":null,"image_url":null,"action_type":"solicitation","author":{"id":5100,"display_name":"Clem T.","avatar_url":null,"created_at":"2025-11-05T16:45:40.360+01:00"},"metadata":{},"member":false,"members":[{"id":1,"lang":"fr","avatar_url":"n/a","display_name":"n/a"}],"members_count":1,"created_at":"2026-10-01T16:38:03.150+02:00","updated_at":"2026-10-15T16:14:58.639+02:00","status_changed_at":null,"distance":2.5,"location":{"latitude":48.85298008272297,"longitude":2.415879454243083}}"""
        val s = map(envelope("action", id = null, target = target))!!
        assertEquals(SuggestionType.ENTRAIDE.key, s.typeKey)
        assertEquals(149820, s.targetId)
        assertEquals("Je cherche chaussure d'hiver homme 42", s.title)
        assertEquals("Clem T.", s.personName)
    }

    /** Réponse réelle de préprod (artifact du 16/11) : cible `poi`, `category_ids`, `partner_id` nul. */
    @Test
    fun realPreprodPoiTargetDecodes() {
        val target = """{"id":145042,"name":"Maison Béthanie - Secours Catholique","description":"Accueil de jour","longitude":5.371295,"latitude":43.304681,"phone":"488087670","website":null,"email":"accueildejour.131@secours-catholique.org","audience":"Accueil inconditionnel","partner_id":null,"address":"11 Rue malaval, 13002 Marseille","hours":"lun : 8h30 à 11h00\nmar : Fermé","languages":"","air_conditioned":false,"source_url":"https://soliguide.fr/fiche/13977","uuid":"s13977","source":"soliguide","category_ids":[6,41,42,43,3,1,7,5]}"""
        val s = map(envelope("poi", id = null, target = target))!!
        assertEquals(SuggestionType.PLACE.key, s.typeKey)
        assertEquals(145042, s.targetId)
        assertEquals("Maison Béthanie - Secours Catholique", s.title)
        assertTrue(s.metas.any { it.text == "11 Rue malaval, 13002 Marseille" })
    }

    @Test
    fun contentPathKeepsItsThreeContentsAndOpensTheFirst() {
        val s = map(
            envelope("content_path", targets = """[{"id":21,"name":"Comprendre"},{"id":22,"name":"Agir"},{"id":23,"name":"Aller vers l'autre"}]"""),
        )!!
        assertEquals(listOf(21, 22, 23), s.items.map { it.id })
        assertEquals(21, s.targetId)
        assertEquals(3, s.metas.size)
        assertEquals(SuggestionCta.READ, s.cta)
    }

    @Test
    fun contentPathOpensTheFirstUnreadContent() {
        val s = map(
            envelope("content_path", targets = """[{"id":21,"name":"Lu","watched":true},{"id":22,"name":"Non lu","watched":false},{"id":23,"name":"Autre","watched":false}]"""),
        )!!
        assertEquals(22, s.targetId)
        assertEquals(22, s.firstContentToOpen()!!.id)
        assertEquals(listOf(21, 22, 23), s.items.map { it.id })
    }

    @Test
    fun contentPathOpensTheFirstWhenAllAreRead() {
        val s = map(
            envelope("content_path", targets = """[{"id":21,"name":"A","watched":true},{"id":22,"name":"B","watched":true},{"id":23,"name":"C","watched":true}]"""),
        )!!
        assertEquals(21, s.targetId)
        assertEquals(21, s.firstContentToOpen()!!.id)
    }

    @Test
    fun otherContentPathSmalltalkAndAmbassadorOmitTargetId() {
        fun other(kind: String, extra: String) =
            """{"id":null,"kind":"$kind","mode":"segment","placement":"others","target":null,$extra"reasons":[]}"""
        val json = """{"next_steps":[
            ${other("content_path", """"targets":[{"id":21,"name":"A"},{"id":22,"name":"B"}],""")},
            ${other("smalltalk", "")},
            ${other("become_ambassador", """"link":"https://forms.example.com/x",""")}
        ]}"""
        val accesses = NextStepMapper.toExplorer(AppJson.decode<NextStepsEnvelope>(json).nextSteps)
        assertEquals(3, accesses.size)
        accesses.forEach {
            val interaction = SuggestionInteraction.of(SuggestionInteraction.TAPPED, it.suggestion!!)
            assertEquals(it.typeKey, interaction.itemKind)
            assertNull(it.typeKey, interaction.targetId)
        }
    }

    @Test
    fun postMessageKnowsWhetherTheGroupIsNational() {
        val national = map(envelope("post_message", target = """{"id":5,"name":"Groupe national"}"""))!!
        assertTrue(national.nationalGroup)
        val local = map(envelope("post_message", target = """{"id":6,"name":"Voisins","zone":"75"}"""))!!
        assertEquals(false, local.nationalGroup)
    }

    /** Badge : groupe de voisins (« Discuter avec vos voisins ») ou groupe thématique (« Discuter dans votre groupe »). */
    @Test
    fun postMessageBadgeDependsOnNeighborsOrThematicGroup() {
        val thematic = map(envelope("post_message", target = """{"id":5,"name":"Cuisine et partage","national":true}"""))!!
        assertTrue(thematic.thematicGroup)
        val neighbors = map(envelope("post_message", target = """{"id":522,"name":"Voisins","national":false,"zone":null}"""))!!
        assertEquals(false, neighbors.thematicGroup)
    }

    @Test
    fun smalltalkAndAmbassadorUseTheEngineSentenceAsText() {
        val reasons = """[{"code":"smalltalk","text":"Les Bonnes ondes vous mettent en relation."}]"""
        val talk = map(envelope("smalltalk", reasons = reasons))!!
        assertEquals("Les Bonnes ondes vous mettent en relation.", talk.body)
        assertEquals(SuggestionCta.TRY, talk.cta)
        val ambassador = map(envelope("become_ambassador", link = "\"https://forms.example.com/x\"", reasons = reasons))!!
        assertEquals("https://forms.example.com/x", ambassador.link)
        assertEquals(SuggestionCta.LEARN_MORE, ambassador.cta)
    }

    @Test
    fun postMessageOpensTheGroupAndShowsItsName() {
        val s = map(envelope("post_message", target = """{"id":5,"name":"Voisins du 11e"}""", reasons = """[{"code":"group_member_message","text":"Un petit message suffit."}]"""))!!
        assertEquals(SuggestionType.POST_MESSAGE.key, s.typeKey)
        assertEquals("Voisins du 11e", s.title)
        assertEquals("Un petit message suffit.", s.body)
        assertEquals(SuggestionCta.WRITE, s.cta)
        assertEquals(5, s.targetId)
    }

    @Test
    fun unknownKindIsDropped() {
        assertNull(map(envelope("hologram")))
    }

    @Test
    fun unreadableTargetIsDroppedWithoutCrashing() {
        assertNull(map(envelope("outing", target = "null")))
        assertNull(map(envelope("outing", target = """{"title":"sans identifiant"}""")))
    }

    @Test
    fun reasonWithoutTextIsIgnored() {
        val s = map(envelope("smalltalk", reasons = """[{"code":"smalltalk"},{"code":"x","text":""}]"""))!!
        assertTrue(s.reasons.isEmpty())
    }

    @Test
    fun othersAreLimitedToThreeAndTapOpensTheTarget() {
        fun other(id: Int) = """{"id":null,"kind":"neighborhood","mode":"segment","placement":"others","target":{"id":$id,"name":"Groupe $id","national":true},"reasons":[]}"""
        val json = """{"next_steps":[${(1..5).joinToString(",") { other(it) }}]}"""
        val accesses = NextStepMapper.toExplorer(AppJson.decode<NextStepsEnvelope>(json).nextSteps)
        assertEquals(3, accesses.size)
        val first = accesses.first()
        assertEquals("Groupe 1", first.label)
        assertNotNull(first.suggestion)
        assertNull(first.suggestion!!.engineId)
        assertEquals(1, first.suggestion!!.targetId)
        // Une autre piste se mesure par son type et sa cible, jamais par un identifiant de prochain pas.
        val interaction = SuggestionInteraction.of(SuggestionInteraction.TAPPED, first.suggestion!!)
        assertEquals("neighborhood", interaction.itemKind)
        assertEquals(1, interaction.targetId)
        assertNull(interaction.nextStepId)
    }

    @Test
    fun heroInteractionCarriesTheNextStepId() {
        val s = map(envelope("smalltalk", id = 42))!!
        assertEquals(42, SuggestionInteraction.of(SuggestionInteraction.DISPLAYED, s).nextStepId)
    }

    @Test
    fun nullNextStepDuringTheWelcomeJourneyDecodesToNothing() {
        assertNull(AppJson.decode<NextStepEnvelope>("""{"next_step":null}""").nextStep)
    }
}
