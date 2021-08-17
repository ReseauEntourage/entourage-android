#Changelog
##Latest tag: Version_7032755
##Previous tag: Version_7022657
* feature: EN-3919 allow empty messages for Encounters (2021-08-16) by <Francois Pellissier>
* feature: Rework button + icon: bigger and always orange (2021-07-23) by <François Pellissier>
* feature: Add lib to track keyboard visibility and improve error message display (2021-07-11) by <François Pellissier>
* feature: Replace invalid phone number toast with a textview and add a check before sending it to server (2021-07-11) by <François Pellissier>

* fix: EN-3974 public demands (actions) are not forced to be private anymore (2021-08-16) by <Francois Pellissier>
* fix: EN-3933, EN-3940 display right unread_messages badge at startup (2021-08-13) by <Francois Pellissier>
* fix: EN-3876 trying to fix move of plus icon when selected (2021-07-23) by <François Pellissier>
* fix: Rework error message UI and wording (2021-07-11) by <François Pellissier>
* fix: Add a phone number test for no internet connection (2021-07-11) by <François Pellissier>
* fix: Add error message for email field (2021-07-11) by <François Pellissier>
* fix: Add error message for first- and lastname fields (2021-07-11) by <François Pellissier>
* fix: Add snackbar error message when fail to retrieve messages (2021-07-09) by <François Pellissier>
* fix: Add snackbar error message when fail to retrieve POIs nearby (2021-07-09) by <François Pellissier>
* Fix BaseNewsfeedFragment API error Snackbar not showing (2021-07-09) by <François Pellissier>
* Fix wording on login page (2021-06-29) by <François Pellissier>
* fix: EN-3849 fix login activity crash (2021-06-22) by <François Pellissier>
* fix: remove some listeners when loading images (2021-06-21) by <François Pellissier>
* fix: missing import (2021-06-21) by <François Pellissier>
* fix: EN-3845 fix crash in partner screen (2021-06-21) by <François Pellissier>
* fix: Add a check to ensure app has Neo interface enabled (2021-06-20) by <François Pellissier>
* fix: EN-3756 fix crash when sharing an entourage (2021-06-17) by <François Pellissier>
* fix: EN-3717 crash when setting UserActionZone (2021-06-17) by <François Pellissier>
* fix: EN-3785 crash when empty event photo URL fix: EN-3752 crash when displaying map on certain devices (2021-06-12) by <François>
* fix: EN-3785 crash when empty event photo URL (2021-06-12) by <François>
* fix: EN-3785 crash when empty event photo URL (2021-06-12) by <François>
* fix: EN-3787 display explicit errors when error changing phone number (2021-06-11) by <François>
* fix: EN-3787 display explicit errors when error changing phone number (2021-06-11) by <François>
* Fix request permission callback (2021-06-10) by <Julien>
* fix: EN-3782 change wording for "changing my number" (2021-06-09) by <François>
* fix: [EN-3755] change wording (2021-06-04) by <François>
* fix: Update next button when firstname or lastname are edited (2021-06-07) by <Julien>
* fix: Update next button when phone number is edited (2021-06-07) by <Julien>

* chore: EN-3974 rename property for Public state of entourages (2021-08-16) by <Francois Pellissier>
* chore: switching to JAVA 11 (2021-08-13) by <Francois Pellissier>
* chore: update libraries and fix some little warnings (2021-07-23) by <Francois Pellissier>
* chore: Add tours UI tests (#39) (2021-07-23) by <GitHub>
* chore: update libraries (2021-07-23) by <François Pellissier>
* chore: fix some warnings (2021-07-11) by <François Pellissier>
* Add a no internet connection test to HomeExpertTest (2021-07-09) by <François Pellissier>
* Add MyEntouragesTest with a no internet connection test (2021-07-09) by <François Pellissier>
* Add GuideMapTest with a no internet connection test (2021-07-09) by <François Pellissier>
* Update LoginTest with 2 no internet connection tests (2021-07-09) by <François Pellissier>
* chore: minor fixes in bitrise.yml (2021-06-29) by <François Pellissier>
* chore: minor fixes in bitrise.yml (2021-06-29) by <François Pellissier>
* Add 3 new bitrise workflows (2021-06-29) by <François Pellissier>
* Update valid password (2021-06-21) by <Julien>
* Finish Home Neo UI tests (2021-06-20) by <François Pellissier>
* Start Home Neo UI tests (2021-06-20) by <François Pellissier>
* Add finally clause to ensure test password is reset after test and update test password (2021-06-20) by <François Pellissier>
* Add UI tests for plus button (2021-06-20) by <François Pellissier>
* Finish UI tests for Profile and Messages (2021-06-20) by <François Pellissier>
* Add UI tests for Profile fragments (2021-06-20) by <François Pellissier>
* Finish home expert feed UI tests (2021-06-20) by <François Pellissier>
* Add Home expert UI Tests (2021-06-20) by <François Pellissier>
* chore: fix non fatal error on connectivity manager (2021-06-19) by <François Pellissier>
* Change DeeplinksTest so that they pass even if user is not logged in (2021-06-19) by <François Pellissier>
* chore: bump version to 7.3 (2021-06-17) by <François Pellissier>
* chore: removing useless API calls (2021-06-11) by <François>
* chore: cleaning some compiler warning (2021-06-11) by <François>
* chore: cleaning old Android4.4-only code (2021-06-11) by <François>
* chore: update to kotlin 1.5.10 (2021-06-11) by <François>
* Finish replacing AndroidImageCropper lib with CropMe lib (2021-06-10) by <Julien>
* Move image transformation methods to Utils class (2021-06-10) by <Julien>
* Start replacing android-image-cropper with cropme lib (2021-06-09) by <Julien>
* Replace Picasso lib with Glide to manage image display (2021-06-08) by <Julien>
* Add explanations to sleep calls in SignUpTest (2021-06-07) by <Julien>
* Add neo home check for neighbour journey (2021-06-07) by <Julien>
* Finish sign up tests UI (2021-06-07) by <Julien>
* Add more sign up tests and rearrange them all (2021-06-07) by <Julien>
* Add tests for user type (2021-06-07) by <Julien>
* Add valid phone number test but disable it while we cannot remove the number form db and reuse it again (2021-06-07) by <Julien>
* Add invalid phone number test and a class to test toast (2021-06-07) by <Julien>
* Write firstname, lastname and phone number tests (2021-06-07) by <Julien>
* Create sign up tests file (2021-06-07) by <Julien>
* chore: Fix DeepLinkingTestWebview tests (we use now browser tab instead of webview) (2021-06-04) by <François Pellissier>
* chore: Fix DeepLinkingTestEntourage by changing tested view and adding a delay (2021-06-04) by <François Pellissier>
* chore: Fix DeepLinkingTestFeed by adding a delay and DeepLinkingTestEvents by changing checked text (2021-06-04) by <François Pellissier>
* chore: Add swipe checks to pre-onboarding tests (2021-06-04) by <François Pellissier>
* chore: Add another pre-onboarding UI tests and clean file (2021-06-04) by <François Pellissier>
* chore: Clean login and pre-onboarding tests (2021-06-04) by <François Pellissier>
* chore: Improve login tests and add pre-onboarding tests (2021-06-04) by <François Pellissier>
* chore: Add UI test from app launch to home fragment (2021-06-04) by <François Pellissier>
* chore: Fix nullability checks on view ids after PR review (2021-06-04) by <François Pellissier>
* chore: Clean code after PR review (2021-06-04) by <François Pellissier>
* chore: Remove unused dependencies (2021-06-04) by <François Pellissier>
* chore: Remove all !! operators to avoid most NullPointerException occurrences (2021-06-04) by <François Pellissier>
* chore: add bitrise secrets to ignore list (2021-06-03) by <François>
