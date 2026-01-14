package net.pl3x.hytaletest;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import net.pl3x.hytaletest.commands.TestCommand;
import net.pl3x.hytaletest.listeners.BreakBlockEventSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HytaleTest extends JavaPlugin {
    public HytaleTest(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getCommandRegistry().registerCommand(new TestCommand());

        getEntityStoreRegistry().registerSystem(new BreakBlockEventSystem());
    }
}
