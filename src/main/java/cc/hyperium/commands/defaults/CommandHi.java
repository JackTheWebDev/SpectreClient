/*File created 12/15/2020 by JackTheWebDev as a test of commands*/

package cc.hyperium.commands.defaults;

import cc.hyperium.commands.BaseCommand;
import cc.hyperium.commands.CommandException;
import cc.hyperium.handlers.handlers.chat.GeneralChatHandler;


public class CommandHi implements BaseCommand {


    @Override
    public String getName() {
        return "hi";
    }

    @Override
    public String getUsage() {
        return "Usage: /hi";
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        GeneralChatHandler.instance().sendMessage("Hi Spectre Member!"); //Send a message
    }
}
