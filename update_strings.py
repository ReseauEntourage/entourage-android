import xml.etree.ElementTree as ET
import sys

def add_or_update_string(tree, name, value):
    root = tree.getroot()
    # Check if exists
    for child in root:
        if child.attrib.get('name') == name:
            child.text = value
            return

    # If not exists, add
    new_el = ET.Element('string', {'name': name})
    new_el.text = value
    new_el.tail = '\n    '
    root.append(new_el)

def main():
    tree = ET.parse('app/src/main/res/values/strings.xml')
    add_or_update_string(tree, 'home_title_small_talk_active', 'Vos discussions solidaires')
    add_or_update_string(tree, 'home_subtitle_small_talk_active', '%1$d discussions actives')
    add_or_update_string(tree, 'home_subtitle_small_talk_active_single', '%1$d discussion active')
    add_or_update_string(tree, 'home_small_talk_matching', '+%1$d en cours de matching')
    add_or_update_string(tree, 'home_small_talk_launch_new', '+ Lancer une nouvelle rencontre (%1$d/3)')
    add_or_update_string(tree, 'home_small_talk_subtitle_waiting', '%1$d discussion en cours de matching')

    # Format and save
    tree.write('app/src/main/res/values/strings.xml', encoding='utf-8', xml_declaration=True)

if __name__ == "__main__":
    main()
