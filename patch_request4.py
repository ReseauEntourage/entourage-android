with open('./app/src/main/java/social/entourage/android/events/EventsPresenter.kt', 'r') as f:
    content = f.read()

# Put back JoinRoleBody in EventsPresenter.kt
if "data class JoinRoleBody(" not in content:
    content = content.replace("}", "}\n\ndata class JoinRoleBody(\n    @field:SerializedName(\"role\")\n    val role: String\n)\n")

with open('./app/src/main/java/social/entourage/android/events/EventsPresenter.kt', 'w') as f:
    f.write(content)
