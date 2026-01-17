package net.pl3x.livemap;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import net.pl3x.livemap.commands.TestCommand;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.util.FileUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;

public class LiveMap extends JavaPlugin {
    private static LiveMap instance;

    public static LiveMap instance() {
        return instance;
    }

    private HttpdServer httpdServer;

    public LiveMap(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        this.httpdServer = new HttpdServer();

        getCommandRegistry().registerCommand(new TestCommand());
    }

    @Override
    public void start() {
        this.httpdServer.start();
    }

    @Override
    public void shutdown() {
        if (this.httpdServer != null) {
            this.httpdServer.stop();
        }
    }

    @Override
    @Nonnull
    public Path getDataDirectory() {
        return FileUtil.createDirs(super.getDataDirectory());
    }
}
