package org.tkit.onecx.product.store.slot.operator;

import static io.javaoperatorsdk.operator.api.reconciler.Constants.WATCH_CURRENT_NAMESPACE;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.product.store.slot.operator.client.DataService;

import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnAddFilter;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnUpdateFilter;

@ControllerConfiguration(name = "data", namespaces = WATCH_CURRENT_NAMESPACE, onAddFilter = DataController.SlotAddFilter.class, onUpdateFilter = DataController.SlotUpdateFilter.class)
public class DataController implements Reconciler<Data>, ErrorStatusHandler<Data> {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    private static final Digest DIGEST = new Digest();

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
        return UpdateControl.updateStatus(data);

    }

    @Override
    public ErrorStatusUpdateControl<Data> updateErrorStatus(Data slot,
            Context<Data> context, Exception e) {

        int responseCode = -1;
        String message = e.getMessage();
        if (e.getCause() instanceof WebApplicationException re) {
            responseCode = re.getResponse().getStatus();
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
        return ErrorStatusUpdateControl.updateStatus(slot);
    }

    private void updateStatusPojo(Data data, int responseCode) {
        var result = new DataStatus();
        result.setResponseCode(responseCode);
        result.setMd5(DIGEST.create(data.getSpec().getData()));
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

    public static class SlotAddFilter implements OnAddFilter<Data> {

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

    public static class SlotUpdateFilter implements OnUpdateFilter<Data> {

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
            return oldResource.getStatus().getMd5().equals(DIGEST.create(newResource.getSpec().getData()));
        }
    }

}
