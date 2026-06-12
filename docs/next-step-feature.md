# Next Step Feature — Documentation technique Android

Affichage d'une suggestion personnalisée ("prochain pas") sur l'écran d'accueil, avec complétion, dismiss et gestion des push notifications.

L'API backend est documentée dans `entourage-ror/docs/next-step-suggestions.md`.

---

## Architecture

```
NextStepRequest          ← interface Retrofit (6 endpoints)
       ↓
ApiModule                ← nextStepRequest instancié dans init {}
       ↓
NextStepPresenter        ← ViewModel, LiveData, appels API
       ↓
NextStepAdapter          ← RecyclerView adapter (ConcatAdapter)
       ↓
HomeFragment             ← intègre l'adapter en première position
```

---

## Fichiers créés

### `api/model/NextStep.kt`

Data classes Gson pour la désérialisation des réponses API.

```kotlin
data class NextStep(
    @SerializedName("id")              val id: Int,
    @SerializedName("suggestion_type") val suggestionType: String,
    @SerializedName("title")           val title: String,
    @SerializedName("reason")          val reason: String?,
    @SerializedName("cta_label")       val ctaLabel: String,
    @SerializedName("cta_action")      val ctaAction: String?,
    @SerializedName("expires_at")      val expiresAt: String?
)
data class NextStepResponse(@SerializedName("next_step") val nextStep: NextStep?)

data class OnboardingQuestion(
    @SerializedName("key")           val key: String,
    @SerializedName("title")         val title: String,
    @SerializedName("type")          val type: String,   // "cards" ou "chips"
    @SerializedName("options")       val options: List<OnboardingOption>,
    @SerializedName("current_value") val currentValue: String?
)
data class OnboardingOption(
    @SerializedName("value") val value: String,
    @SerializedName("label") val label: String
)
data class OnboardingQuestionsResponse(@SerializedName("questions") val questions: List<OnboardingQuestion>)
```

### `api/request/NextStepRequest.kt`

Interface Retrofit avec 6 endpoints :

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `next_step` | Suggestion courante (crée si besoin) |
| `PATCH` | `next_step/{id}/complete` | Marque comme complétée |
| `PATCH` | `next_step/{id}/dismiss` | Marque comme ignorée (exclusion 30j) |
| `POST` | `next_step/tap_push` | Signale un tap sur push (reset compteur silence) |
| `GET` | `users/me/onboarding_questions` | 3 questions de personnalisation |
| `PATCH` | `users/me/onboarding_preferences` | Sauvegarde les préférences |

### `home/NextStepPresenter.kt`

ViewModel exposant :

| LiveData | Type | Description |
|---|---|---|
| `nextStep` | `MutableLiveData<NextStep?>` | Suggestion courante, `null` pendant le cooling-off |
| `isLoading` | `MutableLiveData<Boolean>` | État de chargement |
| `actionSuccess` | `MutableLiveData<Boolean>` | Résultat de complete/dismiss |

Méthodes :
- `loadNextStep()` — charge depuis l'API, met à jour `nextStep`
- `completeStep(id)` — PATCH complete, passe `nextStep` à `null`
- `dismissStep(id)` — PATCH dismiss, recharge via `loadNextStep()`
- `tapPush()` — POST tap_push, fire and forget

### `home/NextStepAdapter.kt`

Adapter RecyclerView compatible `ConcatAdapter`. Reçoit deux lambdas à la construction :

```kotlin
NextStepAdapter(
    onCtaClick = { step -> presenter.completeStep(step.id) },
    onDismissClick = { step -> presenter.dismissStep(step.id) }
)
```

Méthode `update(step: NextStep?)` — affiche la carte si `step != null`, la masque sinon.

### `home/NextStepCardFragment.kt`

Fragment léger. Observe `NextStepPresenter.nextStep` et délègue l'affichage à `NextStepAdapter`. Appelle `loadNextStep()` dans `onResume()` pour rafraîchir au retour sur l'écran.

### `res/layout/fragment_next_step_card.xml`

Card design system Entourage Local :
- Fond `#FEEAE3` (orange extra light), `borderRadius="16dp"`, `margin="16dp"`
- Label "VOTRE PROCHAIN PAS" — uppercase, `#FF9739`, 11sp bold
- Titre : 16sp bold, `#363636`
- Raison (optionnelle) : 13sp, `#6D6C6C`, `visibility="gone"` si null
- Bouton CTA plein orange `#FF9739`, pill `borderRadius="32dp"`, texte blanc 15sp
- Bouton dismiss `×` en haut à droite, `#A0A0A0`

### `res/drawable/bg_next_step_card.xml`

Shape drawable fond `#FEEAE3`, corners 16dp.

---

## Fichiers modifiés

### `api/ApiModule.kt`

```kotlin
val nextStepRequest: NextStepRequest  // déclaré

// dans init {}
nextStepRequest = providesNextStepRequest(retrofit)

// factory method
private fun providesNextStepRequest(retrofit: Retrofit): NextStepRequest =
    retrofit.create(NextStepRequest::class.java)
```

### `home/HomeFragment.kt`

```kotlin
// Champs ajoutés
private lateinit var nextStepAdapter: NextStepAdapter
private lateinit var nextStepPresenter: NextStepPresenter

// Dans setupAdapters() — adapter ajouté en premier dans concatAdapter
nextStepPresenter = ViewModelProvider(this).get(NextStepPresenter::class.java)
nextStepAdapter = NextStepAdapter(
    onCtaClick = { step -> nextStepPresenter.completeStep(step.id) },
    onDismissClick = { step -> nextStepPresenter.dismissStep(step.id) }
)
concatAdapter.addAdapter(0, nextStepAdapter)  // première position

// Dans onViewCreated()
nextStepPresenter.nextStep.observe(viewLifecycleOwner) { step ->
    nextStepAdapter.update(step)
}

// Dans onResume()
nextStepPresenter.loadNextStep()
```

### `notifications/EntourageFirebaseMessagingService.kt`

```kotlin
// Ajouté dans onMessageReceived()
if (remoteMessage.data["type"] == KEY_TYPE_NEXT_STEP) {
    handleNextStepPush()
    return
}

// Nouvelle méthode
private fun handleNextStepPush() {
    NextStepPresenter().tapPush()
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra(KEY_EXTRA_NEXT_STEP_PUSH, true)
    }
    startActivity(intent)
}

companion object {
    const val KEY_TYPE_NEXT_STEP = "next_step"
    const val KEY_EXTRA_NEXT_STEP_PUSH = "next_step_push"
}
```

---

## Comportement de la carte

| État | Affichage |
|---|---|
| Suggestion active | Carte visible avec titre, raison (si présente), bouton CTA, dismiss |
| Cooling-off (2h post-complétion) | Carte masquée — `next_step: null` retourné par l'API |
| Retour sur l'écran | `onResume()` recharge — nouvelle suggestion disponible si cooling-off écoulé |
| Dismiss | Recharge immédiate avec le type suivant (fallback garanti) |

---

## Tests manuels

**1. Vérifier l'affichage de la carte**
- Lancer l'app avec un compte dont la DB a des seeds (`bin/rails db:migrate` côté Rails)
- L'écran d'accueil doit afficher la carte en première position

**2. Compléter une suggestion**
- Taper le bouton CTA → carte disparaît pendant 2h
- Revenir sur l'écran après 2h → nouvelle suggestion affichée

**3. Dismiss**
- Taper `×` → carte se recharge avec un type différent

**4. Push notification**
- Envoyer un push avec `data: { type: "next_step" }` via Firebase Console
- Vérifier que l'app s'ouvre sur l'accueil et que le compteur push est remis à 0

**5. Vérification réseau (Charles / Logcat)**
```
GET  /api/v1/next_step          → 200 { next_step: {...} }
PATCH /api/v1/next_step/:id/complete → 200
PATCH /api/v1/next_step/:id/dismiss  → 200
POST  /api/v1/next_step/tap_push     → 200
```

---

## Ce qui reste à faire

### Onboarding questions (Android)
Les endpoints API sont en place (`getOnboardingQuestions()` / `updateOnboardingPreferences()`), mais aucun écran n'a été créé pour les poser. À implémenter : un écran de personnalisation accessible depuis le profil ou déclenché au premier lancement après inscription.

### Font Poppins Bold
Le design system Entourage utilise Poppins Bold. Seul `poppinregular.ttf` est bundlé dans `res/font/`. Pour un rendu fidèle, ajouter `Poppins-SemiBold.ttf` et `Poppins-Bold.ttf` dans `res/font/` et mettre à jour `fragment_next_step_card.xml`.

### Admin interface Rails
Voir `entourage-ror/docs/next-step-suggestions.md` — section "Ce qui reste à faire".
