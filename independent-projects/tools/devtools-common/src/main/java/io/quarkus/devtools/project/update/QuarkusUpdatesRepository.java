package io.quarkus.devtools.project.update;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import io.quarkus.bootstrap.resolver.maven.BootstrapMavenException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;
import io.quarkus.bootstrap.util.DependencyUtils;
import io.quarkus.platform.descriptor.loader.json.ResourceLoader;
import io.quarkus.platform.descriptor.loader.json.ResourceLoaders;

public final class QuarkusUpdatesRepository {

    private QuarkusUpdatesRepository() {
    }

    private static final String QUARKUS_RECIPE_GA = "io.quarkus:quarkus-update-recipes";

    public static List<String> fetchRecipes(MavenArtifactResolver artifactResolver, String recipeVersion, String currentVersion,
            String targetVersion) {
        final String gav = QUARKUS_RECIPE_GA + ":" + recipeVersion;
        try {
            final ResourceLoader resourceLoader = ResourceLoaders.resolveFileResourceLoader(
                    artifactResolver.resolve(DependencyUtils.toArtifact(gav)).getArtifact().getFile());

            return resourceLoader.loadResourceAsPath("quarkus-updates/core",
                    path -> {
                        try (final Stream<Path> pathStream = Files.walk(path)) {
                            return pathStream
                                    .filter(p -> p.getFileName().toString().matches("^\\d\\H+.ya?ml$"))
                                    .filter(p -> shouldApplyRecipe(p.getFileName().toString(), currentVersion, targetVersion))
                                    .map(p -> {
                                        try {
                                            return new String(Files.readAllBytes(p));
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).collect(Collectors.toList());
                        }
                    });
        } catch (BootstrapMavenException e) {
            throw new RuntimeException("Failed to resolve artifact: " + gav, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load recipes in artifact: " + gav, e);
        }
    }

    static boolean shouldApplyRecipe(String recipeFileName, String currentVersion, String targetVersion) {
        String recipeVersion = recipeFileName.replaceFirst("[.][^.]+$", "");
        final DefaultArtifactVersion recipeAVersion = new DefaultArtifactVersion(recipeVersion);
        final DefaultArtifactVersion currentAVersion = new DefaultArtifactVersion(currentVersion);
        final DefaultArtifactVersion targetAVersion = new DefaultArtifactVersion(targetVersion);
        return currentAVersion.compareTo(recipeAVersion) < 0 && targetAVersion.compareTo(recipeAVersion) >= 0;
    }
}
