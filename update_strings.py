import xml.etree.ElementTree as ET

def add_strings(file_path):
    tree = ET.parse(file_path)
    root = tree.getroot()

    strings = [
        {"name": "ajouter_des_participants", "text": "Ajouter des participants"},
        {"name": "question_isoles", "text": "Combien de personnes isolées supplémentaires ont rejoint l'événement ?"},
        {"name": "question_riverains", "text": "Combien de riverains supplémentaires ont rejoint l'événement ?"},
        {"name": "participants_ajoutes_sur_place", "text": "PARTICIPANTS AJOUTÉS SUR PLACE"},
        {"name": "personnes_isolees", "text": "%1$d personnes isolées"},
        {"name": "personne_isolee", "text": "%1$d personne isolée"},
        {"name": "riverains", "text": "%1$d riverains"},
        {"name": "riverain", "text": "%1$d riverain"},
        {"name": "ajoutes_sur_place", "text": "Ajoutés sur place"},
        {"name": "ajoute_sur_place", "text": "Ajouté sur place"}
    ]

    for s in strings:
        existing = root.find(f"./string[@name='{s['name']}']")
        if existing is None:
            elem = ET.SubElement(root, "string", name=s["name"])
            elem.text = s["text"]

    tree.write(file_path, encoding='utf-8', xml_declaration=True)

add_strings('./app/src/main/res/values/strings.xml')
