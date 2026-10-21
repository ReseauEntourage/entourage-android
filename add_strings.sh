sed -i '' '/<\/resources>/i\
    <!-- Parcours de création / d’édition de groupe (flux-creation-unifie) -->\
    <string name="create_flow_optional">facultatif</string>\
    <string name="create_group_step_one_title">Présentez votre groupe</string>\
    <string name="create_group_description_hint">Dites en quelques mots de quoi parle votre groupe et à qui il s’adresse.</string>\
    <string name="create_group_description_placeholder">Ex : Un groupe pour se retrouver entre voisins, partager des bons plans et s’entraider.</string>\
    <string name="create_group_step_two_title">De quoi parle votre groupe\\u00a0?</string>\
    <string name="create_group_step_three_title">Ajoutez une photo à votre groupe</string>\
    <string name="create_group_preview_title">Aperçu de votre groupe</string>\
    <string name="create_group_error_name">Donnez un nom à votre groupe pour continuer.</string>\
    <string name="create_group_error_description">Ajoutez quelques mots pour présenter votre groupe.</string>\
    <string name="create_group_error_location">Indiquez un lieu pour continuer.</string>\
    <string name="back_edit_group_content">Si vous quittez, les modifications apportées au groupe seront perdues.</string>
' app/src/main/res/values/strings.xml
if [ -f "app/src/main/res/values-en/strings.xml" ]; then
sed -i '' '/<\/resources>/i\
    <!-- Group creation / edition flow (flux-creation-unifie) -->\
    <string name="create_flow_optional">optional</string>\
    <string name="create_group_step_one_title">Introduce your group</string>\
    <string name="create_group_description_hint">Say in a few words what your group is about and who it is for.</string>\
    <string name="create_group_description_placeholder">E.g. A group for neighbours to meet, share good tips and help each other.</string>\
    <string name="create_group_step_two_title">What is your group about?</string>\
    <string name="create_group_step_three_title">Add a photo to your group</string>\
    <string name="create_group_preview_title">Preview of your group</string>\
    <string name="create_group_error_name">Give your group a name to continue.</string>\
    <string name="create_group_error_description">Add a few words to introduce your group.</string>\
    <string name="create_group_error_location">Enter a location to continue.</string>\
    <string name="back_edit_group_content">If you leave, the changes made to the group will be lost.</string>
' app/src/main/res/values-en/strings.xml
fi
