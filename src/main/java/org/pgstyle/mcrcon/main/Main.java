package org.pgstyle.mcrcon.main;

import org.pgstyle.mcrcon.control.CommandLineArguments;
import org.pgstyle.mcrcon.control.MinecraftRcon;

public class Main {

    public static void main(String[] args) {
        System.exit(new MinecraftRcon(CommandLineArguments.fromArgs(args)).call());
    }

}
