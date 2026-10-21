sed -i '' '/<\/resources>/i\
    <!-- Parcours de création / d’édition d’entraide (flux-creation-unifie) -->\
    <string name="create_action_category_label">Catégorie</string>\
    <string name="create_action_step_description_title">Dites-nous en plus</string>\
    <string name="create_action_title_label">Titre</string>\
    <string name="create_action_photo_label">Photo</string>\
    <string name="create_action_description_counter">%s /800</string>\
    <string name="create_action_preview_title">Aperçu de votre %s</string>\
    <string name="create_action_error_title">Donnez un titre pour continuer.</string>\
    <string name="create_action_error_description">Ajoutez une description pour continuer.</string>\
    <string name="create_action_error_location">Indiquez une adresse pour continuer.</string>\
    <string name="create_action_error_sharing">Répondez par oui ou par non pour continuer.</string>
' app/src/main/res/values/strings.xml
if [ -f "app/src/main/res/values-en/strings.xml" ]; then
sed -i '' '/<\/resources>/i\
    <!-- Solidarity action creation / edition flow (flux-creation-unifie) -->\
    <string name="create_action_category_label">Category</string>\
    <string name="create_action_step_description_title">Tell us more</string>\
    <string name="create_action_title_label">Title</string>\
    <string name="create_action_photo_label">Photo</string>\
    <string name="create_action_description_counter">%s /800</string>\
    <string name="create_action_preview_title">Preview of your %s</string>\
    <string name="create_action_error_title">Give a title to continue.</string>\
    <string name="create_action_error_description">Add a description to continue.</string>\
    <string name="create_action_error_location">Enter an address to continue.</string>\
    <string name="create_action_error_sharing">Answer yes or no to continue.</string>
' app/src/main/res/values-en/strings.xml
fi
