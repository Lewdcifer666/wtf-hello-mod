package com.example.hello;

import com.deadlysoul.sortmod.api.ModAction;
import com.deadlysoul.sortmod.api.ModContext;
import com.deadlysoul.sortmod.api.WtfExternalMod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The reference mod.
 *
 * <p>It exists to prove the path, not to be useful: published to a GitHub
 * release, downloaded, verified, installed, loaded, enabled, and run when the
 * user presses a button. Every step of that is something that can go wrong on a
 * 2015 television, and this is the smallest thing that exercises all of them.
 *
 * <p>It hooks nothing. No catalog, no playback, no feedback, no ratings. The
 * specification asks for a harmless example, and a reference mod that reached
 * into the host would be a template for mods that do the same.
 *
 * <h3>What it does demonstrate</h3>
 * <ul>
 *   <li>the full lifecycle, logged, so the callbacks can be seen firing once each;
 *   <li>an action in the Details dialog, which is the whole v1 UI surface;
 *   <li>{@link com.deadlysoul.sortmod.api.ModPrefs}, so that data surviving an
 *       uninstall is something observable rather than a claim -- the greeting
 *       count is still there after Remove and reinstall, and gone after Clear Mod
 *       Data.
 * </ul>
 */
public final class HelloMod implements WtfExternalMod {

    private static final String COUNT = "greetings";

    private ModContext host;
    private boolean enabled;

    /** The host constructs this reflectively, so the no-argument constructor matters. */
    public HelloMod() {}

    @Override
    public void onLoad(ModContext context) {
        this.host = context;
        host.log().i("onLoad: api " + context.apiVersion()
                   + ", data in " + context.dataDir());
    }

    @Override
    public void onEnable() {
        enabled = true;
        host.log().i("onEnable");
    }

    @Override
    public void onDisable() {
        enabled = false;
        host.log().i("onDisable");
    }

    @Override
    public void onUnload() {
        host.log().i("onUnload -- the classes stay in the process until it restarts");
        host = null;
    }

    @Override
    public List<ModAction> getActions() {
        if (!enabled) return Collections.emptyList();
        List<ModAction> actions = new ArrayList<ModAction>();
        actions.add(new ModAction("say_hello", "Say hello"));
        actions.add(new ModAction("forget", "Forget the count"));
        return actions;
    }

    @Override
    public void onAction(String actionId) {
        if ("say_hello".equals(actionId)) {
            int count = host.prefs().getInt(COUNT, 0) + 1;
            host.prefs().putInt(COUNT, count);
            host.prefs().save();

            host.toast("Hello from an external mod. That is " + count
                     + (count == 1 ? " time." : " times."));
            host.log().i("said hello, count now " + count);
            return;
        }

        if ("forget".equals(actionId)) {
            host.prefs().remove(COUNT);
            host.prefs().save();
            host.toast("Forgotten. Back to zero.");
            host.log().i("count reset");
            return;
        }

        host.log().w("unknown action: " + actionId);
    }
}
