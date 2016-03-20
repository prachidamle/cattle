package io.cattle.platform.core.constants;

public class NetworkConstants {

    public static final String INTERNAL_DNS_IP = "169.254.169.250";
    public static final String INTERNAL_DNS_SEARCH_DOMAIN = "rancher.internal";

    public static final String FIELD_DYNAMIC_CREATE_VNET = "dynamicCreateVnet";
    public static final String FIELD_HOST_VNET_URI = "hostVnetUri";
    public static final String FIELD_MAC_PREFIX = "macPrefix";
    public static final String FIELD_CIDR = "cidr";
    public static final String FIELD_SERVICES_DOMAIN = "servicesDomain";

    public static final String KIND_HOSTONLY = "hostOnlyNetwork";
    public static final String KIND_NETWORK = "network";
    public static final String KIND_VIP_NETWORK = "vipNetwork";

}
