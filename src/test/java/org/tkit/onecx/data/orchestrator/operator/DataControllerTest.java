package org.tkit.onecx.data.orchestrator.operator;

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

    static final Logger log = LoggerFactory.getLogger(DataControllerTest.class);
    public static final String WORKSPACE = "workspace";
    public static final String THEME = "theme";
    public static final String TEST_1 = "test-1";

    @Inject
    Operator operator;

    @Inject
    KubernetesClient client;

    @BeforeAll
    static void init() {
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
                Arguments.of(TEST_1, createSpec(WORKSPACE, "{\"items1\":[]}"), DataStatus.Status.CREATED),
                Arguments.of("test-3", createSpec("permission", "{\"items2\":[]}"), DataStatus.Status.UPDATED),
                Arguments.of("test-error-1", createSpec("tenant", "{\"items3\":[]}"), DataStatus.Status.ERROR),
                Arguments.of("test-error-2", createSpec(THEME, "{\"items4\":[]}"), DataStatus.Status.ERROR));
    }

    private static DataSpec createSpec(String key, String data) {
        return createSpec(key, data, "description");
    }

    private static DataSpec createSpec(String key, String data, String description) {
        DataSpec spec = new DataSpec();
        spec.setKey(key);
        spec.setDescription(description);
        spec.setData(data);
        spec.setOrgId("default");
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
                Arguments.of("test-wrong-3", createSpec(THEME, null)),
                Arguments.of("test-wrong-4", createSpec(THEME, "")));
    }

    @Test
    void dataWrongKeySpecTest() {

        operator.start();

        var spec = new DataSpec();
        spec.setData(WORKSPACE);
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
        m.setAppId(TEST_1);
        m.setProductName("product-test");
        m.setKey(WORKSPACE);
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

    @Test
    void dataUpdateErrorSpecTest() {

        operator.start();

        var spec = new DataSpec();
        spec.setData(WORKSPACE);
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

        var m = new DataSpec();
        m.setAppId(TEST_1);
        m.setProductName("product-test");
        m.setKey(WORKSPACE);
        m.setData("{}");

        client.resource(data).inNamespace(client.getNamespace())
                .edit(s -> {
                    s.setSpec(m);
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
                Arguments.of("test-update-1", createSpec(WORKSPACE, "{\"items\":[]}"), createSpec(null, "{\"items\":[]}")),
                Arguments.of("test-update-2", createSpec(WORKSPACE, "{\"items\":[]}"), createSpec("", "{\"items\":[]}")),
                Arguments.of("test-update-21", createSpec(WORKSPACE, "{\"items\":[]}"),
                        createSpec(WORKSPACE, "{\"items\": [ {} ]}")),
                Arguments.of("test-update-3-1", createSpec(WORKSPACE, "{\"items\":[]}", "test1"),
                        createSpec(WORKSPACE, "{\"items\":[]}", "test-2")),
                Arguments.of("test-update-3", createSpec(WORKSPACE, "{\"items\":[]}"), createSpec(WORKSPACE, null)),
                Arguments.of("test-update-4", createSpec(WORKSPACE, "{\"items\":[]}"), createSpec(WORKSPACE, "")));

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
