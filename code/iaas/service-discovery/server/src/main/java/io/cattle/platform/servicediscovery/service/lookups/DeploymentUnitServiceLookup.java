package io.cattle.platform.servicediscovery.service.lookups;

import static io.cattle.platform.core.model.tables.ServiceTable.*;

import io.cattle.platform.core.model.DeploymentUnit;
import io.cattle.platform.core.model.Service;
import io.cattle.platform.object.ObjectManager;

import java.util.Collection;

import javax.inject.Inject;

public class DeploymentUnitServiceLookup implements ServiceLookup {

    @Inject
    ObjectManager objectMgr;

    @Override
    public Collection<? extends Service> getServices(Object obj) {
        if (!(obj instanceof DeploymentUnit)) {
            return null;
        }
        DeploymentUnit du = (DeploymentUnit)obj;
        return objectMgr.find(Service.class,
                SERVICE.ID, du.getServiceId());
    }

}
