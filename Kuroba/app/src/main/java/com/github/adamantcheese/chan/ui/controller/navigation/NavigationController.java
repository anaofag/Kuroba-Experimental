/*
 * KurobaEx - *chan browser https://github.com/K1rakishou/Kuroba-Experimental/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.ui.controller.navigation;

import android.content.Context;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.github.adamantcheese.chan.Chan;
import com.github.adamantcheese.chan.controller.Controller;
import com.github.adamantcheese.chan.controller.transition.ControllerTransition;
import com.github.adamantcheese.chan.controller.transition.PopControllerTransition;
import com.github.adamantcheese.chan.controller.transition.PushControllerTransition;
import com.github.adamantcheese.chan.core.manager.ControllerNavigationManager;
import com.github.adamantcheese.chan.core.navigation.HasNavigation;
import com.github.adamantcheese.chan.utils.AndroidUtils;

import javax.inject.Inject;

public abstract class NavigationController extends Controller implements HasNavigation {

    @Inject
    ControllerNavigationManager controllerNavigationManager;

    protected ViewGroup container;
    protected ControllerTransition controllerTransition;
    protected boolean blockingInput = false;

    public NavigationController(Context context) {
        super(context);
        Chan.inject(this);
    }

    public boolean pushController(final Controller to) {
        return pushController(to, true);
    }

    public boolean pushController(final Controller to, boolean animated) {
        return pushController(to, animated ? new PushControllerTransition(controllerTransitionAnimatorSet) : null);
    }

    public boolean pushController(final Controller to, ControllerTransition controllerTransition) {
        final Controller from = getTop();

        if (blockingInput) {
            // Crash on beta and dev builds 
            if (!AndroidUtils.isStableBuild()) {
                throwDebugInfo("pushController", to, from, controllerTransition);
                return false;
            }

            return false;
        }

        if (from == null && controllerTransition != null) {
            // can't animate push if from is null, just disable the animation
            controllerTransition = null;
        }

        transition(from, to, true, controllerTransition);
        return true;
    }

    public boolean popController() {
        return popController(true);
    }

    public boolean popController(boolean animated) {
        return popController(animated ? new PopControllerTransition(controllerTransitionAnimatorSet) : null);
    }

    public boolean popController(ControllerTransition controllerTransition) {
        final Controller from = getTop();
        final Controller to = childControllers.size() > 1
                ? childControllers.get(childControllers.size() - 2)
                : null;

        if (blockingInput) {
            // Crash on beta and dev builds
            if (!AndroidUtils.isStableBuild()) {
                throwDebugInfo("popController", to, from, controllerTransition);
                return false;
            }

            return false;
        }

        transition(from, to, false, controllerTransition);
        return true;
    }

    private void throwDebugInfo(
            String tag,
            Controller to,
            Controller from,
            ControllerTransition controllerTransition
    ) {
        String debugInfo = tag + ": to=" + to.getClass().getSimpleName() + ", " +
                "from=" + from.getClass().getSimpleName() + ", " +
                "transition=" + controllerTransition.debugInfo();

        throw new IllegalStateException(debugInfo);
    }

    public boolean isBlockingInput() {
        return blockingInput;
    }

    public boolean beginSwipeTransition(final Controller from, final Controller to) {
        if (blockingInput) {
            return false;
        }

        if (this.controllerTransition != null) {
            throw new IllegalArgumentException("Cannot transition while another transition is in progress.");
        }

        blockingInput = true;
        to.onShow();

        return true;
    }

    public void swipeTransitionProgress(float progress) {
    }

    public void endSwipeTransition(final Controller from, final Controller to, boolean finish) {
        if (finish) {
            from.onHide();
            removeChildController(from);

            controllerNavigationManager.onControllerSwipedFrom(from);
            controllerNavigationManager.onControllerSwipedTo(to);
        } else {
            to.onHide();
        }

        controllerTransition = null;
        blockingInput = false;
    }

    public void transition(
            final Controller from,
            final Controller to,
            final boolean pushing,
            ControllerTransition controllerTransition
    ) {
        if (this.controllerTransition != null || blockingInput) {
            throw new IllegalArgumentException("Cannot transition while another transition is in progress.");
        }

        if (!pushing && childControllers.isEmpty()) {
            throw new IllegalArgumentException("Cannot pop with no controllers left");
        }

        if (to != null) {
            to.navigationController = this;
            to.previousSiblingController = from;
        }

        if (pushing && to != null) {
            addChildController(to);
            to.attachToParentView(container);
        }

        if (to != null) {
            to.onShow();
        }

        if (controllerTransition != null) {
            controllerTransition.from = from;
            controllerTransition.to = to;

            blockingInput = true;
            this.controllerTransition = controllerTransition;

            controllerTransition.setCallback(transition -> finishTransition(from, to, pushing));
            controllerTransition.perform();

            return;
        }

        finishTransition(from, to, pushing);
    }

    private void finishTransition(Controller from, Controller to, boolean pushing) {
        if (from != null) {
            from.onHide();
        }

        if (!pushing && from != null) {
            removeChildController(from);
        }

        controllerTransition = null;
        blockingInput = false;

        if (pushing) {
            controllerNavigationManager.onControllerPushed(to);
        } else {
            controllerNavigationManager.onControllerPopped(from);
        }
    }

    public boolean onBack() {
        if (blockingInput) {
            return true;
        }

        if (childControllers.size() > 0) {
            Controller top = getTop();
            if (top == null) {
                return false;
            }

            if (top.onBack()) {
                return true;
            } else {
                if (childControllers.size() > 1) {
                    popController();
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Controller top = getTop();
        return (top != null && top.dispatchKeyEvent(event));
    }
}
