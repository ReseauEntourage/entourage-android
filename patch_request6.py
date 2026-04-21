with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'r') as f:
    content = f.read()

# Instead of blindly removing/replacing, make sure JoinRoleBody exists!
content = content.replace("import social.entourage.android.api.model.ReactionWrapper\n", "import social.entourage.android.api.model.ReactionWrapper\nimport social.entourage.android.events.JoinRoleBody\n")

# Wait, the error says 'JoinRoleBody' is unresolved in EventsRequest.kt itself, meaning the import is failing, meaning JoinRoleBody doesn't exist in social.entourage.android.events.JoinRoleBody anymore?
# Wait, I added it to EventsPresenter.kt previously. It should be imported from there if it's there.
