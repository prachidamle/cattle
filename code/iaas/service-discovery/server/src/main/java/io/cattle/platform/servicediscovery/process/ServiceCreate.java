package io.cattle.platform.servicediscovery.process;

import io.cattle.platform.core.dao.NetworkDao;
import io.cattle.platform.core.constants.LoadBalancerConstants;
import io.cattle.platform.core.model.Service;
import io.cattle.platform.engine.handler.HandlerResult;
import io.cattle.platform.engine.process.ProcessInstance;
import io.cattle.platform.engine.process.ProcessState;
import io.cattle.platform.json.JsonMapper;
import io.cattle.platform.object.util.DataAccessor;
import io.cattle.platform.process.common.handler.AbstractObjectProcessHandler;
import io.cattle.platform.servicediscovery.api.constants.ServiceDiscoveryConstants;
import io.cattle.platform.servicediscovery.api.constants.ServiceDiscoveryConstants.KIND;
import io.cattle.platform.servicediscovery.service.ServiceDiscoveryService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ServiceCreate extends AbstractObjectProcessHandler {

    @Inject
    ServiceDiscoveryService sdService;
    @Inject
    JsonMapper jsonMapper;

    @Inject
    NetworkDao ntwkDao;

    @Override
    public String[] getProcessNames() {
        return new String[] { ServiceDiscoveryConstants.PROCESS_SERVICE_CREATE };
    }

    @Override
    public HandlerResult handle(ProcessState state, ProcessInstance process) {
        Service service = (Service) state.getResource();
        if (service.getKind().equalsIgnoreCase(KIND.LOADBALANCERSERVICE.name())) {
            List<? extends Long> certIds = DataAccessor.fromMap(state.getData())
                    .withKey(LoadBalancerConstants.FIELD_LB_CERTIFICATE_IDS).withDefault(Collections.EMPTY_LIST)
                    .asList(jsonMapper, Long.class);
            Long defaultCertId = DataAccessor.fromMap(state.getData())
                    .withKey(LoadBalancerConstants.FIELD_LB_DEFAULT_CERTIFICATE_ID).as(Long.class);
            sdService.createLoadBalancerService(service, certIds, defaultCertId);
        }
        
        sdService.setVIP(service);
        return null;
    }
}
