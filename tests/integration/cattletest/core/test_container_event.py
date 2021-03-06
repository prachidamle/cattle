from common_fixtures import *  # NOQA
import time
from cattle import ApiError


@pytest.fixture(scope='module', autouse=True)
def update_event_settings(request, super_client):
    settings = super_client.list_setting()
    originals = []

    def update_setting(new_value, s):
        originals.append((setting, {'value': s.value}))
        s = super_client.update(s, {'value': new_value})
        wait_setting_active(super_client, s)

    for setting in settings:
        if setting.name == 'manage.nonrancher.containers' \
                and setting.value != 'true':
            update_setting('true', setting)

    def revert_settings():
        for s in originals:
            super_client.update(s[0], s[1])

    request.addfinalizer(revert_settings)


@pytest.fixture(scope='module')
def host(super_client, context):
    return super_client.reload(context.host)


@pytest.fixture(scope='module')
def agent_cli(context):
    return context.agent_client


@pytest.fixture(scope='module')
def user_id(context):
    return context.project.id


def test_container_event_create(client, host, agent_cli, user_id):
    # Submitting a 'start' containerEvent should result in a container
    # being created.
    external_id = random_str()

    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id)
    assert container.nativeContainer is True
    assert container.state == 'running'


def test_container_event_start_stop(client, host, agent_cli, user_id):
    # Submitting a 'stop' or 'die' containerEvent should result in a
    # container resource being stopped.
    external_id = random_str()

    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id)
    assert container.state == 'running'

    create_event(host, external_id, agent_cli, client, user_id, 'stop')
    container = client.wait_success(container)
    assert container.state == 'stopped'

    create_event(host, external_id, agent_cli, client, user_id, 'start')
    container = client.wait_success(container)
    assert container.state == 'running'

    # Sending a start event on a running container should have no effect
    create_event(host, external_id, agent_cli, client, user_id, 'start')
    container = client.wait_success(container)
    assert container.state == 'running'

    create_event(host, external_id, agent_cli, client, user_id, 'die')
    container = client.wait_success(container)
    assert container.state == 'stopped'

    # Sending a stop event on a stopped container should have no effect
    create_event(host, external_id, agent_cli, client, user_id, 'stop')
    container = client.wait_success(container)
    assert container.state == 'stopped'


def test_container_event_start(client, host, agent_cli, user_id):
    # Submitting a 'start' containerEvent should result in a container
    # being started.
    external_id = random_str()

    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id)
    assert container.state == 'running'

    create_event(host, external_id, agent_cli, client, user_id, 'stop')

    containers = client.list_container(externalId=external_id)
    assert len(containers) == 1
    container = client.wait_success(containers[0])
    assert container.state != 'removed'


def test_container_event_remove_start(client, host, agent_cli, user_id):
    # If a container is removed and then an event comes in for the container,
    # the event should be ignored.
    external_id = random_str()

    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id)
    assert container.state == 'running'

    container = client.wait_success(container.stop())
    assert container.state == 'stopped'
    container = client.wait_success(container.remove())
    assert container.state == 'removed'

    create_event(host, external_id, agent_cli, client, user_id, 'start')
    container = client.wait_success(container)
    assert container.state == 'removed'

    containers = client.list_container(externalId=external_id)
    assert len(containers) == 1


def test_container_event_destroy(client, host, agent_cli, user_id):
    # Submitting a 'destroy' containerEvent should result in a container
    # being removed.
    external_id = random_str()

    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id)
    assert container.state == 'running'

    create_event(host, external_id, agent_cli, client, user_id, 'destroy')

    container = client.wait_success(container)
    assert container.state == 'removed'

    # Sending a destroy event to a removed container should have no effect
    create_event(host, external_id, agent_cli, client, user_id, 'destroy')
    container = client.wait_success(container)
    assert container.state == 'removed'


def test_rancher_container_events(client, context, host, agent_cli, user_id):
    # A "normal" container (one created in Rancher) should also respond to
    # non-rancher container events
    container = context.create_container(name=random_str(),
                                         startOnCreate=False)
    assert container.state == 'stopped'

    inspect = new_inspect(random_str())
    inspect['Config']['Labels'] = {'io.rancher.container.uuid': container.uuid}

    # pass random external id to prove look up by rancher uuid works
    rand = random_str()
    create_event(host, rand, agent_cli, client, user_id, 'start', inspect)
    container = client.wait_success(container)
    assert container.state == 'running'

    create_event(host, rand, agent_cli, client, user_id, 'stop', inspect)
    container = client.wait_success(container)
    assert container.state == 'stopped'

    # Note that we don't pass inspect on destroy because it wont exist. In this
    # case, we have to pass the container's actual externalId
    ext_id = container.externalId
    create_event(host, ext_id, agent_cli, client, user_id, 'destroy')
    container = client.wait_success(container)
    assert container.state == 'removed'


def test_bad_agent(super_client, host):
    # Even though super_client will have permissions to create the container
    # event, additional logic should assert that the creator is a valid agent.
    with pytest.raises(ApiError) as e:
        super_client.create_container_event(
            reportedHostUuid=host.data.fields['reportedUuid'],
            externalId=random_str(),
            externalFrom='busybox:latest',
            externalTimestamp=int(time.time()),
            externalStatus='start')
    assert e.value.error.code == 'MissingRequired'


def test_bad_host(host, new_context):
    # If a host doesn't belong to agent submitting the event, the request
    # should fail.
    agent_cli = new_context.agent_client

    with pytest.raises(ApiError) as e:
        agent_cli.create_container_event(
            reportedHostUuid=host.data.fields['reportedUuid'],
            externalId=random_str(),
            externalFrom='busybox:latest',
            externalTimestamp=int(time.time()),
            externalStatus='start')
    assert e.value.error.code == 'InvalidReference'


def test_container_event_null_inspect(client, host, agent_cli, user_id):
    # Assert that the inspect can be null.
    external_id = random_str()

    create_event(host, external_id, agent_cli, client, user_id,
                 'start', None)

    def container_wait():
        containers = client.list_container(externalId=external_id)
        if len(containers) and containers[0].state != 'requested':
            return containers[0]
    container = wait_for(container_wait)
    assert container is not None


def test_requested_ip_address(super_client, client, host, agent_cli, user_id):
    external_id = random_str()
    inspect = new_inspect(external_id)
    inspect['NetworkSettings'] = {'IPAddress': '10.42.0.240'}
    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id, inspect=inspect)
    container = super_client.reload(container)
    assert container['data']['fields']['requestedIpAddress'] == '10.42.0.240'
    assert container.nics()[0].network().kind == 'hostOnlyNetwork'


def test_container_event_net_none(client, host, agent_cli, user_id):
    external_id = random_str()
    inspect = new_inspect(external_id)
    inspect['Config']['NetworkDisabled'] = True
    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id, inspect=inspect)
    assert container['networkMode'] == 'none'


def test_container_event_net_host(client, host, agent_cli, user_id):
    external_id = random_str()
    inspect = new_inspect(external_id)
    inspect['HostConfig'] = {'NetworkMode': 'host'}
    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id, inspect=inspect)
    assert container['networkMode'] == 'host'


def test_container_event_net_bridge(client, host, agent_cli, user_id):
    external_id = random_str()
    inspect = new_inspect(external_id)
    inspect['HostConfig'] = {'NetworkMode': 'bridge'}
    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id, inspect=inspect)
    assert container['networkMode'] == 'bridge'


def test_container_event_net_blank(client, host, agent_cli, user_id):
    external_id = random_str()
    inspect = new_inspect(external_id)
    inspect['HostConfig'] = {'NetworkMode': ''}
    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id, inspect=inspect)
    assert container['networkMode'] == 'bridge'


def test_container_event_net_container(client, host, agent_cli, user_id):
    target_external_id = random_str()
    target = create_native_container(client, host, target_external_id,
                                     agent_cli, user_id)
    external_id = random_str()
    inspect = new_inspect(external_id)
    inspect['HostConfig'] = {'NetworkMode': 'container:%s' % target.externalId}
    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id, inspect=inspect)
    assert container['networkMode'] == 'container'
    assert container['networkContainerId'] == target.id


def test_container_event_net_container_not_found(client, host, agent_cli,
                                                 user_id):
    external_id = random_str()
    inspect = new_inspect(external_id)
    inspect['HostConfig'] = {'NetworkMode': 'container:wont-be-found'}
    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id, inspect=inspect)
    assert container['networkMode'] == 'none'
    assert container['networkContainerId'] is None


def test_container_event_image_and_reg_cred(client, host, agent_cli, user_id,
                                            super_client):
    server = 'server{0}.io'.format(random_num())
    registry = client.create_registry(serverAddress=server,
                                      name=random_str())
    registry = client.wait_success(registry)
    reg_cred = client.create_registry_credential(
        registryId=registry.id,
        email='test@rancher.com',
        publicValue='rancher',
        secretValue='rancher')
    registry_credential = client.wait_success(reg_cred)
    name = server + '/rancher/authorized:latest'
    image_uuid = 'docker:' + name
    external_id = random_str()
    container = create_native_container(client, host, external_id,
                                        agent_cli, user_id, image=image_uuid)
    assert container.nativeContainer is True
    assert container.state == 'running'
    container = super_client.wait_success(container)
    assert container.registryCredentialId == registry_credential.id
    image = container.image()
    assert image.name == name
    assert image.registryCredentialId == registry_credential.id


def test_system_container(client, host, agent_cli, user_id):
    # System containers are ignored by rancher

    external_id = random_str()

    con_inspect = new_inspect(external_id)

    create_event(host, external_id, agent_cli, client, user_id,
                 'start', con_inspect, image='sim:rancher/agent:latset')
    create_event(host, external_id, agent_cli, client, user_id,
                 'start', con_inspect, image='rancher/agent:latset')
    create_event(host, external_id, agent_cli, client, user_id,
                 'start', con_inspect, image='rancher/agent')
    con_inspect['Config']['Labels'] = {
        'io.rancher.container.system': 'rancher-agent'}
    create_event(host, external_id, agent_cli, client, user_id,
                 'start', con_inspect, image='sim:foo/bar:latest')

    # Create this container as a way to wait for an instance to be successfully
    # created, so that we know that we can check for the previous containers
    create_native_container(client, host, random_str(), agent_cli, user_id)
    containers = client.list_container(externalId=external_id)
    assert len(containers) == 0


def create_native_container(client, host, external_id, user_agent_cli,
                            user_account_id, inspect=None, image=None):
    if not inspect:
        inspect = new_inspect(external_id)

    create_event(host, external_id, user_agent_cli, client, user_account_id,
                 'start', inspect, image=image)

    def container_wait():
        containers = client.list_container(externalId=external_id)
        if len(containers) and containers[0].state != 'requested':
            return containers[0]
    container = wait_for(container_wait)
    container = client.wait_success(container)
    return container


def create_event(host, external_id, agent_cli, client, user_account_id, status,
                 inspect=None, wait_and_assert=True,
                 image=None):
    timestamp = int(time.time())
    if (image is None):
        image = 'sim:busybox:latest'
    event = agent_cli.create_container_event(
        reportedHostUuid=host.data.fields['reportedUuid'],
        externalId=external_id,
        externalFrom=image,
        externalTimestamp=timestamp,
        externalStatus=status,
        dockerInspect=inspect)

    if wait_and_assert:
        assert event.reportedHostUuid == host.data.fields['reportedUuid']
        assert event.externalId == external_id
        assert event.externalFrom == image
        assert event.externalStatus == status
        assert event.externalTimestamp == timestamp

        def event_wait():
            try:
                created = client.reload(event)
                if created.state == 'created':
                    return event
            except ApiError as e:
                if e.error.status != 404:
                    raise e

        wait_for(event_wait)
        event = client.reload(event)

        assert host.id == event.hostId
        assert user_account_id == event.accountId
        assert event.state == 'created'

    return event


def new_inspect(rand):
    return {'Name': 'name-%s' % rand, 'Config': {'Image': 'sim:fake/image'}}


def _client_for_agent(credentials):
    return cattle.from_env(url=cattle_url(),
                           cache=False,
                           access_key=credentials.publicValue,
                           secret_key=credentials.secretValue)
