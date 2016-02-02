package social.entourage.android;

import dagger.Component;

/**
 * Component linked to DrawerActivity lifecycle
 * Provides a DrawerPresenter
 * @see DrawerActivity
 * @see DrawerPresenter
 */
@ActivityScope
@Component(
        dependencies = EntourageComponent.class,
        modules = DrawerModule.class
)
@SuppressWarnings("unused")
public interface DrawerComponent {
    void inject(DrawerActivity activity);

    DrawerPresenter getDrawerPresenter();
}
