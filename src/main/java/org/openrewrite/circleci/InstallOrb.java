package org.openrewrite.circleci;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.yaml.*;
import org.openrewrite.yaml.tree.Yaml;

@Value
@EqualsAndHashCode(callSuper = true)
public class InstallOrb extends Recipe {
    @Option(displayName = "Name",
            description = "The orb key to be followed by an orb slug identifying a specific orb version.",
            example = "kube")
    String name;

    @Option(displayName = "Slug",
            description = "A specific orb to install, in the form `<namespace>/<orb-name>@1.2.3`.",
            example = "circleci/kubernetes@0.11.0")
    String slug;

    @Override
    public String getDisplayName() {
        return "Install an orb";
    }

    @Override
    public String getDescription() {
        return "Install a CircleCI [orb](https://circleci.com/docs/2.0/orb-intro/) if it is not already installed.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getSingleSourceApplicableTest() {
        return new HasSourcePath<>(".circleci/config.yml");
    }

    @Override
    protected YamlVisitor<ExecutionContext> getVisitor() {
        JsonPathMatcher orbs = new JsonPathMatcher("$.orbs");
        return new YamlIsoVisitor<ExecutionContext>() {
            @Override
            public Yaml.Document visitDocument(Yaml.Document document, ExecutionContext executionContext) {
                Yaml.Document d = super.visitDocument(document, executionContext);
                if (!orbs.find(getCursor()).isPresent() || Boolean.TRUE.equals(getCursor().getMessage("INSERT_ORB"))) {
                    doAfterVisit(new MergeYamlVisitor<>(document.getBlock(), "" +
                            "orbs:\n" +
                            "  " + name + ": " + slug,
                            false));
                }
                return d;
            }

            @Override
            public Yaml.Mapping visitMapping(Yaml.Mapping mapping, ExecutionContext ctx) {
                if (orbs.matches(getCursor().getParentOrThrow())) {
                    for (Yaml.Mapping.Entry entry : mapping.getEntries()) {
                        if (entry.getValue() instanceof Yaml.Scalar) {
                            String existingSlug = ((Yaml.Scalar) entry.getValue()).getValue();
                            if (slug.split("@")[0].equals(existingSlug.split("@")[0])) {
                                return mapping;
                            }
                        }
                    }

                    getCursor().putMessageOnFirstEnclosing(Yaml.Document.class, "INSERT_ORB", true);
                }

                return super.visitMapping(mapping, ctx);
            }
        };
    }
}
