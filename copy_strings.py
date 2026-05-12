import xml.etree.ElementTree as ET
import glob
import os

keys_to_copy = [
    'home_title_small_talk_active',
    'home_subtitle_small_talk_active',
    'home_subtitle_small_talk_active_single',
    'home_small_talk_matching',
    'home_small_talk_launch_new',
    'home_small_talk_subtitle_waiting'
]

def copy_strings():
    # Load base strings
    base_tree = ET.parse('app/src/main/res/values/strings.xml')
    base_root = base_tree.getroot()
    base_dict = {}
    for el in base_root:
        if el.attrib.get('name') in keys_to_copy:
            base_dict[el.attrib.get('name')] = el.text

    # Apply to all other strings.xml
    for filepath in glob.glob('app/src/main/res/values-*/strings.xml'):
        tree = ET.parse(filepath)
        root = tree.getroot()
        changed = False

        for key in keys_to_copy:
            # Check if exists
            exists = False
            for el in root:
                if el.attrib.get('name') == key:
                    exists = True
                    break

            if not exists and key in base_dict:
                new_el = ET.Element('string', {'name': key})
                new_el.text = base_dict[key]
                new_el.tail = '\n    '
                root.append(new_el)
                changed = True

        if changed:
            tree.write(filepath, encoding='utf-8', xml_declaration=True)

if __name__ == "__main__":
    copy_strings()
