with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'r') as f:
    content = f.read()
if "import social.entourage.android.events.JoinRoleBody" not in content:
    content = content.replace("import social.entourage.android.events.create.CreateEvent", "import social.entourage.android.events.create.CreateEvent\nimport social.entourage.android.events.JoinRoleBody")
with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'w') as f:
    f.write(content)
