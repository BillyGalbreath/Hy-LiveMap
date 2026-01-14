package net.pl3x.livemap;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import net.pl3x.livemap.commands.TestCommand;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class LiveMap extends JavaPlugin {
    public LiveMap(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getCommandRegistry().registerCommand(new TestCommand());
    }
}
