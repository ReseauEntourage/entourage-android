with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'r') as f:
    content = f.read()
content = content.replace("import social.entourage.android.events.JoinRoleBody\n", "")
with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'w') as f:
    f.write(content)
