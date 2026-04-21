with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'r') as f:
    content = f.read()

# Make sure JoinRoleBody exists since we removed it from EventsPresenter.kt earlier
content = content.replace("import social.entourage.android.events.JoinRoleBody\n", "")

with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'w') as f:
    f.write(content)
