if [ -f "app/src/main/res/values-en/strings.xml" ]; then
sed -i '/<\/resources>/i \
    <string name="group_feed_post_unavailable">This post no longer exists.</string>' app/src/main/res/values-en/strings.xml
fi
