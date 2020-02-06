package social.entourage.android.view;

import android.app.Activity;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import org.jetbrains.annotations.NotNull;
import social.entourage.android.R;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class EntourageTapPrompt {
    private String primaryText;
    private String secondaryText;
    @IdRes
    private int id;
    @Nullable
    private EntourageTapPrompt next;

    public EntourageTapPrompt(@IdRes int tapId, @NotNull String primary, @Nullable String secondary, @Nullable EntourageTapPrompt nextPrompt) {
        id= tapId;
        primaryText = primary;
        secondaryText = secondary;
        next = nextPrompt;
    }

    public void show(Activity activity) {
        new MaterialTapTargetPrompt.Builder(activity)
                .setTarget(id)
                .setPrimaryText(primaryText)
                .setSecondaryText(secondaryText)
                .setBackgroundColour(ContextCompat.getColor(activity, R.color.accent))
                .setPromptStateChangeListener((prompt, state) -> {
                    if (next != null && state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                        next.show(activity);
                    }
                })
                .setCaptureTouchEventOutsidePrompt(true)
                .show();
    }
}
