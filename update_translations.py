import os
import re

translations = {
    "values-en": {
        "home_title_small_talk": "Join a supportive discussion",
        "small_talk_subtitle_match": "Connect with a small group of people who share your interests, anywhere in France."
    },
    "values-es": {
        "home_title_small_talk": "Únete a una discusión solidaria",
        "small_talk_subtitle_match": "Te conectamos con un pequeño grupo de personas que comparten tus intereses, en cualquier parte de Francia."
    },
    "values-de": {
        "home_title_small_talk": "Nimm an einer solidarischen Diskussion teil",
        "small_talk_subtitle_match": "Wir bringen Sie mit einer kleinen Gruppe von Menschen zusammen, die Ihre Interessen teilen, überall in Frankreich."
    },
    "values-pl": {
        "home_title_small_talk": "Dołącz do wspierającej dyskusji",
        "small_talk_subtitle_match": "Łączymy Cię z małą grupą osób o podobnych zainteresowaniach, w całej Francji."
    },
    "values-ro": {
        "home_title_small_talk": "Alătură-te unei discuții de susținere",
        "small_talk_subtitle_match": "Te conectăm cu un mic grup de persoane care îți împărtășesc interesele, oriunde în Franța."
    },
    "values-uk": {
        "home_title_small_talk": "Приєднуйтесь до групи підтримки",
        "small_talk_subtitle_match": "Ми з'єднаємо вас з невеликою групою людей, які поділяють ваші інтереси, по всій Франції."
    },
    "values-ar": {
        "home_title_small_talk": "انضم إلى مناقشة تضامنية",
        "small_talk_subtitle_match": "نوصلك بمجموعة صغيرة من الأشخاص الذين يشاركونك اهتماماتك، في أي مكان في فرنسا."
    }
}

base_path = "app/src/main/res"

def update_file(folder, keys):
    file_path = os.path.join(base_path, folder, "strings.xml")
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return

    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    for key, value in keys.items():
        # Escape single quotes in value
        escaped_value = value.replace("'", "\'")

        # Check if key exists
        pattern = f'<string name="{key}">.*?</string>'
        replacement = f'<string name="{key}">{escaped_value}</string>'

        if re.search(pattern, content):
            content = re.sub(pattern, replacement, content)
            print(f"Updated {key} in {folder}")
        else:
            # Insert before </resources>
            insert_pattern = "</resources>"
            insert_string = f'    {replacement}\n</resources>'
            content = content.replace(insert_pattern, insert_string)
            print(f"Inserted {key} in {folder}")

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

for folder, keys in translations.items():
    update_file(folder, keys)
