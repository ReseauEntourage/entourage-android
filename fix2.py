with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'r') as f:
    content = f.read()

content = content.replace("import social.entourage.android.events.JoinRoleBody\n", "")
if "data class JoinRoleBody" not in content:
    content = content + "\ndata class JoinRoleBody(\n    @com.google.gson.annotations.SerializedName(\"role\")\n    val role: String\n)\n"

with open('./app/src/main/java/social/entourage/android/api/request/EventsRequest.kt', 'w') as f:
    f.write(content)
