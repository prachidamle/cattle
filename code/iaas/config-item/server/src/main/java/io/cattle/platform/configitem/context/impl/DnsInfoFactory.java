package io.cattle.platform.configitem.context.impl;

import static io.cattle.platform.core.model.tables.InstanceHostMapTable.INSTANCE_HOST_MAP;
import io.cattle.platform.configitem.context.dao.DnsInfoDao;
import io.cattle.platform.configitem.context.data.DnsEntryData;
import io.cattle.platform.configitem.server.model.ConfigItem;
import io.cattle.platform.configitem.server.model.impl.ArchiveContext;
import io.cattle.platform.core.constants.NetworkServiceConstants;
import io.cattle.platform.core.dao.NetworkDao;
import io.cattle.platform.core.model.Agent;
import io.cattle.platform.core.model.Instance;
import io.cattle.platform.core.model.InstanceHostMap;
import io.cattle.platform.core.model.Nic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.Lists;

@Named
public class DnsInfoFactory extends AbstractAgentBaseContextFactory {
    @Inject
    DnsInfoDao dnsInfoDao;
    @Inject
    NetworkDao networkDao;

    @Override
    protected void populateContext(Agent agent, Instance instance, ConfigItem item, ArchiveContext context) {
        boolean isVIPProviderConfigured = isVIPProviderConfigured(instance);
        List<DnsEntryData> dnsEntries = new ArrayList<DnsEntryData>();
        // 1. retrieve all instance links for the hosts
        dnsEntries.addAll(dnsInfoDao.getInstanceLinksHostDnsData(instance));
        // 2. retrieve all service links for the host
        dnsEntries.addAll(dnsInfoDao.getServiceHostDnsData(instance, isVIPProviderConfigured));
        // 3. retrieve self service links
        dnsEntries.addAll(dnsInfoDao.getSelfServiceLinks(instance, isVIPProviderConfigured));
        // 4. retrieve external service links
        dnsEntries.addAll(dnsInfoDao.getExternalServiceDnsData(instance));
        // 5. get dns service links
        dnsEntries.addAll(dnsInfoDao.getDnsServiceLinks(instance, isVIPProviderConfigured));

        // 6. aggregate the links based on the source ip address
        Map<String, DnsEntryData> processedDnsEntries = new HashMap<>();
        for (DnsEntryData dnsEntry : dnsEntries) {
            DnsEntryData newData = null;
            if (processedDnsEntries.containsKey(dnsEntry.getSourceIpAddress().getAddress())) {
                newData = processedDnsEntries.get(dnsEntry.getSourceIpAddress().getAddress());
                populateARecords(dnsEntry, newData);
                populateCnameRecords(dnsEntry, newData);
            } else {
                newData = dnsEntry;
            }

            processedDnsEntries.put(dnsEntry.getSourceIpAddress().getAddress(), newData);
        }
        context.getData().put("dnsEntries", processedDnsEntries.values());
    }

    protected void populateARecords(DnsEntryData dnsEntry, DnsEntryData newData) {
        Map<String, List<String>> resolve = newData.getResolve();
        for (String dnsName : dnsEntry.getResolve().keySet()) {
            Set<String> ips = new HashSet<>();
            if (resolve.containsKey(dnsName)) {
                ips.addAll(resolve.get(dnsName));
            }
            ips.addAll(dnsEntry.getResolve().get(dnsName));
            resolve.put(dnsName, Lists.newArrayList(ips));
            newData.setResolve(resolve);
        }
    }

    protected void populateCnameRecords(DnsEntryData dnsEntry, DnsEntryData newData) {
        Map<String, String> resolveCname = newData.getResolveCname();
        for (String dnsName : dnsEntry.getResolveCname().keySet()) {
            if (!resolveCname.containsKey(dnsName)) {
                resolveCname.putAll(dnsEntry.getResolveCname());
            }
            newData.setResolveCname(resolveCname);
        }
    }

    protected boolean isVIPProviderConfigured(Instance instance) {
        Nic primaryNic = networkDao.getPrimaryNic(instance.getId());
        if (primaryNic == null) {
            return false;
        }
        List<? extends InstanceHostMap> hostMaps = objectManager.find(InstanceHostMap.class,
                INSTANCE_HOST_MAP.INSTANCE_ID,
                instance.getId());

        return networkDao.getServiceProviderInstanceOnHost(NetworkServiceConstants.KIND_VIP,
                hostMaps.get(0).getHostId()) != null;
    }
}
