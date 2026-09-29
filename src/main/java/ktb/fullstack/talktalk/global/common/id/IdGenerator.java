package ktb.fullstack.talktalk.global.common.id;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

import java.util.UUID;

public final class IdGenerator {

    private static final NoArgGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    private IdGenerator() {}

    public static UUID nextId() {
        return GENERATOR.generate();
    }
}
