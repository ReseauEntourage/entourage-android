import re

# 1. Update MetaData.kt
with open('./app/src/main/java/social/entourage/android/api/model/MetaData.kt', 'r') as f:
    metadata_content = f.read()

new_fields = """
    @field:SerializedName("unsubscribed_participants_offer_help")
    var unsubscribedParticipantsOfferHelp: Int? = 0,

    @field:SerializedName("unsubscribed_participants_ask_for_help")
    var unsubscribedParticipantsAskForHelp: Int? = 0,
) : Serializable"""

metadata_content = metadata_content.replace(") : Serializable", new_fields, 1)

with open('./app/src/main/java/social/entourage/android/api/model/MetaData.kt', 'w') as f:
    f.write(metadata_content)

# 2. Update Events.kt (Remove fields from root)
with open('./app/src/main/java/social/entourage/android/api/model/Events.kt', 'r') as f:
    events_content = f.read()

events_content = re.sub(r'\s+@field:SerializedName\("unsubscribed_participants_offer_help"\)\n\s+var unsubscribedParticipantsOfferHelp: Int\? = 0,', '', events_content)
events_content = re.sub(r'\s+@field:SerializedName\("unsubscribed_participants_ask_for_help"\)\n\s+var unsubscribedParticipantsAskForHelp: Int\? = 0,', '', events_content)

events_content = events_content.replace("\n        this.unsubscribedParticipantsOfferHelp,", "")
events_content = events_content.replace("\n        this.unsubscribedParticipantsAskForHelp,", "")

with open('./app/src/main/java/social/entourage/android/api/model/Events.kt', 'w') as f:
    f.write(events_content)

# 3. Update EventModel.kt (Remove fields from root)
with open('./app/src/main/java/social/entourage/android/events/EventModel.kt', 'r') as f:
    eventmodel_content = f.read()

eventmodel_content = eventmodel_content.replace("\n    var unsubscribedParticipantsOfferHelp: Int? = 0,", "")
eventmodel_content = eventmodel_content.replace("\n    var unsubscribedParticipantsAskForHelp: Int? = 0,", "")

with open('./app/src/main/java/social/entourage/android/events/EventModel.kt', 'w') as f:
    f.write(eventmodel_content)

# 4. Update MembersFragment.kt
with open('./app/src/main/java/social/entourage/android/groups/details/members/MembersFragment.kt', 'r') as f:
    membersfragment_content = f.read()

membersfragment_content = membersfragment_content.replace("event.unsubscribedParticipantsAskForHelp ?: 0", "event.metadata?.unsubscribedParticipantsAskForHelp ?: 0")
membersfragment_content = membersfragment_content.replace("event.unsubscribedParticipantsOfferHelp ?: 0", "event.metadata?.unsubscribedParticipantsOfferHelp ?: 0")

with open('./app/src/main/java/social/entourage/android/groups/details/members/MembersFragment.kt', 'w') as f:
    f.write(membersfragment_content)
