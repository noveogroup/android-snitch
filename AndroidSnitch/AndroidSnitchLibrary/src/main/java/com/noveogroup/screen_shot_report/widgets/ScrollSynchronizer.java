package com.noveogroup.screen_shot_report.widgets;

// CR is this class really needed ?
public class ScrollSynchronizer {

    // CR Holder is useless
    private static class Holder {
        private static ScrollSynchronizer instance = new ScrollSynchronizer();
    }

    private Object locker = null;
    private boolean locked = false;

    private ScrollSynchronizer() {
    }

    public static ScrollSynchronizer getInstance() {
        return Holder.instance;
    }

    public void tryLock(final Object scrollView) {
        if (locker == null || !locked) {
            locker = scrollView;
            locked = true;
        }
    }

    public void tryUnlock(final Object scrollView) {
        if (locker == scrollView) {
            locked = false;
            // CR locker = null may be ?
        }
    }

    public boolean amILocker(final Object scrollView) {
        return (locker == scrollView);
    }

    public boolean isLocked() {
        return locked;
    }

    public void reset() {
        locker = null;
        locked = false;
    }
}
