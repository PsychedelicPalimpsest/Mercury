package org.cadixdev.mercury;

import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.cadixdev.mercury.remapper.MercuryRemapper;
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
        MemoryMappingTree mappingTree = new MemoryMappingTree();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(args[0])

        ))) {
            Tiny2FileReader.read(bufferedReader, mappingTree);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TinyRemapper tinyRemapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createMappingProvider(mappingTree, "target", "source"))
                .propagateBridges(TinyRemapper.LinkedMethodPropagation.COMPATIBLE)
                .build();

        tinyRemapper.readInputs(Paths.get(args[1]));
        final Mercury mercury = new Mercury();
        mercury.getClassPath().add(Paths.get(args[1]));
        for (int i = START_LIBS; i <= args.length - 1; i++) {
            mercury.getClassPath().add(Paths.get(args[i]));
        }

        mercury.setSourceCompatibility(JavaCore.VERSION_17);
        mercury.getProcessors().add(MercuryRemapper.create(tinyRemapper.getEnvironment()));
        mercury.setFlexibleAnonymousClassMemberLookups(true);
        mercury.setGracefulClasspathChecks(true);


        // TODO : We have to (somehow) combine the develop and main branches.
        //       Only then can we properly get javadoc. FUCK

        System.out.println("Rewriting");
        try {
            mercury.rewrite(Paths.get(args[2]), Paths.get(args[3]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finished rewriting");
        System.exit(0);
    }
}
