package org.tkit.onecx.data.orchestrator.operator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.xml.bind.DatatypeConverter;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.data.orchestrator.operator.client.DataService;

import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnAddFilter;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnUpdateFilter;
import io.smallrye.config.SmallRyeConfig;

@ControllerConfiguration(name = "data", maxReconciliationInterval = @MaxReconciliationInterval(interval = Constants.NO_MAX_RECONCILIATION_INTERVAL), informer = @Informer(name = "parameter", namespaces = Constants.WATCH_CURRENT_NAMESPACE, onAddFilter = DataController.AddFilter.class, onUpdateFilter = DataController.UpdateFilter.class))
public class DataController implements Reconciler<Data> {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Inject
    DataService service;

    @Override
    public UpdateControl<Data> reconcile(Data data, Context<Data> context)
            throws Exception {

        String appId = data.getSpec().getAppId();
        String productName = data.getSpec().getProductName();
        String key = data.getSpec().getKey();

        log.info("Reconcile data for key: {}, productName: {}, appId: {}", key, productName, appId);
        int responseCode = service.updateData(data);

        updateStatusPojo(data, responseCode);
        log.info("Data '{}' reconciled - updating status", data.getMetadata().getName());
        return UpdateControl.patchStatus(data);

    }

    @Override
    public ErrorStatusUpdateControl<Data> updateErrorStatus(Data slot,
            Context<Data> context, Exception e) {

        int responseCode = -1;
        String message = e.getMessage();
        if (e.getCause() instanceof WebApplicationException re) {
            responseCode = re.getResponse().getStatus();
            message = re.getResponse().readEntity(String.class);
        }
        if (e.getCause() instanceof DataService.MissingKeyConfiguration me) {
            message = me.getMessage();
        }

        log.error("Error reconcile resource", e);
        var status = new DataStatus();
        status.setResponseCode(responseCode);
        status.setStatus(DataStatus.Status.ERROR);
        status.setMessage(message);
        slot.setStatus(status);
        return ErrorStatusUpdateControl.patchStatus(slot);
    }

    private void updateStatusPojo(Data data, int responseCode) {
        var result = new DataStatus();
        result.setResponseCode(responseCode);
        result.setChecksum(createCheckSum(data.getSpec().getData()));
        var status = switch (responseCode) {
            case 201:
                yield DataStatus.Status.CREATED;
            case 200:
                yield DataStatus.Status.UPDATED;
            default:
                yield DataStatus.Status.UNDEFINED;
        };
        result.setStatus(status);
        data.setStatus(result);
    }

    public static class AddFilter implements OnAddFilter<Data> {

        @Override
        public boolean accept(Data resource) {
            if (resource.getSpec() == null) {
                return false;
            }
            if (resource.getSpec().getKey() == null || resource.getSpec().getKey().isEmpty()) {
                return false;
            }
            if (resource.getSpec().getData() == null) {
                return false;
            }
            return !resource.getSpec().getData().isEmpty();
        }
    }

    public static class UpdateFilter implements OnUpdateFilter<Data> {

        @Override
        public boolean accept(Data newResource, Data oldResource) {
            if (newResource.getSpec() == null) {
                return false;
            }
            if (newResource.getSpec().getKey() == null || newResource.getSpec().getKey().isEmpty()) {
                return false;
            }
            if (newResource.getSpec().getData() == null || newResource.getSpec().getData().isEmpty()) {
                return false;
            }
            if (oldResource.getStatus() == null) {
                return true;
            }
            if (oldResource.getStatus().getChecksum() == null) {
                return true;
            }
            return !oldResource.getStatus().getChecksum().equals(createCheckSum(newResource.getSpec().getData()));
        }
    }

    static MessageDigest createDigest(String name) {
        try {
            return MessageDigest.getInstance(name);
        } catch (NoSuchAlgorithmException e) {
            throw new CheckSumException(e);
        }
    }

    static String createCheckSum(String data) {
        var config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class).getConfigMapping(DataConfig.class);
        var digest = createDigest(config.digest());
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hash).toUpperCase();
    }

    static class CheckSumException extends RuntimeException {

        public CheckSumException(Throwable e) {
            super("Error create check-sum from the data content", e);
        }
    }
}
