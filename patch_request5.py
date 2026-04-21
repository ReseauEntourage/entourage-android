with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'r') as f:
    content = f.read()

# Let's restore the import for JoinRoleBody
content = content.replace("import social.entourage.android.api.model.ReactionWrapper\n", "import social.entourage.android.api.model.ReactionWrapper\nimport social.entourage.android.events.JoinRoleBody\n")
with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'w') as f:
    f.write(content)
