package org.cadixdev.mercury;

import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.cadixdev.mercury.remapper.MercuryRemapper;
import org.cadixdev.mercury.remapper.ParchmentRemapper;
import org.eclipse.jdt.core.JavaCore;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        final int MAPPINGS = 0;
        final int JAR_INPUT = 1;
        final int SOURCE_INPUT = 2;
        final int SOURCE_OUTPUT = 3;

        final int START_LIBS = 4;


        ParchmentRemapper remapper;
        try {
            remapper = new ParchmentRemapper(ParchmentTree.loadFile(Path.of(args[MAPPINGS])));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Mercury mercury = new Mercury();
        mercury.getClassPath().add(Paths.get(args[JAR_INPUT]));
        for (int i = START_LIBS; i <= args.length - 1; i++) {
            mercury.getClassPath().add(Paths.get(args[i]));
        }

        mercury.setSourceCompatibility(JavaCore.VERSION_17);
        mercury.getProcessors().add(remapper);
        mercury.setFlexibleAnonymousClassMemberLookups(true);
        mercury.setGracefulClasspathChecks(true);


        System.out.println("Rewriting");
        try {
            mercury.rewrite(Paths.get(args[SOURCE_INPUT]), Paths.get(args[SOURCE_OUTPUT]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finished rewriting");
        System.exit(0);
    }
}
