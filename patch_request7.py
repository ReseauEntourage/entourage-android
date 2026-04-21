with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'r') as f:
    content = f.read()

content = content.replace("import social.entourage.android.api.model.ReactionWrapper\nimport social.entourage.android.events.JoinRoleBody\nimport social.entourage.android.events.JoinRoleBody\n", "import social.entourage.android.api.model.ReactionWrapper\nimport social.entourage.android.events.JoinRoleBody\n")
content = content.replace("import social.entourage.android.events.JoinRoleBody\nimport social.entourage.android.events.JoinRoleBody\n", "import social.entourage.android.events.JoinRoleBody\n")

if "import social.entourage.android.events.JoinRoleBody" not in content:
    content = content.replace("import social.entourage.android.api.model.ReactionWrapper", "import social.entourage.android.api.model.ReactionWrapper\nimport social.entourage.android.events.JoinRoleBody")

with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'w') as f:
    f.write(content)
