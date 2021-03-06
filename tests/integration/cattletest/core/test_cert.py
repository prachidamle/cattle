from common_fixtures import *  # NOQA
from cattle import ApiError


RESOURCE_DIR = os.path.join(os.path.dirname(os.path.realpath(__file__)),
                            'resources/certs')


def test_create_cert_basic(client):
    cert = _read_cert("san_domain_com.crt")
    key = _read_cert("san_domain_com.key")
    cert1 = client. \
        create_certificate(name=random_str(),
                           cert=cert,
                           key=key)
    cert1 = client.wait_success(cert1)
    assert cert1.state == 'active'
    assert cert1.cert == cert
    assert cert1.certFingerprint is not None
    assert cert1.expiresAt is not None
    assert cert1.CN is not None
    assert cert1.issuer is not None
    assert cert1.issuedAt is not None
    assert cert1.algorithm is not None
    assert cert1.version is not None
    assert cert1.serialNumber is not None
    assert cert1.keySize == 2048
    assert cert1.subjectAlternativeNames is not None

    return cert1


def test_create_cert_invalid_cert(client):
    cert = _read_cert("cert_invalid.pem")
    key = _read_cert("key.pem")
    with pytest.raises(ApiError) as e:
        client. \
            create_certificate(name=random_str(),
                               cert=cert,
                               key=key)
    assert e.value.error.status == 422
    assert e.value.error.code == 'InvalidFormat'


def test_create_cert_chain(client):
    cert = _read_cert("enduser-example.com.crt")
    key = _read_cert("enduser-example.com.key")
    chain = _read_cert("enduser-example.com.chain")
    cert1 = client. \
        create_certificate(name=random_str(),
                           cert=cert,
                           key=key,
                           certChain=chain)
    cert1 = client.wait_success(cert1)
    assert cert1.state == 'active'
    assert cert1.cert == cert
    return cert1


def _read_cert(name):
    with open(os.path.join(RESOURCE_DIR, name)) as f:
        return f.read()
