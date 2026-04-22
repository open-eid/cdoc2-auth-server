package ee.cyber.cdoc2.server.adapter.resource;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ResourceLoaderWrapper {
    private final ResourceLoader resourceLoader;

    /**
     * Loads a resource, attempting the file system first. Falls back to classpath.
     *
     * @param name resource name
     * @return resource
     */
    public Resource loadResource(String name) {
        Resource resource = resourceLoader.getResource("file:" + name);
        if (resource.exists()) {
            return resource;
        }
        return resourceLoader.getResource("classpath:" + name);
    }
}
