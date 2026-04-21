import re

with open('./app/src/main/java/social/entourage/android/events/EventsPresenter.kt', 'r') as f:
    content = f.read()

# Remove duplicate JoinRoleBody from EventsPresenter
content = re.sub(r'data class JoinRoleBody\(\n\s+@field:SerializedName\("role"\)\n\s+val role: String\n\)', '', content, flags=re.DOTALL)

with open('./app/src/main/java/social/entourage/android/events/EventsPresenter.kt', 'w') as f:
    f.write(content)
