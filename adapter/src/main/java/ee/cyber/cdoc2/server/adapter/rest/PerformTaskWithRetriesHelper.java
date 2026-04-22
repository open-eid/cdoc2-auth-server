package ee.cyber.cdoc2.server.adapter.rest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class PerformTaskWithRetriesHelper {
    private PerformTaskWithRetriesHelper() {
        // Utility class
    }

    public static <T> List<T> allOfWithRetries(
        List<Supplier<CompletableFuture<T>>> tasks,
        int maxRetries
    ) {
        List<CompletableFuture<T>> retryingFutures =
            tasks.stream()
                .map(task -> withRetries(task, maxRetries))
                .toList();

        return CompletableFuture.allOf(retryingFutures.toArray(new CompletableFuture[0]))
            .thenApply(v ->
                retryingFutures.stream()
                    .map(CompletableFuture::join)
                    .toList()
            ).join();
    }

    private static <T> CompletableFuture<T> withRetries(
        Supplier<CompletableFuture<T>> future,
        int maxRetries
    ) {
        CompletableFuture<T> promise = new CompletableFuture<>();

        BiConsumer<Integer, Throwable> attempt = new BiConsumer<>() {
            @Override
            public void accept(Integer retriesLeft, Throwable lastError) {
                future.get().whenComplete((result, error) -> {
                    if (error == null) {
                        promise.complete(result);
                    } else if (retriesLeft > 0) {
                        accept(retriesLeft - 1, error);
                    } else {
                        promise.completeExceptionally(error);
                    }
                });
            }
        };

        attempt.accept(maxRetries, null);
        return promise;
    }
}
