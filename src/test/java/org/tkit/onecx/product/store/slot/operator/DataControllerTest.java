package org.tkit.onecx.product.store.slot.operator;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DataControllerTest extends AbstractTest {

    final static Logger log = LoggerFactory.getLogger(DataControllerTest.class);

    @Inject
    Operator operator;

    @Inject
    KubernetesClient client;

    @BeforeAll
    public static void init() {
        Awaitility.setDefaultPollDelay(2, SECONDS);
        Awaitility.setDefaultPollInterval(2, SECONDS);
        Awaitility.setDefaultTimeout(5, SECONDS);
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    void dataTest(String name, DataSpec spec, DataStatus.Status status) {

        operator.start();

        Data data = new Data();
        data.setMetadata(new ObjectMetaBuilder().withName(name).withNamespace(client.getNamespace()).build());
        data.setSpec(spec);

        client.resource(data).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(status);
        });
    }

    private static Stream<Arguments> provideTestData() {
        return Stream.of(
                Arguments.of("test-1", createSpec("workspace", "{\"items\":[]}"), DataStatus.Status.CREATED),
                Arguments.of("test-3", createSpec("permission", "{\"items\":[]}"), DataStatus.Status.UPDATED),
                Arguments.of("test-error-1", createSpec("tenant", "{\"items\":[]}"), DataStatus.Status.ERROR),
                Arguments.of("test-error-2", createSpec("theme", "{\"items\":[]}"), DataStatus.Status.ERROR));
    }

    private static DataSpec createSpec(String key, String data) {
        DataSpec spec = new DataSpec();
        spec.setKey(key);
        spec.setDescription("description");
        spec.setData(data);
        return spec;
    }

    @ParameterizedTest
    @MethodSource("provideWrongData")
    void wrongDataTest(String name, DataSpec spec) {

        operator.start();

        Data data = new Data();
        data.setMetadata(new ObjectMetaBuilder().withName(name).withNamespace(client.getNamespace()).build());
        data.setSpec(spec);

        client.resource(data).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNull();
        });
    }

    private static Stream<Arguments> provideWrongData() {
        return Stream.of(
                Arguments.of("test-wrong-1", createSpec(null, "{\"items\":[]}")),
                Arguments.of("test-wrong-2", createSpec("", "{\"items\":[]}")),
                Arguments.of("test-wrong-3", createSpec("theme", null)),
                Arguments.of("test-wrong-4", createSpec("theme", "")));
    }

    @Test
    void dataWrongKeySpecTest() {

        operator.start();

        var spec = new DataSpec();
        spec.setData("workspace");
        spec.setKey("does-not-exists");

        Data data = new Data();
        data.setMetadata(
                new ObjectMetaBuilder().withName("does-not-exists-key-spec").withNamespace(client.getNamespace()).build());
        data.setSpec(spec);

        client.resource(data).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(DataStatus.Status.ERROR);
            assertThat(mfeStatus.getMessage()).isNotNull().isEqualTo("Missing configuration for key does-not-exists");
        });
    }

    @Test
    void dataNullSpecTest() {

        operator.start();

        Data data = new Data();
        data.setMetadata(new ObjectMetaBuilder().withName("null-spec").withNamespace(client.getNamespace()).build());
        data.setSpec(null);

        client.resource(data).serverSideApply();

        await().pollDelay(4, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNull();
        });

    }

    @Test
    void dataUpdateEmptySpecTest() {

        operator.start();

        var m = new DataSpec();
        m.setAppId("test-1");
        m.setProductName("product-test");
        m.setKey("workspace");
        m.setData("{}");

        Data data = new Data();
        data
                .setMetadata(new ObjectMetaBuilder()
                        .withName("to-update-spec").withNamespace(client.getNamespace()).build());
        data.setSpec(m);

        log.info("Creating test data object: {}", data);
        client.resource(data).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(DataStatus.Status.CREATED);
        });

        client.resource(data).inNamespace(client.getNamespace())
                .edit(s -> {
                    s.setSpec(null);
                    return s;
                });

        await().pollDelay(4, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(DataStatus.Status.CREATED);
        });
    }

    private static Stream<Arguments> provideUpdateData() {
        return Stream.of(
                Arguments.of("test-update-1", createSpec("workspace", "{\"items\":[]}"), createSpec(null, "{\"items\":[]}")),
                Arguments.of("test-update-2", createSpec("workspace", "{\"items\":[]}"), createSpec("", "{\"items\":[]}")),
                Arguments.of("test-update-21", createSpec("workspace", "{\"items\":[]}"),
                        createSpec("workspace", "{\"items\": [ {} ]}")),
                Arguments.of("test-update-3", createSpec("workspace", "{\"items\":[]}"), createSpec("workspace", null)),
                Arguments.of("test-update-4", createSpec("workspace", "{\"items\":[]}"), createSpec("workspace", "")));
    }

    @ParameterizedTest
    @MethodSource("provideUpdateData")
    void dataUpdateSpecTest(String name, DataSpec spec, DataSpec update) {

        operator.start();

        Data data = new Data();
        data
                .setMetadata(new ObjectMetaBuilder()
                        .withName(name).withNamespace(client.getNamespace()).build());
        data.setSpec(spec);

        client.resource(data).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(DataStatus.Status.CREATED);
        });

        client.resource(data).inNamespace(client.getNamespace())
                .edit(s -> {

                    s.setSpec(update);
                    return s;
                });

        await().pollDelay(4, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(DataStatus.Status.CREATED);
        });
    }
}
