1. **Remove horizontal wrapper for Small Talk in `HomeFragment`**
   - In `HomeFragment.kt`, instead of adding `smallTalkWrapperAdapter`, add `homeSmallTalkAdapter` directly into the `ConcatAdapter` so it spans the full width and isn't horizontally scrollable.
   - Adjust `homeSmallTalkAdapter` to not set layout width to 85% of screen width in `onViewAttachedToWindow` (since it's a vertical list now, we can rely on `match_parent`).
   - Also, since it's going to be a single card summarising everything, the adapter shouldn't emit multiple items. We'll change `HomeSmallTalkAdapter` to just render a single summary item (or empty list if no items).
2. **Update `HomeSmallTalkItem` and adapter data logic**
   - Replace the multiple item logic in `HomeFragment.kt`:
     - Instead of a list of `HomeSmallTalkItem` representing each conversation or waiting state, create a unified `HomeSmallTalkSummary` model.
     - `matchedRequests` = `currentRequests.filter { it.smalltalkId != null }`
     - `hasUnmatchedRequest` = `currentRequests.any { it.smalltalkId == null }`
     - Logic:
       - If `currentRequests.isEmpty()`: pass "0 discussion" state (match possible)
       - If `matchedRequests.isEmpty()` and `hasUnmatchedRequest`: pass "waiting" state
       - If `matchedRequests.isNotEmpty()`: pass "active discussions" state, taking the first avatar from each `UserSmallTalkRequest` for the avatar pile, counting unread messages, and checking if `currentRequests.size < 3` to show the "+ Lancer" button.
3. **Update UI Layouts**
   - Create/Update a layout file `item_home_small_talk_summary.xml` replacing `item_home_small_talk_match.xml`, `item_home_small_talk_conversation.xml`, and `item_home_small_talk_waiting.xml` (or keep them separate but updated). The easiest is to keep three distinct layouts corresponding to the three AC states:
     - `TYPE_MATCH` (AC1: 0 discussion): `item_home_small_talk_match.xml` updated to match screenshot `0_discussion.png` (Title, Subtitle, CTA Discuter, Image puzzle).
     - `TYPE_WAITING` (En cours de matching): `item_home_small_talk_waiting.xml` updated to match screenshot `matching.png`.
     - `TYPE_ACTIVE` (AC2: 1 à 3 discussions engagées): New `item_home_small_talk_active.xml` based on screenshots (Title "Vos discussions solidaires", subtitle "X discussion(s) active(s) [+ 1 en cours de matching]", avatars pile, "Voir" button with badge, "+ Lancer une nouvelle rencontre (X/3)" button).
4. **Navigation updates for "Voir" button**
   - Add a parameter `goConv` to `MainActivity` or simply use the existing one but append a specific parameter to select the "Bonnes ondes" filter. Let's look into how filters are applied in `MessagesFragment`.
5. **Add Analytics**
   - Add `click_bonnes_ondes_start_discussion` on the "Discuter" button in the match state.
   - Add `click_bonnes_ondes_view_messages` on the "Voir" button in the active state.
   - Note: Analytics constants might need to be added to `AnalyticsEvents.kt`.
6. **Pre-commit checks**
   - Ensure strings are properly translated or at least present in `values/strings.xml`.
   - Run tests or pre-commit instructions.
