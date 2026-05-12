if [ -f "app/src/main/res/values-en/strings.xml" ]; then
sed -i '/<\/resources>/i \
    <string name="download_image_title">Downloading photo</string>\
    <string name="download_image_description">Downloading...</string>\
    <string name="download_started">Download started</string>\
    <string name="download_error">Error during download</string>' app/src/main/res/values-en/strings.xml
fi
