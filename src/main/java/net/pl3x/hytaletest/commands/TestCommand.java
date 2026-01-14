package net.pl3x.hytaletest.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class TestCommand extends CommandBase {
    public TestCommand() {
        super("test", "Testing commands api");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext ctx) {
        ctx.sendMessage(Message.raw("Test passed!"));
    }
}
